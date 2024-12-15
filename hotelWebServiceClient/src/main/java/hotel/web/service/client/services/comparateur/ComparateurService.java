package hotel.web.service.client.services.comparateur;

import com.example.agenthotelgrpc.proto.ComparateurResponse;
import com.example.agenthotelgrpc.proto.ComparateurServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class ComparateurService {
    @GrpcClient("comparateur-service")
    private ComparateurServiceGrpc.ComparateurServiceBlockingStub comparateurServiceBlockingStub;

    public ComparateurResponse compare(com.example.agenthotelgrpc.proto.ComparateurRequest request){
        return comparateurServiceBlockingStub.compare(request);
    }

}
