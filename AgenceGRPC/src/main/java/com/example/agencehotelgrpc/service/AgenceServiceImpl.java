package com.example.agencehotelgrpc.service;

import com.example.agencehotelgrpc.models.Hotel;
import com.example.agencehotelgrpc.repositories.AgenceRepository;
import com.example.agencehotelgrpc.serviceUtils.AgenceService;
import com.example.agenthotelgrpc.proto.*;
import io.grpc.Status;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import io.grpc.stub.StreamObserver;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@GrpcService
public class AgenceServiceImpl extends AgenceServiceGrpc.AgenceServiceImplBase {
    private final AgenceRepository agenceRepository;
    private final AgenceService agenceService;

    @GrpcClient("hotel-service")
    private HotelServiceGrpc.HotelServiceBlockingStub hotelServiceBlockingStub;

    @GrpcClient("hotel-service")
    private HotelServiceGrpc.HotelServiceStub hotelServiceStub; // stub pour les appels asynchrones (callbacks)

    public AgenceServiceImpl(AgenceRepository agenceRepository, AgenceService agenceService) {
        this.agenceRepository = agenceRepository;
        this.agenceService = agenceService;
    }

    @Override
    @Transactional
    public void authentifier(AuthentifierRequest request, StreamObserver<AuthentifierResponse> responseObserver) {
        try {
            // Créer un StreamObserver pour envoyer les requêtes à l'hôtel
            StreamObserver<AuthentifierHotelRequest> requestObserver = hotelServiceStub.authentifier(
                    new StreamObserver<AuthentifierResponse>() {
                        @Override
                        public void onNext(AuthentifierResponse response) {
                            // Si l'authentification réussie, on la communique
                            responseObserver.onNext(response);
                        }

                        @Override
                        public void onError(Throwable t) {
                            responseObserver.onError(Status.INTERNAL
                                    .withDescription("Erreur lors de l'authentification avec l'hôtel: " + t.getMessage())
                                    .asException());
                        }

                        @Override
                        public void onCompleted() {
                            responseObserver.onCompleted();
                        }
                    });

            // Récupérer les hôtels partenaires de l'agence
            List<Hotel> hotelsPartenaires = agenceRepository.findById(request.getIdAgence())
                    .orElseThrow(() -> new RuntimeException("Agence non trouvée"))
                    .getHotelsPartenaires();

            System.err.println("[AUTH] : " + hotelsPartenaires);

            boolean allHotelsAuthenticated = true;

            // Envoi de la requête pour chaque hôtel partenaire
            for (Hotel hotel : hotelsPartenaires) {
                try {
                    AuthentifierHotelRequest hotelRequest = AuthentifierHotelRequest.newBuilder()
                            .setIdAgence(request.getIdAgence())
                            .setIdHotel(hotel.getId())
                            .setPassword(request.getPassword())
                            .build();

                    // Envoi de la requête d'authentification
                    requestObserver.onNext(hotelRequest);
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'envoi de la requête d'authentification à l'hôtel ID: "
                            + hotel.getId() + ", erreur: " + e.getMessage());
                    allHotelsAuthenticated = false;
                }
            }

            // Si tous les hôtels sont authentifiés correctement, on marque la fin du stream
            if (allHotelsAuthenticated) {
                requestObserver.onCompleted();
            } else {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Erreur lors de l'authentification de certains hôtels.")
                        .asException());
            }

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Erreur interne du service: " + e.getMessage())
                    .asException());
        }
    }

    @Override
    @Transactional
    public StreamObserver<ComparateurAgenceRequest> compare(StreamObserver<ComparateurResponse> responseObserver) {
        List<OffreComparateurDTO> accumulatedOffres = new ArrayList<>();
        CountDownLatch completionLatch = new CountDownLatch(1);

        return new StreamObserver<ComparateurAgenceRequest>() {
            @Override
            @Transactional
            public void onNext(ComparateurAgenceRequest request) {
                System.err.println("Requête reçue pour l'agence ID: " + request.getIdAgence());

                int idAgence = request.getIdAgence();
                String dateDebut = request.getDateDebut();
                String dateFin = request.getDateFin();
                String ville = request.getVille();
                int nbPersonnes = request.getNbPersonnes();
                int nbEtoiles = request.getNbEtoiles();

                List<Hotel> hotelsPartenaires = agenceRepository.findById(idAgence)
                        .orElseThrow(() -> new RuntimeException("Agence non trouvée"))
                        .getHotelsPartenaires()
                        .stream()
                        .filter(hotel -> hotel.getAdresse() != null
                                && hotel.getAdresse().getVille() != null
                                && hotel.getAdresse().getVille().equalsIgnoreCase(ville)
                                && hotel.getNbEtoiles() >= nbEtoiles)
                        .collect(Collectors.toList());

                for (Hotel hotel : hotelsPartenaires) {
                    try {
                        // Créer un nouveau CountDownLatch pour cette requête d'hôtel
                        CountDownLatch hotelLatch = new CountDownLatch(1);

                        ComparateurHotelRequest hotelRequest = ComparateurHotelRequest.newBuilder()
                                .setIdAgence(idAgence)
                                .setIdHotel(hotel.getId())
                                .setDateDebut(dateDebut)
                                .setDateFin(dateFin)
                                .setNbPersonnes(nbPersonnes)
                                .build();

                        System.err.println("Requête préparée pour l'hôtel " + hotel.getId());

                        StreamObserver<ComparateurHotelRequest> hotelRequestObserver = hotelServiceStub.compare(
                                new StreamObserver<ComparateurResponse>() {
                                    @Override
                                    public void onNext(ComparateurResponse value) {
                                        synchronized (accumulatedOffres) {
                                            accumulatedOffres.addAll(value.getOffresList());
                                        }
                                        System.err.println("Offres ajoutées pour l'hôtel " + hotel.getId());
                                    }

                                    @Override
                                    public void onError(Throwable t) {
                                        System.err.println("Erreur pour l'hôtel " + hotel.getId() + ": " + t.getMessage());
                                        hotelLatch.countDown();
                                    }

                                    @Override
                                    public void onCompleted() {
                                        hotelLatch.countDown();
                                    }
                                });

                        // Envoyer la requête et la compléter
                        hotelRequestObserver.onNext(hotelRequest);
                        hotelRequestObserver.onCompleted();

                        // Attendre la réponse de l'hôtel avec un timeout
                        try {
                            if (!hotelLatch.await(5, TimeUnit.SECONDS)) {
                                System.err.println("Timeout lors de l'attente de la réponse de l'hôtel " + hotel.getId());
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.err.println("Interruption lors de l'attente de la réponse de l'hôtel " + hotel.getId());
                        }

                    } catch (Exception e) {
                        System.err.println("Erreur pour l'hôtel " + hotel.getId() + ": " + e.getMessage());
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Erreur lors de la comparaison: " + t.getMessage())
                        .asException());
                completionLatch.countDown();
            }

            @Override
            public void onCompleted() {
                try {
                    // Attendre un court instant pour s'assurer que toutes les réponses sont arrivées
                    Thread.sleep(100);

                    // Envoyer la réponse finale
                    ComparateurResponse.Builder responseBuilder = ComparateurResponse.newBuilder();
                    synchronized (accumulatedOffres) {
                        for (OffreComparateurDTO offre : accumulatedOffres) {
                            responseBuilder.addOffres(offre);
                        }
                    }
                    responseObserver.onNext(responseBuilder.build());
                    responseObserver.onCompleted();
                } catch (Exception e) {
                    responseObserver.onError(Status.INTERNAL
                            .withDescription("Erreur lors de l'envoi de la réponse finale: " + e.getMessage())
                            .asException());
                } finally {
                    completionLatch.countDown();
                }
            }
        };
    }


    @Override
    @Transactional
    public void getOffres(GetOffresRequest request, StreamObserver<GetOffresResponse> responseObserver) {
        try {
            // Créer un StreamObserver pour recevoir la réponse des hôtels
            StreamObserver<GetOffresHotelRequest> requestObserver = hotelServiceStub.getOffres(
                    new StreamObserver<GetOffresResponse>() {
                        // Liste pour accumuler les offres reçues
                        List<OffreDTO> accumulatedOffres = new ArrayList<>();

                        @Override
                        public void onNext(GetOffresResponse response) {
                            // Ajouter les offres reçues à la liste cumulée
                            accumulatedOffres.addAll(response.getOffresList());
                        }

                        @Override
                        public void onError(Throwable t) {
                            responseObserver.onError(Status.INTERNAL
                                    .withDescription("Erreur lors de la récupération des offres auprès des hôtels: " + t.getMessage())
                                    .asException());
                        }

                        @Override
                        public void onCompleted() {
                            // Envoyer la réponse finale avec les offres cumulées
                            GetOffresResponse.Builder responseBuilder = GetOffresResponse.newBuilder();
                            for (OffreDTO offre : accumulatedOffres) {
                                responseBuilder.addOffres(offre);
                            }
                            responseObserver.onNext(responseBuilder.build());
                            responseObserver.onCompleted();
                        }
                    });

            // Récupérer les hôtels partenaires de l'agence
//            List<Hotel> hotelsPartenaires = agenceRepository.findById(request.getIdAgence())
//                    .orElseThrow(() -> new RuntimeException("Agence non trouvée"))
//                    .getHotelsPartenaires();
            // filtrer la liste des partenaires par ville et par nombre d'etoiles
            List<Hotel> hotelsPartenaires = agenceRepository.findById(request.getIdAgence())
                    .orElseThrow(() -> new RuntimeException("Agence non trouvée"))
                    .getHotelsPartenaires()
                    .stream()
                    .filter(hotel -> hotel.getAdresse() != null
                            && hotel.getAdresse().getVille() != null
                            && hotel.getAdresse().getVille().equalsIgnoreCase(request.getVille())
                            && hotel.getNbEtoiles() >= request.getNbEtoiles())
                    .collect(Collectors.toList());



            // Envoi de la requête d'offres pour chaque hôtel partenaire
            for (Hotel hotel : hotelsPartenaires) {
                try {
                    GetOffresHotelRequest hotelRequest = GetOffresHotelRequest.newBuilder()
                            .setIdAgence(request.getIdAgence())
                            .setIdHotel(hotel.getId())
                            .setDateDebut(request.getDateDebut())
                            .setDateFin(request.getDateFin())
                            .setNbPersonnes(request.getNbPersonnes())
                            //.setVille(request.getVille())
                            //.setNbEtoiles(request.getNbEtoiles())
                            .build();

                    // Envoi de la requête d'offres à l'hôtel
                    requestObserver.onNext(hotelRequest);
                    System.err.println("Requête envoyée à l'hôtel ID: " + hotel.getId());
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'envoi de la requête d'offres à l'hôtel ID: "
                            + hotel.getId() + ", erreur: " + e.getMessage());
                }
            }

            // Indiquer la fin du stream
            requestObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Erreur interne du service: " + e.getMessage())
                    .asException());
        }
    }


    @Override
    @Transactional
    public void possible(PossibleRequest request, StreamObserver<PossibleResponse> responseObserver) {
        try {
            PossibleRequest possibleRequest = PossibleRequest.newBuilder()
                    .setIdOffre(request.getIdOffre())
                    .setDateDebut(request.getDateDebut())
                    .setDateFin(request.getDateFin())
                    .setNbPersonnes(request.getNbPersonnes())
                    .build();

            // Appeler le service hôtelier pour vérifier si c'est possible
            PossibleResponse possibleResponse = hotelServiceBlockingStub.possible(possibleRequest);

            // Si ce n'est pas possible
            if (!possibleResponse.getPossible()) {
                throw new RuntimeException("La réservation n'est pas possible");
            }

            // Si c'est possible, continuer
            PossibleResponse.Builder possibleResponseBuilder = PossibleResponse.newBuilder();
            possibleResponseBuilder.setPossible(true);
            responseObserver.onNext(possibleResponseBuilder.build());
            responseObserver.onCompleted();

        } catch (RuntimeException e) {
            // Gestion des erreurs
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            // Gestion des erreurs générales
            responseObserver.onError(Status.UNKNOWN
                    .withDescription("Une erreur inattendue s'est produite")
                    .asRuntimeException());
        }
    }



    @Override
    @Transactional
    public void reserver(ReservationRequest request, StreamObserver<ReservationResponse> responseObserver) {
        try {
            // Construire la requête pour le service hôtelier
            ReservationHotelRequest reservationHotelRequest = ReservationHotelRequest.newBuilder()
                    .setIdOffre(request.getIdOffre())
                    //.setIdHotel(request.getIdHotel())
                    .setIdAgence(request.getIdAgence())
                    .setNomClient(request.getNomClient())
                    .setPrenomClient(request.getPrenomClient())
                    .setEmailClient(request.getEmailClient())
                    .setDateDebut(request.getDateDebut())
                    .setDateFin(request.getDateFin())
                    .setNbPersonnes(request.getNbPersonnes())
                    .build();

            // Appeler le service hôtelier pour effectuer la réservation
            ReservationResponse hotelResponse = hotelServiceBlockingStub.reserver(reservationHotelRequest);

            // Vérifier si la réservation a été acceptée par l'hôtel
            if (!hotelResponse.getReserve()) {
                throw new RuntimeException("La réservation a été refusée par l'hôtel");
            }

            // Si la réservation est effectuée avec succès, enregistrer dans la base de données
            try {
                agenceService.ajouterClient(
                        request.getNomClient(),
                        request.getPrenomClient(),
                        request.getEmailClient(),
                        request.getCryptogramme(),
                        request.getNumeroCarte(),
                        request.getDateExpiration(),
                        request.getIdAgence()
                );

                agenceService.ajouterReservation(
                        String.valueOf(request.getIdOffre()),
                        //request.getIdHotel(),
                        hotelResponse.getIdHotel(),
                        request.getIdAgence(),
                        request.getDateDebut(),
                        request.getDateFin(),
                        request.getNbPersonnes(),
                        hotelResponse.getReservation().getPrix(),
                        hotelResponse.getReservation().getIdReservation()
                );
            } catch (Exception e) {
                // Si l'enregistrement local échoue
                throw new RuntimeException("Échec de l'enregistrement local : " + e.getMessage());
            }

            // Construire et envoyer la réponse finale en utilisant les données de la réservation de l'hôtel
            ReservationResponse response = ReservationResponse.newBuilder()
                    .setReserve(true)
                    .setReservation(hotelResponse.getReservation())  // Réutiliser directement le DTO de l'hôtel
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            System.err.println("Erreur lors de la réservation : " + e.getMessage());

            Status status = Status.INTERNAL
                    .withDescription("Échec de la réservation : " + e.getMessage());
            responseObserver.onError(status.asRuntimeException());
        }
    }
}