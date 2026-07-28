package com.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    
    // Spring Security usa esta interfaz para buscar usuarios. 
    // ¡La implementaremos en el siguiente paso!
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Extraemos el token de la cabecera HTTP
            String jwt = extraerToken(request);

            // 2. Si hay token y además es válido...
            if (jwt != null && jwtUtils.validarToken(jwt)) {
                
                // Sacamos el nombre de usuario que guardamos dentro del token
                String username = jwtUtils.obtenerUsernameDelToken(jwt);

                // Buscamos al usuario en la base de datos para cargar sus roles
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 3. Creamos el "Carnet de Identidad" (Autenticación) para esta petición
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                                userDetails, 
                                null, 
                                userDetails.getAuthorities()
                        );

                // Le añadimos detalles extra (como la dirección IP desde donde se conecta)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 4. Guardamos la autenticación en el Contexto de Seguridad
                // A partir de aquí, Spring sabe exactamente QUIÉN está haciendo la petición
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("No se pudo establecer la autenticación del usuario en el contexto de seguridad: {}", e.getMessage());
        }

        // 5. Dejamos que la petición continúe su camino hacia el Controlador
        filterChain.doFilter(request, response);
    }

    /**
     * Método auxiliar para extraer el token de la cabecera Authorization.
     * El formato estándar que enviará Angular es: "Bearer eyJhbGciOiJIUzI1..."
     */
    private String extraerToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        // Si la cabecera tiene texto y empieza por "Bearer "
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Recortamos los primeros 7 caracteres ("Bearer ")
        }

        return null;
    }
}