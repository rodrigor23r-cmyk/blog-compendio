package com.example.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.CategoriaRequestDTO;
import com.example.dto.CategoriaResponseDTO;
import com.example.entities.Categoria;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.CategoriaMapper;
import com.example.repository.CategoriaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> obtenerTodas() {
        return categoriaRepository.findAll().stream()
                .map(categoriaMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public CategoriaResponseDTO crearCategoria(CategoriaRequestDTO requestDTO) {
        if (categoriaRepository.existsByNombreIgnoreCase(requestDTO.nombre())) {
            // Podrías crear una excepción personalizada como DuplicateResourceException
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + requestDTO.nombre());
        }

        Categoria categoria = categoriaMapper.toEntity(requestDTO);
        return categoriaMapper.toResponseDTO(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public CategoriaResponseDTO actualizarCategoria(Long id, CategoriaRequestDTO requestDTO) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        // Si cambia el nombre, verificamos que el nuevo no exista ya
        if (!categoria.getNombre().equalsIgnoreCase(requestDTO.nombre()) && 
            categoriaRepository.existsByNombreIgnoreCase(requestDTO.nombre())) {
            throw new IllegalArgumentException("Ya existe otra categoría con el nombre: " + requestDTO.nombre());
        }

        categoria.setNombre(requestDTO.nombre());
        return categoriaMapper.toResponseDTO(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public void eliminarCategoria(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría no encontrada con id: " + id);
        }
        
        // NOTA DE ARQUITECTURA: Al ser una relación N:M, si borras una categoría que está 
        // siendo usada por un Post, MySQL podría lanzar un error de integridad referencial.
        // Lo ideal aquí sería limpiar primero la tabla intermedia, pero para empezar así está perfecto.
        categoriaRepository.deleteById(id);
    }
}
