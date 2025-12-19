package com.bank.education.apxcli.navigation.permission;

import com.bank.education.apxcli.navigation.model.PathType;
import org.springframework.stereotype.Component;

/**
 * Valida permisos de comandos según el PathType actual
 * Define qué comandos son válidos en cada posición de la jerarquía
 */
@Component
public class CommandPermissionService {

    /**
     * Valida si se puede crear un deployment unit desde la posición actual
     * Solo permitido en ROOT
     */
    public boolean canCreateDeploymentUnit(PathType currentType) {
        return currentType == PathType.ROOT;
    }

    /**
     * Valida si se puede crear un componente desde la posición actual
     * Permitido en: FOLDER, DU_LIB
     */
    public boolean canCreateComponent(PathType currentType) {
        return currentType == PathType.FOLDER ||
               currentType == PathType.DU_LIB;
    }

    /**
     * Valida si se puede eliminar la entidad actual
     * No permitido en ROOT
     */
    public boolean canDelete(PathType currentType) {
        return currentType != PathType.ROOT;
    }

    /**
     * Valida si se puede crear una dependencia desde la posición actual
     * Permitido en: COMPONENT_IN_FOLDER, COMPONENT_IN_DULIB, COMPONENT_STANDALONE
     */
    public boolean canCreateDependency(PathType currentType) {
        return currentType == PathType.COMPONENT_IN_FOLDER ||
               currentType == PathType.COMPONENT_IN_DULIB ||
               currentType == PathType.COMPONENT_STANDALONE;
    }

    /**
     * Valida si se puede eliminar una dependencia desde la posición actual
     * Mismo criterio que crear dependencia
     */
    public boolean canDeleteDependency(PathType currentType) {
        return canCreateDependency(currentType);
    }

    /**
     * Valida si se puede listar contenido desde la posición actual
     * Permitido en todos excepto componentes (que no tienen hijos)
     */
    public boolean canList(PathType currentType) {
        return currentType != PathType.COMPONENT_IN_FOLDER &&
               currentType != PathType.COMPONENT_IN_DULIB &&
               currentType != PathType.COMPONENT_STANDALONE;
    }

    /**
     * Valida si se puede mostrar información de la entidad actual
     * Permitido en todos los tipos
     */
    public boolean canShowInfo(PathType currentType) {
        return currentType != null;
    }

    /**
     * Valida si se puede navegar hacia abajo desde la posición actual
     * No permitido en componentes (no tienen hijos)
     */
    public boolean canNavigateDown(PathType currentType) {
        return currentType != PathType.COMPONENT_IN_FOLDER &&
               currentType != PathType.COMPONENT_IN_DULIB &&
               currentType != PathType.COMPONENT_STANDALONE;
    }

    /**
     * Valida si se puede navegar hacia arriba desde la posición actual
     * No permitido en ROOT
     */
    public boolean canNavigateUp(PathType currentType) {
        return currentType != PathType.ROOT;
    }

    /**
     * Valida tipo de componente que se puede crear en la posición actual
     * Retorna mensaje de error si no es válido, o null si es válido
     */
    public String validateComponentType(PathType currentType, String componentType) {
        if (currentType == PathType.FOLDER) {
            // En carpetas: solo se pueden crear DTO, LIB, TRX según la carpeta
            return null; // La validación específica se hace por nombre de carpeta
        }
        
        if (currentType == PathType.DU_LIB) {
            // En DU_LIB: solo se pueden crear LIB o LIB_IMPL
            if (!"LIB".equalsIgnoreCase(componentType) && 
                !"LIB-IMPL".equalsIgnoreCase(componentType)) {
                return "En DU-LIB solo se pueden crear componentes de tipo LIB o LIB-IMPL";
            }
        }
        
        return null; // Válido
    }

    /**
     * Valida reglas de dependencias según tipos de componentes
     * Retorna mensaje de error si no es válido, o null si es válido
     */
    public String validateDependencyRules(String sourceType, String targetType) {
        // DTO solo puede depender de DTO
        if ("DTO".equalsIgnoreCase(sourceType)) {
            if (!"DTO".equalsIgnoreCase(targetType)) {
                return "Componentes DTO solo pueden depender de otros DTOs";
            }
        }
        
        // LIB puede depender de DTO y LIB
        if ("LIB".equalsIgnoreCase(sourceType)) {
            if (!"DTO".equalsIgnoreCase(targetType) && 
                !"LIB".equalsIgnoreCase(targetType)) {
                return "Componentes LIB solo pueden depender de DTO o LIB";
            }
        }
        
        // TRX puede depender de DTO, LIB y TRX
        if ("TRX".equalsIgnoreCase(sourceType)) {
            if (!"DTO".equalsIgnoreCase(targetType) && 
                !"LIB".equalsIgnoreCase(targetType) &&
                !"TRX".equalsIgnoreCase(targetType)) {
                return "Componentes TRX solo pueden depender de DTO, LIB o TRX";
            }
        }
        
        return null; // Válido
    }

    /**
     * Obtiene mensaje de error genérico para comando no permitido
     */
    public String getPermissionDeniedMessage(String command, PathType currentType) {
        return String.format("El comando '%s' no está permitido en %s", 
                           command, getPathTypeDescription(currentType));
    }

    /**
     * Obtiene descripción legible del PathType
     */
    private String getPathTypeDescription(PathType type) {
        switch (type) {
            case ROOT:
                return "la raíz del sistema";
            case DU_ONLINE:
                return "un Deployment Unit Online";
            case DU_LIB:
                return "un Deployment Unit Library";
            case FOLDER:
                return "una carpeta";
            case COMPONENT_IN_FOLDER:
                return "un componente dentro de carpeta";
            case COMPONENT_IN_DULIB:
                return "un componente en DU-LIB";
            case COMPONENT_STANDALONE:
                return "un componente standalone";
            default:
                return "esta posición";
        }
    }
}
