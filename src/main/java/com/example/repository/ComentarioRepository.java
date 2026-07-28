package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entities.Comentario;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    
    // Spring genera la consulta: SELECT * FROM comentarios WHERE post_id = ?
    List<Comentario> findByPostId(Long postId);
}
