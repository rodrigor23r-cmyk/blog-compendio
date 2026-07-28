package com.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entities.Usuario;

/**
 * UsuarioRepository
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Método clave para la autenticación
    Optional<Usuario> findByUsername(String username);
    
    // También es buena idea tener este para el registro de nuevos usuarios
    boolean existsByUsername(String username);

    
    
    
    
    // Aquí puedes agregar métodos de consulta personalizados si es necesario

}
