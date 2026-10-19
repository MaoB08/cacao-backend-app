package com.cacaoscan.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir archivos de uploads como recursos estáticos compatibles con cualquier SO (Windows/Linux)
        java.nio.file.Path rootPath = java.nio.file.Paths.get(uploadsDir).toAbsolutePath().normalize();
        
        String perfilUri = rootPath.resolve("perfil").toUri().toString();
        String diagnosticosUri = rootPath.resolve("diagnosticos").toUri().toString();
        String visitasUri = rootPath.resolve("visitas").toUri().toString();

        if (!perfilUri.endsWith("/")) perfilUri += "/";
        if (!diagnosticosUri.endsWith("/")) diagnosticosUri += "/";
        if (!visitasUri.endsWith("/")) visitasUri += "/";

        registry.addResourceHandler("/uploads/perfil/**")
                .addResourceLocations(perfilUri);
        registry.addResourceHandler("/uploads/diagnosticos/**")
                .addResourceLocations(diagnosticosUri);
        registry.addResourceHandler("/uploads/visitas/**")
                .addResourceLocations(visitasUri);
    }
}
