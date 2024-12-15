package com.example.agencehotelgrpc.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EntityScan(basePackages = {
        "com.example.agencehotelgrpc.models"
})
@EnableJpaRepositories(basePackages = {
        "com.example.agencehotelgrpc.repositories"
})
@SpringBootApplication(scanBasePackages = {
        "com.example.agencehotelgrpc",
        "com.example.agencehotelgrpc.exceptions",
        "com.example.agencehotelgrpc.service",
})
public class AgenceGrpcApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgenceGrpcApplication.class, args);
    }

}
