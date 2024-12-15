package com.example.agencehotelgrpc.services;

import com.example.agencehotelgrpc.models.Agence;
import com.example.agencehotelgrpc.repositories.AgenceRepository;
import com.example.agenthotelgrpc.proto.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;

@GrpcService
public class ComparateurServiceImpl extends ComparateurServiceGrpc.ComparateurServiceImplBase  {

    private final AgenceRepository agenceRepository;

    @GrpcClient("agence-service")
    private AgenceServiceGrpc.AgenceServiceStub agenceServiceStub;

    public ComparateurServiceImpl(AgenceRepository agenceRepository) {
        this.agenceRepository = agenceRepository;
    }

    @Override
    @Transactional
    public void compare(ComparateurRequest request, StreamObserver<ComparateurResponse> responseObserver) {
        try {
            StreamObserver<ComparateurAgenceRequest> requestObserver = agenceServiceStub.compare(
                    new StreamObserver<ComparateurResponse>() {
                        @Override
                        public void onNext(ComparateurResponse value) {
                            responseObserver.onNext(value);
                        }

                        @Override
                        public void onError(Throwable t) {
                            responseObserver.onError(t);
                        }

                        @Override
                        public void onCompleted() {
                            responseObserver.onCompleted();
                        }
                    });

            // Envoyer les requêtes aux agences
            for (Agence agence : agenceRepository.findAll()) {
                ComparateurAgenceRequest agenceRequest = ComparateurAgenceRequest.newBuilder()
                        .setIdAgence(agence.getId())
                        .setDateDebut(request.getDateDebut())
                        .setDateFin(request.getDateFin())
                        .setVille(request.getVille())
                        .setNbPersonnes(request.getNbPersonnes())
                        .setNbEtoiles(request.getNbEtoiles())
                        .build();

                requestObserver.onNext(agenceRequest);
            }

            // Important : signaler la fin des requêtes
            requestObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Erreur : " + e.getMessage())
                    .asException());
        }
    }


}
