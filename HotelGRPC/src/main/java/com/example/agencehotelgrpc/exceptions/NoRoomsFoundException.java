package com.example.agencehotelgrpc.exceptions;

public class NoRoomsFoundException extends RuntimeException {
    public NoRoomsFoundException(String message) {
        super(message);
    }
}
