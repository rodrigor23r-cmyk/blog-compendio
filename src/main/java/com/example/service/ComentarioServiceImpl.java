package com.example.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.ComentarioRequestDTO;
import com.example.dto.ComentarioResponseDTO;
import com.example.entities.Comentario;
import com.example.entities.Post;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.ComentarioMapper;
import com.example.repository.ComentarioRepository;
import com.example.repository.PostRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComentarioServiceImpl implements ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final PostRepository postRepository;
    private final ComentarioMapper comentarioMapper;

    @Override
    @Transactional
    public ComentarioResponseDTO crearComentario(Long postId, ComentarioRequestDTO requestDTO) {
        // 1. Verificamos que el post exista
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado con id: " + postId));

        // 2. Mapeamos el DTO a Entidad
        Comentario comentario = comentarioMapper.toComentarioEntity(requestDTO);
        
        // 3. Asignamos el post
        comentario.setPost(post);

        // 4. Guardamos y devolvemos
        Comentario guardado = comentarioRepository.save(comentario);
        return comentarioMapper.toResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> obtenerComentariosDePost(Long postId) {
        // Verificamos que el post exista antes de buscar sus comentarios
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post no encontrado con id: " + postId);
        }

        return comentarioRepository.findByPostId(postId).stream()
                .map(comentarioMapper::toResponseDTO)
                .toList(); // .toList() es la versión moderna y recomendada frente a Collectors.toList()
    }
}
