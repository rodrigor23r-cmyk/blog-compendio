package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Activamos la configuración CORS que definimos abajo
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. Desactivamos CSRF (necesario para APIs REST con POST/PUT/DELETE)
            .csrf(csrf -> csrf.disable())
            
            // 3. Permitimos el acceso total a todas las rutas por ahora
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

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