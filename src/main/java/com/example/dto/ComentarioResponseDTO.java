package com.example.dto;

import java.time.LocalDateTime;

public record ComentarioResponseDTO(
    Long id,
    String texto,
    LocalDateTime fechaCreacion,
    UsuarioResponseDTO autor // Usamos el DTO seguro del usuario
) {}