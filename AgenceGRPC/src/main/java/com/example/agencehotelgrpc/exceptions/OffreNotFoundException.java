package com.example.agencehotelgrpc.exceptions;

public class OffreNotFoundException extends RuntimeException {
    public OffreNotFoundException(String message) {
        super(message);
    }
}
