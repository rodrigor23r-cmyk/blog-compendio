package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageStorageService {

    // Lee la ruta desde el application.properties
    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path rootLocation;

    // @PostConstruct hace que este método se ejecute nada más arrancar Spring Boot
    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadDir);
        try {
            // Crea la carpeta y sus subcarpetas si no existen
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar la carpeta de almacenamiento", e);
        }
    }

    public String guardarImagen(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null; // O lanza excepción si la foto es obligatoria
        }

        // 1. RESTRINGIR: Solo archivos de imagen
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen (JPG, PNG, etc.)");
        }

        // 2. SANITIZAR: Limpiar el nombre original por si contiene caracteres extraños o rutas (../)
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // 3. SEGURIDAD: Evitar colisiones de nombres usando un UUID
        // (Si dos personas suben "foto.jpg", la segunda sobreescribiría a la primera)
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String nuevoNombreArchivo = UUID.randomUUID().toString() + extension;

        // 4. GUARDAR FÍSICAMENTE (NIO)
        try {
            // Construimos la ruta final absoluta
            Path destinationFile = this.rootLocation.resolve(Paths.get(nuevoNombreArchivo)).normalize().toAbsolutePath();
            
            // Copiamos el flujo de datos al destino. Si existe (imposible por el UUID), lo reemplaza.
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            
            return nuevoNombreArchivo; // Devolvemos el nombre seguro para guardarlo en la BBDD
        } catch (IOException e) {
            throw new RuntimeException("Fallo al guardar el archivo " + nuevoNombreArchivo, e);
        }
    }

    public void eliminarImagen(String nombreArchivo) {
        try {
            Path file = rootLocation.resolve(nombreArchivo);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Aquí puedes usar un logger para registrar que no se pudo borrar el archivo
            System.err.println("No se pudo borrar la imagen huérfana: " + nombreArchivo);
        }
    }
}
