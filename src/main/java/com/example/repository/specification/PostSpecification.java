package com.example.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import com.example.entities.Post;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostSpecification {

    public static Specification<Post> conFiltrosDinamicos(
            String usernameAutenticado,
            boolean esAdmin,
            Long categoriaId,
            LocalDateTime fechaInicio,
            Boolean soloMisPosts) {

        return (root, query, cb) -> {
            // Un predicado es una condición que se aplica a la consulta. Aquí vamos a
            // construir una lista de predicados
            List<Predicate> predicates = new ArrayList<>();

            // --- REGLA DE VISIBILIDAD Y PROPIEDAD ---
            if (Boolean.TRUE.equals(soloMisPosts)) {
                // Si el usuario pide explícitamente "Mis Posts":
                if (usernameAutenticado != null && !usernameAutenticado.equals("anonymousUser")) {
                    // Filtramos SOLO los posts del usuario autenticado
                    predicates.add(cb.equal(root.get("autor").get("username"), usernameAutenticado));
                } else {
                    // Si un anónimo pide misPosts=true, no tiene posts propios, devolvemos conjunto
                    // vacío
                    predicates.add(cb.disjunction());
                }
            } else {
                // 1. REGLA DE VISIBILIDAD para todos los demás casos (no soloMisPosts)
                if (!esAdmin) {
                    if (usernameAutenticado != null && !usernameAutenticado.equals("anonymousUser")) {
                        // Logueado: Ve los públicos OR los suyos propios
                        Predicate esPublico = cb.equal(root.get("esPublico"), true);
                        Predicate esElAutor = cb.equal(root.get("autor").get("username"), usernameAutenticado);
                        predicates.add(cb.or(esPublico, esElAutor));
                    } else {
                        // Anónimo: Solo ve los públicos
                        predicates.add(cb.equal(root.get("esPublico"), true));
                    }
                }
            }
            // 2. FILTRO POR CATEGORÍA (Opcional)
            if (categoriaId != null) {
                predicates.add(cb.equal(root.join("categorias").get("id"), categoriaId));
            }

            // 3. FILTRO POR FECHA (Opcional)
            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fechaInicio));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
