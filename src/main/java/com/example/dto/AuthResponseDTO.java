package com.example.dto;

public record AuthResponseDTO(
    String token,
    String tipo // Por defecto será "Bearer"
) {}
