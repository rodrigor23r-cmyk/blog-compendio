package com.example.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.PostRequestDTO;
import com.example.dto.PostResponseDTO;
import com.example.entities.Categoria;
import com.example.entities.Post;
import com.example.entities.Usuario;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.PostMapper;
import com.example.repository.CategoriaRepository;
import com.example.repository.PostRepository;
import com.example.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PostResponseDTO> obtenerTodos() {
        return postRepository.findAll().stream()
                .map(postMapper::toPostResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PostResponseDTO crearPost(PostRequestDTO requestDTO) {

        // 1. Buscamos el Autor REAL en la BD (para que traiga username, rol, etc.)
        Usuario autor = usuarioRepository.findById(requestDTO.autorId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Usuario no encontrado con id: " + requestDTO.autorId()));

        // 2. Buscamos las Categorías REALES en la BD
        Set<Categoria> categorias = new HashSet<>();
        if (requestDTO.categoriaIds() != null && !requestDTO.categoriaIds().isEmpty()) {
            categorias = new HashSet<>(categoriaRepository.findAllById(requestDTO.categoriaIds()));
        }

        // 3. Convertimos el Record DTO a Entidad usando MapStruct
        Post nuevoPost = postMapper.toPostEntity(requestDTO);

        // 4. Le asignamos los objetos completos recargados de la BD
        nuevoPost.setAutor(autor);
        nuevoPost.setCategorias(categorias);

        // 5. Guardamos en MySQL (JPA ejecutará el INSERT)
        Post postGuardado = postRepository.save(nuevoPost);

        // 6. Convertimos la Entidad guardada de nuevo a Record DTO para devolverla
        return postMapper.toPostResponseDTO(postGuardado);
    }

    // Obtener un post por su ID
    @Override
    @Transactional(readOnly = true)
    public PostResponseDTO obtenerPorId(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado con id: " + id));
        return postMapper.toPostResponseDTO(post);
    }

    // Actualizar un post existente
    @Override
    @Transactional
    public PostResponseDTO actualizarPost(Long id, PostRequestDTO requestDTO) {
        Post postExistente = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado con id: " + id));

        // Actualizamos los campos manualmente o a través del mapper
        postExistente.setTitulo(requestDTO.titulo());
        postExistente.setCuerpo(requestDTO.cuerpo());
        postExistente.setFotoUrl(requestDTO.fotoUrl());

        // MapStruct también puede mapear sobre una entidad existente si creas un método
        // de actualización,
        // pero al ser pocos campos asignarlos así es seguro y transparente.

        Post postActualizado = postRepository.save(postExistente);
        return postMapper.toPostResponseDTO(postActualizado);
    }

    // Eliminar un post
    @Override
    @Transactional
    public void eliminarPost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post no encontrado con id: " + id);
        }
        postRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponseDTO> obtenerPostsPaginados(int numeroPagina, int tamanoPagina, String ordenarPor,
            String direccion) {

        // 1. Configuramos la dirección del ordenamiento (ASC o DESC)
        Sort sort = direccion.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(ordenarPor).ascending()
                : Sort.by(ordenarPor).descending();

        // 2. Creamos el objeto Pageable (Atención: Spring usa índice 0 para la primera
        // página)
        Pageable pageable = PageRequest.of(numeroPagina, tamanoPagina, sort);

        // 3. Buscamos en la base de datos
        Page<Post> postsPaginados = postRepository.findAll(pageable);

        // 4. Mapeamos el Page<Post> a Page<PostResponseDTO>
        return postsPaginados.map(postMapper::toPostResponseDTO);
    }
}
