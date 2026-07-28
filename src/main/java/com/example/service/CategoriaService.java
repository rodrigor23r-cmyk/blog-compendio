package com.example.service;

import java.util.List;

import com.example.dto.CategoriaRequestDTO;
import com.example.dto.CategoriaResponseDTO;

/**
 * CategoriaService
 */
public interface CategoriaService {

    List<CategoriaResponseDTO> obtenerTodas();

    CategoriaResponseDTO crearCategoria(CategoriaRequestDTO requestDTO);

    CategoriaResponseDTO actualizarCategoria(Long id, CategoriaRequestDTO requestDTO);

    void eliminarCategoria(Long id);

}
