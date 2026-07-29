package com.example.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.dto.CategoriaRequestDTO;
import com.example.dto.CategoriaResponseDTO;
import com.example.entities.Categoria;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    CategoriaResponseDTO toResponseDTO(Categoria categoria);

    @Mapping(target = "id", ignore = true)
    Categoria toEntity(CategoriaRequestDTO requestDTO);
}
            