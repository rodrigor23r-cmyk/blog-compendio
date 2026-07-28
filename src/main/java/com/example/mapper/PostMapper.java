package com.example.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.dto.PostRequestDTO;
import com.example.dto.PostResponseDTO;
import com.example.dto.UsuarioResponseDTO;
import com.example.entities.Categoria;
import com.example.entities.Post;
import com.example.entities.Usuario;

// componentModel = "spring" le dice a MapStruct que convierta esto en un @Bean 
// para que puedas inyectarlo luego con @Autowired o constructores
@Mapper(componentModel = "spring")
public interface PostMapper {

    // ========================================================
    // 1. DE ENTIDAD A RESPONSE DTO (Para enviar a Angular)
    // ========================================================
    PostResponseDTO toPostResponseDTO(Post post);

    // MapStruct es lo bastante listo para usar este método automáticamente
    // cuando intente convertir el atributo 'autor' dentro del Post
    UsuarioResponseDTO toUsuarioResponseDTO(Usuario usuario);

    // Enseñamos a MapStruct cómo pasar de un objeto Categoria a un simple String
    default String mapCategoriaToString(Categoria categoria) {
        return categoria != null ? categoria.getNombre() : null;
    }


    // ========================================================
    // 2. DE REQUEST DTO A ENTIDAD (Al recibir desde Angular)
    // ========================================================
    @Mapping(target = "id", ignore = true) // El ID lo generará MySQL
    @Mapping(target = "fechaCreacion", ignore = true) // Lo asignará el @PrePersist
    @Mapping(target = "comentarios", ignore = true) // Empieza sin comentarios
    @Mapping(target = "autor", source = "autorId")
    @Mapping(target = "categorias", source = "categoriaIds")
    Post toPostEntity(PostRequestDTO requestDTO);

    // Truco de magia JPA: Si solo tenemos el ID del autor, creamos una entidad 
    // vacía solo con el ID. Hibernate es lo bastante listo para usar ese ID
    // como clave foránea (foreign key) al hacer el INSERT del Post.
    default Usuario mapIdToUsuario(Long id) {
        if (id == null) return null;
        return Usuario.builder().id(id).build(); // Usamos el builder de Lombok
    }

    // Lo mismo para enlazar las categorías por ID
    default Categoria mapIdToCategoria(Long id) {
        if (id == null) return null;
        return Categoria.builder().id(id).build();
    }
}
