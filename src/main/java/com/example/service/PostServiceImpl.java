package com.example.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.example.repository.specification.PostSpecification;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    private void verificarAutorOAdmin(Post post) {
       // 1. Obtenemos el "username" del usuario que está haciendo la petición (el dueño del Token)
        String usuarioAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 2. Obtenemos los roles del usuario autenticado
        boolean esAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
            .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // 3. LA MAGIA: Comparamos. Si no es el autor Y TAMPOCO es administrador... ¡Bloqueo!
        // (Asumiendo que tu entidad Post tiene una relación con Usuario llamada "autor")
        if (!post.getAutor().getUsername().equals(usuarioAutenticado) && !esAdmin) {
            throw new AccessDeniedException("No tienes permiso para editar un post que no es tuyo");
        }
    }

    // Este método no tiene llamada desde el Controller, pero lo dejamos para futuras implementaciones
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

        // 1. Sacamos quién es el usuario que está haciendo la petición con su token
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario autor = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

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
        if (requestDTO.esPublico() != null) {
            nuevoPost.setEsPublico(requestDTO.esPublico());
        }

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

        // ¡EL CERROJO DE SEGURIDAD!
        verificarAutorOAdmin(post);

        return postMapper.toPostResponseDTO(post);
    }

    // Actualizar un post existente
    @Override
    @Transactional
    public PostResponseDTO actualizarPost(Long id, PostRequestDTO requestDTO) {
        // 1. Buscamos el post existente en la base de datos
        Post postExistente = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado con id: " + id));

        // 2. ¡EL CERROJO DE SEGURIDAD!
        verificarAutorOAdmin(postExistente);

        // 3. Actualizamos los campos manualmente o a través del mapper
        postExistente.setTitulo(requestDTO.titulo());
        postExistente.setCuerpo(requestDTO.cuerpo());
        postExistente.setFotoUrl(requestDTO.fotoUrl());
        if (requestDTO.esPublico() != null) {
            postExistente.setEsPublico(requestDTO.esPublico());
        }
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
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado con id: " + id));

        // ¡EL CERROJO DE SEGURIDAD!
        verificarAutorOAdmin(post);

        postRepository.deleteById(id);
    }

    // Obtener posts paginados
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponseDTO> obtenerPostsPaginados(int numeroPagina, int tamanoPagina, String ordenarPor,
            String direccion, Long categoriaId, LocalDateTime fechaInicio, Boolean soloMisPosts) {

        // 1. Averiguar quién es el usuario actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        boolean esAdmin = false;

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            username = auth.getName();
            esAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }
        

        // 2. Configuramos la dirección del ordenamiento (ASC o DESC)
        Sort sort = direccion.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(ordenarPor).ascending()
                : Sort.by(ordenarPor).descending();

        // 3. Creamos el objeto Pageable (Atención: Spring usa índice 0 para la primera
        // página)
        Pageable pageable = PageRequest.of(numeroPagina, tamanoPagina, sort);

        // 4. Crear la Specification con los filtros
        Specification<Post> spec = PostSpecification.conFiltrosDinamicos(username, esAdmin, categoriaId, fechaInicio, soloMisPosts);

        // 5. Buscamos en la base de datos
        Page<Post> postsPaginados = postRepository.findAll(spec, pageable);

        // 6. Mapeamos el Page<Post> a Page<PostResponseDTO>
        return postsPaginados.map(postMapper::toPostResponseDTO);
    }

   
}

