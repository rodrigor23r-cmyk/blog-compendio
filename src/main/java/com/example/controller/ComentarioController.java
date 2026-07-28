package com.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.dto.ComentarioRequestDTO;
import com.example.dto.ComentarioResponseDTO;
import com.example.service.ComentarioService;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @PostMapping
    public ResponseEntity<ComentarioResponseDTO> crearComentario(
            @PathVariable Long postId,
            @Valid @RequestBody ComentarioRequestDTO requestDTO) {
        
        ComentarioResponseDTO nuevoComentario = comentarioService.crearComentario(postId, requestDTO);
        return new ResponseEntity<>(nuevoComentario, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ComentarioResponseDTO>> listarComentariosPorPost(@PathVariable Long postId) {
        return ResponseEntity.ok(comentarioService.obtenerComentariosDePost(postId));
    }
}