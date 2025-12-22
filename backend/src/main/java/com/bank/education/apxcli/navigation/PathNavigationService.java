package com.bank.education.apxcli.navigation;

import com.bank.education.apxcli.navigation.model.NavigationPath;
import com.bank.education.apxcli.navigation.model.PathType;
import com.bank.education.apxcli.navigation.navigator.PathNavigator;
import com.bank.education.apxcli.navigation.parser.PathParser;
import com.bank.education.apxcli.navigation.resolver.PathTypeResolver;
import com.bank.education.apxcli.navigation.validator.PathValidator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade para el sistema de navegación
 * Punto de entrada único para todas las operaciones de navegación
 */
@Service
public class PathNavigationService {

    private final PathParser pathParser;
    private final PathTypeResolver pathTypeResolver;
    private final PathValidator pathValidator;
    private final PathNavigator pathNavigator;

    public PathNavigationService(PathParser pathParser,
                                 PathTypeResolver pathTypeResolver,
                                 PathValidator pathValidator,
                                 PathNavigator pathNavigator) {
        this.pathParser = pathParser;
        this.pathTypeResolver = pathTypeResolver;
        this.pathValidator = pathValidator;
        this.pathNavigator = pathNavigator;
    }

    // ========== NAVEGACIÓN ==========

    /**
     * Navega a un destino desde la posición actual
     * 
     * @param input Path ingresado por usuario
     * @param current Posición actual (null = root)
     * @return NavigationPath del destino, o null si es inválido
     */
    public NavigationPath navigate(String input, NavigationPath current) {
        return pathNavigator.navigateTo(input, current);
    }

    /**
     * Navega un nivel hacia atrás (cd ..)
     */
    public NavigationPath navigateBack(NavigationPath current) {
        return pathNavigator.navigateBack(current);
    }

    /**
     * Navega a root
     */
    public NavigationPath navigateToRoot() {
        return new NavigationPath(new ArrayList<>(), PathType.ROOT, null);
    }

    // ========== PARSING Y RESOLUCIÓN ==========

    /**
     * Parsea un path string en segmentos
     */
    public List<String> parsePath(String path) {
        return pathParser.parse(path);
    }

    /**
     * Resuelve el tipo de un path a partir de sus segmentos
     */
    public PathType resolvePathType(List<String> segments) {
        return pathTypeResolver.resolveType(segments);
    }

    /**
     * Resuelve el tipo de un path string
     */
    public PathType resolvePathType(String path) {
        List<String> segments = pathParser.parse(path);
        return pathTypeResolver.resolveType(segments);
    }

    // ========== VALIDACIÓN ==========

    /**
     * Valida que una transición entre tipos sea válida
     */
    public boolean canNavigate(PathType from, PathType to) {
        return pathValidator.canNavigate(from, to);
    }

    /**
     * Valida que un deployment unit exista
     */
    public boolean deploymentUnitExists(String duName) {
        return pathValidator.deploymentUnitExists(duName);
    }

    /**
     * Valida que un componente exista en un DU
     */
    public boolean componentExists(String duName, String componentName) {
        return pathValidator.componentExists(duName, componentName);
    }

    /**
     * Valida que una carpeta exista en un DU
     */
    public boolean folderExists(String duName, String folderName) {
        return pathValidator.folderExists(duName, folderName);
    }

    /**
     * Valida que un componente exista dentro de una carpeta
     */
    public boolean componentExistsInFolder(String duName, String folderName, String componentName) {
        return pathValidator.componentExistsInFolder(duName, folderName, componentName);
    }

    /**
     * Valida formato de nombre (para creación de entidades)
     */
    public boolean isValidName(String name) {
        return pathValidator.isValidName(name);
    }

    // ========== CACHE ==========

    /**
     * Limpia el cache de tipos de path
     */
    public void clearCache() {
        pathTypeResolver.clearCache();
    }

    /**
     * Invalida una entrada específica del cache
     */
    public void invalidateCache(String path) {
        pathTypeResolver.invalidateCache(path);
    }

    // ========== MÉTODOS DE CONVENIENCIA ==========

    /**
     * Crea NavigationPath a partir de un path string
     * Útil para inicializar posiciones conocidas
     */
    public NavigationPath createPath(String path) {
        List<String> segments = pathParser.parse(path);
        PathType type = pathTypeResolver.resolveType(segments);
        
        if (type == null) {
            return null;
        }

        // Calcular parent type
        PathType parentType = null;
        if (!segments.isEmpty()) {
            List<String> parentSegments = segments.subList(0, segments.size() - 1);
            parentType = pathTypeResolver.resolveType(parentSegments);
        }

        return new NavigationPath(segments, type, parentType);
    }

    /**
     * Obtiene el path absoluto como string
     */
    public String getAbsolutePath(NavigationPath path) {
        if (path == null || path.getSegments().isEmpty()) {
            return "/";
        }
        return "/" + String.join("/", path.getSegments());
    }

    /**
     * Verifica si un path es válido y existe
     */
    public boolean isValidPath(String path) {
        NavigationPath navPath = createPath(path);
        return navPath != null;
    }
}
