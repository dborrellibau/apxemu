package com.bank.education.apxcli.navigation.resolver;

import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.navigation.model.PathType;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resuelve el tipo de una ruta consultando la base de datos
 * Mantiene un cache para optimizar consultas repetidas
 */
@Component
public class PathTypeResolver {

    private final DeploymentUnitRepository deploymentUnitRepository;
    private final Map<String, PathType> cache;

    public PathTypeResolver(DeploymentUnitRepository deploymentUnitRepository) {
        this.deploymentUnitRepository = deploymentUnitRepository;
        this.cache = new ConcurrentHashMap<>();
    }

    /**
     * Resuelve el tipo de ruta basado en los segmentos
     * Consulta la BD solo cuando es necesario
     * 
     * @param segments Lista de segmentos del path
     * @return PathType correspondiente, o null si no se puede determinar
     */
    public PathType resolveType(List<String> segments) {
        if (segments == null || segments.isEmpty()) {
            return PathType.ROOT;
        }

        int level = segments.size();
        String duName = segments.get(0);

        // Intentar desde cache
        String cacheKey = String.join("/", segments);
        PathType cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // Consultar BD para obtener el DU
        Optional<DeploymentUnit> duOpt = deploymentUnitRepository.findByName(duName);
        
        if (!duOpt.isPresent()) {
            // Nivel 1 sin DU en BD = componente standalone
            if (level == 1) {
                PathType type = PathType.COMPONENT_STANDALONE;
                cache.put(cacheKey, type);
                return type;
            }
            // Path inválido (niveles mayores sin DU)
            return null;
        }

        DeploymentUnit du = duOpt.get();
        PathType resolvedType = resolveTypeWithDU(segments, du);
        
        if (resolvedType != null) {
            cache.put(cacheKey, resolvedType);
        }
        
        return resolvedType;
    }

    /**
     * Resuelve el tipo cuando ya tenemos el DeploymentUnit
     */
    private PathType resolveTypeWithDU(List<String> segments, DeploymentUnit du) {
        int level = segments.size();

        // Level 1: DU_ONLINE o DU_LIB
        if (level == 1) {
            return mapDeploymentUnitTypeToPathType(du.getType());
        }

        // Level 2+: depende del tipo de DU
        if (du.getType() == DeploymentUnit.DeploymentUnitType.DU_ONLINE) {
            // DU_ONLINE tiene carpetas en nivel 2
            if (level == 2) {
                String folderName = segments.get(1);
                if (isValidFolder(folderName)) {
                    return PathType.FOLDER;
                }
            }
            // Y componentes en nivel 3
            if (level == 3) {
                String folderName = segments.get(1);
                if (isValidFolder(folderName)) {
                    return PathType.COMPONENT_IN_FOLDER;
                }
            }
        } else if (du.getType() == DeploymentUnit.DeploymentUnitType.DU_LIB) {
            // DU_LIB tiene componentes directamente en nivel 2
            if (level == 2) {
                return PathType.COMPONENT_IN_DULIB;
            }
        }

        return null;
    }

    /**
     * Mapea DeploymentUnit.DeploymentUnitType a PathType
     */
    private PathType mapDeploymentUnitTypeToPathType(DeploymentUnit.DeploymentUnitType duType) {
        switch (duType) {
            case DU_ONLINE:
                return PathType.DU_ONLINE;
            case DU_LIB:
                return PathType.DU_LIB;
            default:
                return null;
        }
    }

    /**
     * Valida si el nombre corresponde a una carpeta válida
     * Carpetas permitidas: dto, lib, transactions
     */
    private boolean isValidFolder(String folderName) {
        return "dto".equalsIgnoreCase(folderName) ||
               "lib".equalsIgnoreCase(folderName) ||
               "transactions".equalsIgnoreCase(folderName);
    }

    /**
     * Limpia el cache (útil para testing o cuando se modifica la estructura)
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Invalida una entrada específica del cache
     */
    public void invalidateCache(String path) {
        cache.remove(path);
    }
}
