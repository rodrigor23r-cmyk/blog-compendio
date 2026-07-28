package com.example.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.security.JwtAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // Necesario para inyectar el filtro automáticamente
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Indicamos que usaremos BCrypt para encriptar las contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Exponemos el AuthenticationManager para poder usarlo en el AuthController
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Activamos la configuración CORS que definimos abajo
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. Desactivamos CSRF (necesario para APIs REST con POST/PUT/DELETE)
            .csrf(csrf -> csrf.disable())
            
            // MUY IMPORTANTE: Le decimos a Spring que no guarde sesiones en memoria (STATELESS)
            // porque cada petición deberá traer su propio token
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Reglas de acceso a las rutas
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas: login y registro
                .requestMatchers("/api/auth/**").permitAll()
                // Permitimos a cualquiera LEER (GET) posts, comentarios y categorías
                .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categorias/**").permitAll()
                // Cualquier otra petición (POST, PUT, DELETE) requiere estar autenticado con token
                .anyRequest().authenticated()
            );

        // Añadimos nuestro filtro JWT justo antes del filtro por defecto de Spring Security
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Bean para definir las reglas exactas de qué orígenes y métodos permitimos
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permitimos el puerto por defecto de Angular en desarrollo
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));

        // Permitimos todos los métodos HTTP que usaremos en las operaciones CRUD
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Permitimos las cabeceras estándar y la de Authorization para cuando pongamos JWT
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));

        // Permitimos enviar credenciales/cookies si fuera necesario
        configuration.setAllowCredentials(true);

        // Aplicamos esta configuración a TODAS las rutas de la API (/api/**)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}