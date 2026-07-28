package com.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.dto.AuthResponseDTO;
import com.example.dto.LoginRequestDTO;
import com.example.dto.RegistroRequestDTO;
import com.example.security.JwtUtils;
import com.example.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        
        // 1. Validamos las credenciales con la base de datos (Spring Security lo hace por debajo)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );

        // 2. Si es correcto, lo guardamos en el contexto
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generamos el token JWT
        String token = jwtUtils.generarToken(authentication);

        // 4. Devolvemos el token al cliente
        return ResponseEntity.ok(new AuthResponseDTO(token, "Bearer"));
    }

    @PostMapping("/registro")
    public ResponseEntity<Map<String, String>> registrar(@Valid @RequestBody RegistroRequestDTO registroRequest) {
        // Llamamos al servicio para crear y encriptar al usuario
        authService.registrarUsuario(registroRequest);
        
        // Devolvemos un 201 CREATED con un mensaje de confirmación JSON
        return new ResponseEntity<>(Map.of("mensaje", "Usuario registrado correctamente"), HttpStatus.CREATED);
    }
}
