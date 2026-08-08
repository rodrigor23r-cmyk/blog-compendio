package com.example.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Post
 */
@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    // AQUÍ ESTÁ EL TRUCO: columnDefinition = "TEXT" o "LONGTEXT"
    // permite guardar miles de caracteres con etiquetas HTML o Markdown
    @Column(nullable = false, columnDefinition = "TEXT")
    private String cuerpo;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "foto_url")
    private String fotoUrl; // Guardamos la ruta o URL de la imagen, no el archivo en sí

    // Relación N:1 con Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario autor;

    // Atributo de visibilidad
    @Builder.Default
    @Column(name = "es_publico", nullable = false, columnDefinition = "BIT(1) DEFAULT 1")
    private Boolean esPublico = true; // Por defecto, los posts son públicos

    // Relación 1:N con Comentarios. Si borras un post, se borran sus comentarios.
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comentario> comentarios;

    // Relación N:M con Categorias
    // la Clase Categoria no tiene referencia a Post para evitar recursividad
    // infinita al serializar a JSON
    @ManyToMany
    @JoinTable(name = "post_categoria", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private Set<Categoria> categorias; // Usar Set es mejor que List en ManyToMany para evitar problemas de rendimiento
                                       // en Hibernate
    // No sé porqué no necesito inicializar el set de categorías.

    @Builder.Default
    @Column(name = "visitas", nullable = false)
    private Integer visitas = 0;


    // le indico a JPA que antes de persistir el objeto en la base de datos, se debe
    // ejecutar el método prePersist()
    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }

}
