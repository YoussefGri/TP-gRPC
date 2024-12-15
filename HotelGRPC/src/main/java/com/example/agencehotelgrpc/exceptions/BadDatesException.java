package com.example.agencehotelgrpc.exceptions;

public class BadDatesException extends RuntimeException {
    public BadDatesException(String message) {
        super(message);
    }
}
