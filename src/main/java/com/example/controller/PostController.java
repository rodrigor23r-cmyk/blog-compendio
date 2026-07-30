package com.example.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.PostRequestDTO;
import com.example.dto.PostResponseDTO;
import com.example.service.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<Page<PostResponseDTO>> listarPostsPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaCreacion") String sort,
            @RequestParam(defaultValue = "DESC") String dir,
            @RequestParam(required = false, name = "cat") Long categoriaId,
            @RequestParam(required = false, name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false, name = "owner") Boolean soloMisPosts) {
            
        Page<PostResponseDTO> posts = postService.obtenerPostsPaginados(
                page, size, sort, dir, categoriaId, fechaInicio, soloMisPosts);

        return ResponseEntity.ok(posts);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> crearPost(
        @Valid @RequestPart("post") PostRequestDTO requestDTO,
        @RequestPart(value = "imagen", required = false) MultipartFile imagen
        ) {
        PostResponseDTO nuevoPost = postService.crearPost(requestDTO, imagen);
        return new ResponseEntity<>(nuevoPost, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(postService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDTO> actualizarPost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequestDTO requestDTO) {
        return ResponseEntity.ok(postService.actualizarPost(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPost(@PathVariable Long id) {
        postService.eliminarPost(id);
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content
    }
}
