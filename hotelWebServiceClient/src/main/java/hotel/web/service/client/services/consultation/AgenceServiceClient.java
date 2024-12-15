package hotel.web.service.client.services.consultation;

import com.example.agenthotelgrpc.proto.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class AgenceServiceClient {

    @GrpcClient("agence-service")
    private AgenceServiceGrpc.AgenceServiceBlockingStub agenceServiceBlockingStub;

    public AuthentifierResponse authentification(com.example.agenthotelgrpc.proto.AuthentifierRequest request) {
        return agenceServiceBlockingStub.authentifier(request);
    }

    public ReservationResponse reserver(com.example.agenthotelgrpc.proto.ReservationRequest request) {
        return agenceServiceBlockingStub.reserver(request);
    }

    public GetOffresResponse getOffres(com.example.agenthotelgrpc.proto.GetOffresRequest request) {
        return agenceServiceBlockingStub.getOffres(request);
    }
}
