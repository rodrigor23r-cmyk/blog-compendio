package com.example.service;

import java.util.List;

import com.example.dto.PostRequestDTO;
import com.example.dto.PostResponseDTO;

import jakarta.validation.Valid;

public interface PostService {

    List<PostResponseDTO> obtenerTodos();

    PostResponseDTO crearPost(PostRequestDTO requestDTO);

    void eliminarPost(Long id);

    PostResponseDTO obtenerPorId(Long id);

    PostResponseDTO actualizarPost(Long id, @Valid PostRequestDTO requestDTO);

}
