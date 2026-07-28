package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ComentarioRequestDTO(
    @NotBlank(message = "El comentario no puede estar vacío")
    String texto,

    @NotNull(message = "El ID del autor es obligatorio")
    Long autorId
) {}
