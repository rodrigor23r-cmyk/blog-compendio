package com.example.exception;

// Hereda de RuntimeException para que no nos obligue a poner try/catch por todas partes
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}
