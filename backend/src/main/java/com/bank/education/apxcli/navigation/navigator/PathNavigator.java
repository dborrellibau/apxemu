package com.bank.education.apxcli.navigation.navigator;

import com.bank.education.apxcli.navigation.model.NavigationPath;
import com.bank.education.apxcli.navigation.model.PathType;
import com.bank.education.apxcli.navigation.parser.PathParser;
import com.bank.education.apxcli.navigation.resolver.PathTypeResolver;
import com.bank.education.apxcli.navigation.validator.PathValidator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Maneja operaciones de navegación para el comando cd
 */
@Component
public class PathNavigator {

    private final PathParser pathParser;
    private final PathTypeResolver pathTypeResolver;
    private final PathValidator pathValidator;

    public PathNavigator(PathParser pathParser,
                        PathTypeResolver pathTypeResolver,
                        PathValidator pathValidator) {
        this.pathParser = pathParser;
        this.pathTypeResolver = pathTypeResolver;
        this.pathValidator = pathValidator;
    }

    /**
     * Navega a un destino desde la posición actual
     * Soporta rutas absolutas, relativas y cd ..
     * 
     * @param input Path ingresado por usuario (ej: "customer-service", "dto", "..", "/root/path")
     * @param current Posición actual (null = root)
     * @return NavigationPath del destino, o null si es inválido
     */
    public NavigationPath navigateTo(String input, NavigationPath current) {
        if (input == null || input.trim().isEmpty()) {
            return current;
        }

        // Caso especial: cd ..
        if ("..".equals(input.trim())) {
            return navigateBack(current);
        }

        // Parsear input
        List<String> segments = pathParser.parse(input);
        
        // Determinar si es ruta absoluta (empieza con /)
        boolean isAbsolute = input.trim().startsWith("/");

        // Construir segmentos finales
        List<String> finalSegments;
        if (isAbsolute) {
            // Ruta absoluta: usar directamente
            finalSegments = segments;
        } else {
            // Ruta relativa: combinar con current
            finalSegments = combineSegments(current, segments);
        }

        // Resolver tipo del destino
        PathType targetType = pathTypeResolver.resolveType(finalSegments);
        if (targetType == null) {
            return null; // Path inválido
        }

        // Validar transición (solo para rutas relativas de UN SOLO PASO)
        // Si hay múltiples segmentos en el input, es una ruta completa y no necesita validación de transición
        if (!isAbsolute && current != null && segments.size() == 1) {
            if (!pathValidator.canNavigate(current.getType(), targetType)) {
                return null; // Transición inválida
            }
        }

        // Validar existencia
        if (!exists(finalSegments, targetType)) {
            return null; // No existe
        }

        // Crear NavigationPath destino
        return createNavigationPath(finalSegments, targetType);
    }

    /**
     * Navega un nivel hacia atrás (cd ..)
     * 
     * @param current Posición actual
     * @return Parent path, o null si ya estamos en root
     */
    public NavigationPath navigateBack(NavigationPath current) {
        if (current == null || current.getLevel() == 0) {
            return null; // Ya en root
        }

        List<String> currentSegments = current.getSegments();
        if (currentSegments.isEmpty()) {
            return null; // Ya en root
        }

        // Remover último segmento
        List<String> parentSegments = new ArrayList<>(currentSegments.subList(0, currentSegments.size() - 1));

        // Resolver tipo del parent
        PathType parentType = pathTypeResolver.resolveType(parentSegments);
        if (parentType == null) {
            return null;
        }

        return createNavigationPath(parentSegments, parentType);
    }

    /**
     * Combina segmentos actuales con nuevos segmentos relativos
     */
    private List<String> combineSegments(NavigationPath current, List<String> newSegments) {
        List<String> result = new ArrayList<>();
        
        if (current != null && !current.getSegments().isEmpty()) {
            result.addAll(current.getSegments());
        }
        
        result.addAll(newSegments);
        
        return result;
    }

    /**
     * Valida que el path exista en BD según su tipo
     */
    private boolean exists(List<String> segments, PathType type) {
        switch (type) {
            case ROOT:
                return segments.isEmpty();
                
            case DU_ONLINE:
            case DU_LIB:
            case COMPONENT_STANDALONE:
                // Todos los componentes de nivel 1 deben existir en BD
                return segments.size() == 1 && pathValidator.deploymentUnitExists(segments.get(0));
                
            case FOLDER:
                return segments.size() == 2 && 
                       pathValidator.folderExists(segments.get(0), segments.get(1));
                
            case COMPONENT_IN_DULIB:
                return segments.size() == 2 && 
                       pathValidator.componentExists(segments.get(0), segments.get(1));
                
            case COMPONENT_IN_FOLDER:
                return segments.size() == 3 && 
                       pathValidator.componentExistsInFolder(segments.get(0), segments.get(1), segments.get(2));
                
            default:
                return false;
        }
    }

    /**
     * Crea NavigationPath con tipo y parent type
     */
    private NavigationPath createNavigationPath(List<String> segments, PathType type) {
        PathType parentType = null;
        
        if (!segments.isEmpty()) {
            List<String> parentSegments = new ArrayList<>(segments.subList(0, segments.size() - 1));
            parentType = pathTypeResolver.resolveType(parentSegments);
        }
        
        return new NavigationPath(segments, type, parentType);
    }
}
