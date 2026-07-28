package com.example.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.entities.Usuario;
import com.example.repository.UsuarioRepository;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // 1. Buscamos a tu usuario real en la base de datos
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // 2. Convertimos tu rol (ej. "ROLE_ADMIN" o "ROLE_USER") al formato de Spring Security
        // Nota: Spring Security espera por convención que los roles empiecen por "ROLE_"
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());

        // 3. Construimos y devolvemos el objeto UserDetails interno de Spring
        return new User(
                usuario.getUsername(),
                usuario.getPassword(), // Asegúrate de que tu entidad Usuario tiene el atributo password
                Collections.singletonList(authority) // Lista de permisos/roles
        );
    }
}
