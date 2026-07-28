package com.example.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
    int estado,             // Ej: 400, 404
    String mensaje,         // Ej: "Error de validación" o "Post no encontrado"
    LocalDateTime fecha,    // Cuándo ocurrió
    Map<String, String> detalles // Aquí irán los campos que fallaron y por qué
) {}
