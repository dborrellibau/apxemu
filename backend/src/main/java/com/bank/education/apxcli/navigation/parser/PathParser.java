package com.bank.education.apxcli.navigation.parser;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parser de rutas para el sistema de navegación
 * Normaliza y tokeniza paths sin acceder a la base de datos
 */
@Component
public class PathParser {

    /**
     * Parsea una ruta cruda en sus componentes (segmentos)
     * Ejemplos:
     *   "customer-service" -> [customer-service]
     *   "customer-service/dto" -> [customer-service, dto]
     *   "customer-service/dto/CustomerDTO" -> [customer-service, dto, CustomerDTO]
     *   "" -> []
     */
    public List<String> parse(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Normalizar: trim, eliminar múltiples slashes
        String normalized = normalizePath(rawPath);

        // Remover '/' inicial si existe (rutas absolutas)
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        // Tokenizar y retornar segmentos
        return tokenize(normalized);
    }

    /**
     * Normaliza el path:
     * - Trim espacios
     * - Reemplaza múltiples '/' por uno solo
     * - Elimina '/' final si existe
     */
    private String normalizePath(String path) {
        String trimmed = path.trim();
        
        // Reemplazar múltiples slashes por uno solo
        String normalized = trimmed.replaceAll("/+", "/");
        
        // Eliminar slash final (excepto si es solo "/")
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        
        return normalized;
    }

    /**
     * Tokeniza el path en segmentos
     * Filtra segmentos vacíos y "." (directorio actual)
     */
    private List<String> tokenize(String normalizedPath) {
        if (normalizedPath.isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(normalizedPath.split("/"))
                .filter(segment -> !segment.isEmpty() && !segment.equals("."))
                .collect(Collectors.toList());
    }

    /**
     * Calcula el nivel basado en la cantidad de segmentos
     * Level 0 = root (0 segmentos)
     * Level 1 = DU o componente standalone (1 segmento)
     * Level 2 = carpeta en DU_ONLINE o componente en DU_LIB (2 segmentos)
     * Level 3 = componente dentro de carpeta (3 segmentos)
     */
    public int calculateLevel(List<String> segments) {
        return segments.size();
    }

    /**
     * Valida que el path no exceda el nivel máximo permitido (3)
     */
    public boolean isWithinMaxLevel(List<String> segments) {
        return segments.size() <= 3;
    }

    /**
     * Extrae el nombre del DU del path (primer segmento)
     * Retorna null si no hay DU
     */
    public String extractDuName(List<String> segments) {
        if (segments.isEmpty()) {
            return null;
        }
        return segments.get(0);
    }

    /**
     * Extrae el nombre de la carpeta del path (segundo segmento)
     * Retorna null si no hay carpeta
     */
    public String extractFolderName(List<String> segments) {
        if (segments.size() < 2) {
            return null;
        }
        return segments.get(1);
    }

    /**
     * Extrae el nombre del componente del path (último segmento)
     * Retorna null si no hay componente
     */
    public String extractComponentName(List<String> segments) {
        if (segments.isEmpty()) {
            return null;
        }
        return segments.get(segments.size() - 1);
    }
}
