package com.example.service;

import java.util.List;

import com.example.dto.ComentarioRequestDTO;
import com.example.dto.ComentarioResponseDTO;

public interface ComentarioService {

    ComentarioResponseDTO crearComentario(Long postId, ComentarioRequestDTO requestDTO);

    
    List<ComentarioResponseDTO> obtenerComentariosDePost(Long postId);

}
