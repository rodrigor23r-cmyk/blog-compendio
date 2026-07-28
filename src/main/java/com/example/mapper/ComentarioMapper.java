package com.example.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.dto.ComentarioRequestDTO;
import com.example.dto.ComentarioResponseDTO;
import com.example.entities.Comentario;
import com.example.entities.Usuario;

@Mapper(componentModel = "spring")
public interface ComentarioMapper {

    ComentarioResponseDTO toResponseDTO(Comentario comentario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "post", ignore = true) // Lo asignamos en el Service
    @Mapping(target = "autor", source = "autorId")
    Comentario toComentarioEntity(ComentarioRequestDTO requestDTO);

    
    /**Creando un "usuario cascarón"
     * (un objeto totalmente vacío donde solo el atributo id vale 1). */

    default Usuario mapIdToUsuario(Long id) {
        if (id == null) return null;
        return Usuario.builder().id(id).build();
    }
}
