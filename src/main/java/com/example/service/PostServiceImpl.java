package com.example.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.PostRequestDTO;
import com.example.dto.PostResponseDTO;
import com.example.entities.Post;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.PostMapper;
import com.example.repository.PostRepository;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PostResponseDTO> obtenerTodos() {
        return postRepository.findAll().stream()
                .map(postMapper::toPostResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PostResponseDTO crearPost(PostRequestDTO requestDTO) {
        // 1. Convertimos el Record DTO a Entidad usando MapStruct
        Post nuevoPost = postMapper.toPostEntity(requestDTO);
        
        // 2. Guardamos en MySQL (JPA ejecutará el INSERT)
        Post postGuardado = postRepository.save(nuevoPost);
        
        // 3. Convertimos la Entidad guardada de nuevo a Record DTO para devolverla
        return postMapper.toPostResponseDTO(postGuardado);
    }

    // Obtener un post por su ID
@Override
@Transactional(readOnly = true)
public PostResponseDTO obtenerPorId(Long id) {
    Post post = postRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado con id: " + id));
    return postMapper.toPostResponseDTO(post);
}

// Actualizar un post existente
@Override
@Transactional
public PostResponseDTO actualizarPost(Long id, PostRequestDTO requestDTO) {
    Post postExistente = postRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado con id: " + id));

    // Actualizamos los campos manualmente o a través del mapper
    postExistente.setTitulo(requestDTO.titulo());
    postExistente.setCuerpo(requestDTO.cuerpo());
    postExistente.setFotoUrl(requestDTO.fotoUrl());
    
    // MapStruct también puede mapear sobre una entidad existente si creas un método de actualización,
    // pero al ser pocos campos asignarlos así es seguro y transparente.

    Post postActualizado = postRepository.save(postExistente);
    return postMapper.toPostResponseDTO(postActualizado);
}

// Eliminar un post
@Override
@Transactional
public void eliminarPost(Long id) {
    if (!postRepository.existsById(id)) {
        throw new ResourceNotFoundException("Post no encontrado con id: " + id);
    }
    postRepository.deleteById(id);
}
}
