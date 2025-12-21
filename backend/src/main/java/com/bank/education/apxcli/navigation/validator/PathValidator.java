package com.bank.education.apxcli.navigation.validator;

import com.bank.education.apxcli.model.ComponentFolder;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.navigation.model.PathType;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Valida rutas de navegación para el comando cd
 * Solo valida existencia en BD y transiciones válidas
 */
@Component
public class PathValidator {

    private final DeploymentUnitRepository deploymentUnitRepository;

    public PathValidator(DeploymentUnitRepository deploymentUnitRepository) {
        this.deploymentUnitRepository = deploymentUnitRepository;
    }

    /**
     * Valida que la transición entre dos tipos de path sea válida (para cd relativo)
     */
    public boolean canNavigate(PathType from, PathType to) {
        if (from == null || to == null) {
            return false;
        }

        // ROOT puede ir a DU_ONLINE, DU_LIB, o COMPONENT_STANDALONE
        if (from == PathType.ROOT) {
            return to == PathType.DU_ONLINE || 
                   to == PathType.DU_LIB || 
                   to == PathType.COMPONENT_STANDALONE;
        }

        // DU_ONLINE puede ir a FOLDER
        if (from == PathType.DU_ONLINE) {
            return to == PathType.FOLDER;
        }

        // FOLDER puede ir a COMPONENT_IN_FOLDER
        if (from == PathType.FOLDER) {
            return to == PathType.COMPONENT_IN_FOLDER;
        }

        // DU_LIB puede ir a COMPONENT_IN_DULIB
        if (from == PathType.DU_LIB) {
            return to == PathType.COMPONENT_IN_DULIB;
        }

        // Componentes y COMPONENT_STANDALONE no tienen hijos
        return false;
    }

    /**
     * Valida que un deployment unit exista en la BD
     */
    public boolean deploymentUnitExists(String duName) {
        if (duName == null || duName.trim().isEmpty()) {
            return false;
        }
        return deploymentUnitRepository.findByName(duName).isPresent();
    }

    /**
     * Valida que un componente exista dentro de un DU
     */
    @Transactional(readOnly = true)
    public boolean componentExists(String duName, String componentName) {
        if (duName == null || componentName == null) {
            return false;
        }

        Optional<DeploymentUnit> parentDuOpt = deploymentUnitRepository.findByName(duName);
        if (!parentDuOpt.isPresent()) {
            return false;
        }

        DeploymentUnit parentDu = parentDuOpt.get();
        
        // Buscar en los child deployment units
        return parentDu.getChildDeploymentUnits().stream()
                .anyMatch(child -> child.getName().equals(componentName));
    }

    /**
     * Valida que un componente exista dentro de una carpeta
     */
    @Transactional(readOnly = true)
    public boolean componentExistsInFolder(String duName, String folderName, String componentName) {
        if (duName == null || folderName == null || componentName == null) {
            return false;
        }

        Optional<DeploymentUnit> duOpt = deploymentUnitRepository.findByName(duName);
        if (!duOpt.isPresent()) {
            return false;
        }

        DeploymentUnit du = duOpt.get();
        
        // Buscar la carpeta usando getChildComponentFolders() de Containable
        Optional<ComponentFolder> folderOpt = du.getChildComponentFolders().stream()
                .filter(folder -> folder.getType().name().equalsIgnoreCase(folderName))
                .findFirst();

        if (!folderOpt.isPresent()) {
            return false;
        }

        ComponentFolder folder = folderOpt.get();
        
        // Buscar el componente en containedUnits
        return folder.getContainedUnits().stream()
                .anyMatch(component -> component.getName().equals(componentName));
    }

    /**
     * Valida que una carpeta exista dentro de un DU
     */
    @Transactional(readOnly = true)
    public boolean folderExists(String duName, String folderName) {
        if (duName == null || folderName == null) {
            return false;
        }

        Optional<DeploymentUnit> duOpt = deploymentUnitRepository.findByName(duName);
        if (!duOpt.isPresent()) {
            return false;
        }

        DeploymentUnit du = duOpt.get();
        
        // Usar getChildComponentFolders() de Containable y comparar por FolderType
        return du.getChildComponentFolders().stream()
                .anyMatch(folder -> folder.getType().name().equalsIgnoreCase(folderName));
    }

    /**
     * Valida que un nombre sea válido para crear entidades (sin caracteres especiales, no vacío)
     */
    public boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        // Solo letras, números, guiones y guiones bajos
        return name.matches("^[a-zA-Z0-9_-]+$");
    }
}
