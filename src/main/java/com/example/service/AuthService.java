package com.example.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.RegistroRequestDTO;
import com.example.entities.Rol;
import com.example.entities.Usuario;
import com.example.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registrarUsuario(RegistroRequestDTO request) {
        // 1. Verificamos que el usuario no exista ya
        if (usuarioRepository.existsByUsername(request.username())) {
            // Reutilizamos la misma excepción de negocio que usamos en las categorías
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }

        // 2. Creamos el usuario encriptando su contraseña y asignando rol por defecto
        Usuario nuevoUsuario = Usuario.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .rol(Rol.USER) // A los usuarios nuevos les damos el rol básico
                .build();

        // 3. Guardamos en base de datos
        usuarioRepository.save(nuevoUsuario);
    }
}
