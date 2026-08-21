package com.cacaoscan.backend.config;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Jackson para serializar/deserializar
 * geometrías JTS (Polygon, Point) en formato GeoJSON.
 *
 * Esto permite que las entidades con campos Polygon y Point
 * se conviertan automáticamente a/desde JSON cuando se usan
 * en los endpoints REST.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JtsModule jtsModule() {
        return new JtsModule();
    }
}
