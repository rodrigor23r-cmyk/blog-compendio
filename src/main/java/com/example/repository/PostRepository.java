package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.entities.Post;

// @Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    // Aquí puedes agregar métodos de consulta personalizados si es necesario

    // Tu nuevo método personalizado para incrementar visitas de forma atómica
    @Modifying
    @Query("UPDATE Post p SET p.visitas = p.visitas + 1 WHERE p.id = :postId")
    void incrementarVisitas(@Param("postId") Long postId);
}
