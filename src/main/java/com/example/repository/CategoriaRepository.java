package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entities.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    // Nos permite comprobar rápido si ya hay un tag con ese nombre
    boolean existsByNombreIgnoreCase(String nombre);
}
