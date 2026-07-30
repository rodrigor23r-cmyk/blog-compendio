package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;


// WebMvcConfigurer es una interfaz que nos permite personalizar la configuración de Spring MVC.
// nos permite añadir manejadores de recursos estáticos.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Leemos la ruta de la carpeta desde tu application.properties
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Convertimos la ruta relativa ("uploads/posts") en una ruta absoluta del sistema operativo
        // Spring necesita que las rutas de disco duro empiecen por "file:/"
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/api/uploads/posts/**")
                .addResourceLocations(uploadPath);
    }
}