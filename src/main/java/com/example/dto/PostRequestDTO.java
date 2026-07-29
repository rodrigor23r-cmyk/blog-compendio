package com.example.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostRequestDTO(
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 150, message = "El título debe tener entre 3 y 150 caracteres")
    String titulo,
    
    @NotBlank(message = "El cuerpo del post no puede estar vacío")
    String cuerpo,
    
    String fotoUrl,

    @NotNull(message = "El atributo de visibilidad es obligatorio")
    Boolean esPublico,
    
    Set<Long> categoriaIds
) {}