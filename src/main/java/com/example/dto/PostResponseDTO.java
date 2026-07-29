package com.example.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record PostResponseDTO(
    Long id,
    String titulo,
    String cuerpo,
    String fotoUrl,
    LocalDateTime fechaCreacion,
    UsuarioResponseDTO autor,
    Boolean esPublico,
    Set<String> categorias
) {}
