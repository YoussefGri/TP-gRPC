package com.example.agencehotelgrpc.service;


import com.example.agencehotelgrpc.dto.OffreDTO;
import com.example.agencehotelgrpc.dto.OffreComparateurDTO;
import com.example.agencehotelgrpc.models.Agence;
import com.example.agencehotelgrpc.models.Hotel;
import com.example.agencehotelgrpc.models.Offre;
import com.example.agencehotelgrpc.repositories.AgenceRepository;
import com.example.agencehotelgrpc.repositories.HotelRepository;
import com.example.agencehotelgrpc.repositories.OffreRepository;
import com.example.agencehotelgrpc.servicesUtil.ConsultationDisponibilitesService;
import com.example.agencehotelgrpc.servicesUtil.ReservationService;
import com.example.agenthotelgrpc.proto.*;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.agencehotelgrpc.util.Converters.loadImageBytes;
import static com.example.agencehotelgrpc.util.Converters.stringToDate;


@GrpcService
public class HotelServiceImpl extends HotelServiceGrpc.HotelServiceImplBase {

    private final HotelRepository hotelRepository;
    private final AgenceRepository agenceRepository;
    private final ConsultationDisponibilitesService consultationDisponibilitesService;
    private final ReservationService reservationService;
    private final OffreRepository offreRepository;

    public HotelServiceImpl(HotelRepository hotelRepository,
                            ConsultationDisponibilitesService consultationDisponibilitesService,
                            AgenceRepository agenceRepository,
                            ReservationService reservationService, OffreRepository offreRepository) {
        this.hotelRepository = hotelRepository;
        this.consultationDisponibilitesService = consultationDisponibilitesService;
        this.agenceRepository = agenceRepository;
        this.reservationService = reservationService;
        this.offreRepository = offreRepository;
    }

    @Override
    @Transactional
    public void possible(PossibleRequest request, StreamObserver<PossibleResponse> responseObserver) {
        try {
            int idOffre = request.getIdOffre();
            String dateDebut = request.getDateDebut();
            String dateFin = request.getDateFin();
            int nbPersonnes = request.getNbPersonnes();

            // Vérification de la possibilité de réservation
            boolean ispossible = reservationService.isReservationPossible(idOffre, nbPersonnes, dateDebut, dateFin);

            // Création de la réponse
            PossibleResponse responseBuilder = PossibleResponse.newBuilder()
                    .setPossible(ispossible)
                    .build();

            // Envoi de la réponse
            responseObserver.onNext(responseBuilder);
            responseObserver.onCompleted();

        } catch (RuntimeException e) {
            // Gestion des exceptions spécifiques
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            // Gestion des erreurs imprévues
            responseObserver.onError(Status.UNKNOWN
                    .withDescription("Une erreur inattendue s'est produite")
                    .asRuntimeException());
        }
    }


    @Override
    @Transactional
    public void reserver(ReservationHotelRequest request, StreamObserver<ReservationResponse> responseObserver) {
        try {
            // Effectuer la réservation via le service
            Offre offre = offreRepository.findById(request.getIdOffre())
                    .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
            int idHotel = offre.getHotel().getId();

            com.example.agencehotelgrpc.dto.ReservationDTO reservationDTO = reservationService.effectuerReservation(
                    request.getIdOffre(),
                    //request.getIdHotel(),
                    idHotel,
                    request.getIdAgence(),
                    request.getNomClient(),
                    request.getPrenomClient(),
                    request.getEmailClient(),
                    request.getDateDebut(),
                    request.getDateFin(),
                    request.getNbPersonnes()
            );

            // Convertir le DTO Java en DTO Proto
            com.example.agenthotelgrpc.proto.ReservationDTO protoDTO = com.example.agenthotelgrpc.proto.ReservationDTO.newBuilder()
                    .setIdReservation(reservationDTO.getIdReservation())
                    .setNomHotel(reservationDTO.getNomHotel())
                    .setVille(reservationDTO.getVille())
                    .setDateDebut(reservationDTO.getDateDebut())
                    .setDateFin(reservationDTO.getDateFin())
                    .setNomClient(reservationDTO.getNomClient())
                    .setPrenomClient(reservationDTO.getPrenomClient())
                    .setEmailClient(reservationDTO.getEmailClient())
                    .setPrix(reservationDTO.getPrix())
                    .build();

            // Construire la réponse
            ReservationResponse response = ReservationResponse.newBuilder()
                    .setReserve(true)
                    .setIdHotel(idHotel)
                    .setReservation(protoDTO)
                    .build();

            // Envoyer la réponse au client
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            System.err.println("Erreur dans HotelServiceImpl reserver: " + e.getMessage());
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Une erreur s'est produite lors du traitement de la réservation : " + e.getMessage())
                    .asRuntimeException());
        }
    }


    @Transactional
    @Override
    public StreamObserver<GetOffresHotelRequest> getOffres(StreamObserver<GetOffresResponse> responseObserver) {

        // Liste pour accumuler les offres récupérées
        List<com.example.agencehotelgrpc.dto.OffreDTO> accumulatedOffres = new ArrayList<>();

        return new StreamObserver<GetOffresHotelRequest>() {
            @Override
            @Transactional
            public void onNext(GetOffresHotelRequest request) {
                try {
                    // Récupérer les paramètres de la requête
                    String dateDebut = request.getDateDebut();
                    String dateFin = request.getDateFin();
                    int nbPersonnes = request.getNbPersonnes();
                    int idAgence = request.getIdAgence();
                    int idHotel = request.getIdHotel();

                    // Récupérer les offres disponibles
                    List<com.example.agencehotelgrpc.dto.OffreDTO> offresDTO = consultationDisponibilitesService
                            .getOffresDetails(idHotel, idAgence, dateDebut, dateFin, nbPersonnes);

                    // Ajouter les offres récupérées à la liste cumulée
                    if (offresDTO != null && !offresDTO.isEmpty()) {
                        accumulatedOffres.addAll(offresDTO);
                    } else {
                        System.out.println("Aucune offre trouvée pour l'hôtel ID: " + idHotel);
                    }
                } catch (Exception e) {
                    // Si une erreur survient pour un hôtel, on l'ignore et on passe au suivant
                    System.err.println("Erreur dans HotelServiceImpl getOffres pour l'hôtel ID "
                            + request.getIdHotel() + ": " + e.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Erreur reçue du client : " + t.getMessage());
                responseObserver.onError(t); // Propager l'erreur au client si nécessaire
            }

            @Override
            public void onCompleted() {
                // Construire la réponse à partir des offres cumulées
                GetOffresResponse.Builder responseBuilder = GetOffresResponse.newBuilder();
                for (com.example.agencehotelgrpc.dto.OffreDTO offre : accumulatedOffres) {
                    responseBuilder.addOffres(convertToProtoOffreDTO(offre));
                }

                // Envoyer la réponse finale au client
                responseObserver.onNext(responseBuilder.build());
                responseObserver.onCompleted(); // Signaler la fin au client
            }

            private com.example.agenthotelgrpc.proto.OffreDTO convertToProtoOffreDTO(com.example.agencehotelgrpc.dto.OffreDTO offreDTO) {

                String imagePath = "src/main/resources/static/images/chambre_type_" + offreDTO.getNbPersonnes() + ".jpg";

                byte[] imageBytes = loadImageBytes(imagePath);

                return com.example.agenthotelgrpc.proto.OffreDTO.newBuilder()
                        .setIdOffre(offreDTO.getIdOffre())
                        .setIdHotel(offreDTO.getIdHotel())
                        .setNomHotel(offreDTO.getNomHotel())
                        .setVille(offreDTO.getVille())
                        .setDateDebut(offreDTO.getDateDebutOffre())
                        .setDateFin(offreDTO.getDateFinOffre())
                        .setPrix(offreDTO.getPrix())
                        .setNbPersonnes(offreDTO.getNbPersonnes())
                        .setPourcentageReduction(offreDTO.getPourcentageReduction())
                        .setNbEtoiles(offreDTO.getNbEtoiles())
                        .setImage(ByteString.copyFrom(imageBytes))
                        .build();
            }
        };
    }

    @Override
    @Transactional
    public StreamObserver<ComparateurHotelRequest> compare(StreamObserver<ComparateurResponse> responseObserver) {
        List<OffreComparateurDTO> offresCumulees = new ArrayList<>();

        return new StreamObserver<ComparateurHotelRequest>() {
            @Override
            @Transactional
            public void onNext(ComparateurHotelRequest comparateurHotelRequest) {
                try {
                    System.err.println("Requête de comparaison reçue");

                    int idAgence = comparateurHotelRequest.getIdAgence();
                    int idHotel = comparateurHotelRequest.getIdHotel();
                    int nbPersonnes = comparateurHotelRequest.getNbPersonnes();

                    String dateDebut = comparateurHotelRequest.getDateDebut();
                    String dateFin = comparateurHotelRequest.getDateFin();


                    // Récupération des informations de l'agence et de l'hôtel
                    Agence agence = agenceRepository.findById(idAgence)
                            .orElseThrow(() -> new RuntimeException("Agence non trouvée"));
                    Hotel hotel = hotelRepository.findById(idHotel)
                            .orElseThrow(() -> new RuntimeException("Hotel non trouvé"));

                    String nomAgence = agence.getNom();
                    String nomHotel = hotel.getNom();
                    String adresseHotel = hotel.getAdresse().toString();
                    String adresseAgence = agence.getAdresse().toString();
                    int nbEtoiles = hotel.getNbEtoiles();

                    Date dateDebutDate = stringToDate(dateDebut);
                    Date dateFinDate = stringToDate(dateFin);

                    // Récupération des offres
                    List<OffreDTO> offres = consultationDisponibilitesService.getOffresDetails(
                            idAgence, idHotel, dateDebut, dateFin, nbPersonnes
                    );

                    System.err.println("Offres récupérées pour l'hôtel " + idHotel + " : " + offres.size());

                    // Traitement des offres
                    for (OffreDTO offre : offres) {
                        int nbLitsDisponibles = consultationDisponibilitesService.calculateTotalAvailableBeds(
                                offre.getIdHotel(),
                                offre.getNbPersonnes(),
                                dateDebutDate,
                                dateFinDate
                        );

                        OffreComparateurDTO offreComparateurDTO = new OffreComparateurDTO(
                                nomHotel,
                                nomAgence,
                                adresseAgence,
                                adresseHotel,
                                nbEtoiles,
                                nbLitsDisponibles,
                                offre.getPrix(),
                                offre.getPourcentageReduction()
                        );

                        offresCumulees.add(offreComparateurDTO);
                    }

                    // Envoi de la réponse pour cette requête
                    ComparateurResponse response = ComparateurResponse.newBuilder()
                            .addAllOffres(offresCumulees.stream()
                                    .map(this::convertToProto)
                                    .collect(Collectors.toList()))
                            .build();

                    System.err.println("Envoi de la réponse pour l'hôtel " + idHotel + " : " + response.getOffresList().size());

                    responseObserver.onNext(response);

                } catch (Exception e) {
                    System.err.println("Erreur dans HotelServiceImpl compare: " + e.getMessage());
                    responseObserver.onError(io.grpc.Status.INTERNAL
                            .withDescription("Une erreur s'est produite lors de la récupération des offres : " + e.getMessage())
                            .asRuntimeException());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Erreur de streaming: " + throwable.getMessage());
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                // Envoi de la réponse finale si nécessaire
                responseObserver.onCompleted();
            }

            // Méthode utilitaire pour convertir OffreComparateurDTO en message Proto
            private com.example.agenthotelgrpc.proto.OffreComparateurDTO convertToProto(OffreComparateurDTO offre) {
                return com.example.agenthotelgrpc.proto.OffreComparateurDTO.newBuilder()
                        .setNomHotel(offre.getNomHotel())
                        .setNomAgence(offre.getNomAgence())
                        .setAdresseAgence(offre.getAdresseAgence())
                        .setAdresseHotel(offre.getAdresseHotel())
                        .setNbEtoiles(offre.getNbEtoiles())
                        .setNbLitsDisponibles(offre.getNbLitsDisponibles())
                        .setPrix(offre.getPrix())
                        .setPourcentageReduction(offre.getPourcentageReduction())
                        .build();
            }

        };
    }



    @Override
    public StreamObserver<AuthentifierHotelRequest> authentifier(
            StreamObserver<AuthentifierResponse> responseObserver) {

        return new StreamObserver<AuthentifierHotelRequest>() {
            private boolean authenticated = false;

            @Override
            public void onNext(AuthentifierHotelRequest request) { // reception requetes client
                // Vérifier les credentials
                boolean correctCredentials = consultationDisponibilitesService.grantAccess(
                        request.getIdHotel(),
                        request.getIdAgence(),
                        request.getPassword()
                );

                // Si on trouve une authentification valide, on la mémorise
                if (correctCredentials) {
                    authenticated = true;
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() { // le client me dit qu'il a fini d'envoyer les requetes
                // Envoyer la réponse finale une fois toutes les requêtes reçues
                AuthentifierResponse response = AuthentifierResponse.newBuilder()
                        .setAuthentifie(authenticated)
                        .build();
                System.out.println("AuthentifierResponse: " + response.getAuthentifie());
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }


}