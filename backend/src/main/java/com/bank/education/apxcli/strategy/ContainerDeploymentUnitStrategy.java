package com.bank.education.apxcli.strategy;

import com.bank.education.apxcli.model.ComponentFolder;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.dto.CommandResponse;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Base strategy for deployment units that CONTAIN folders
 * These are container objects like DU-ONLINE, DU-LIB
 */
public abstract class ContainerDeploymentUnitStrategy implements DeploymentUnitStrategy {
    
    @Override
    public List<ComponentFolder> createDefaultFolders(DeploymentUnit deploymentUnit) {
        List<ComponentFolder> folders = new ArrayList<>();
        
        for (ComponentFolder.FolderType folderType : getSupportedFolderTypes()) {
            ComponentFolder folder = new ComponentFolder(folderType, deploymentUnit, folderType.getDescription());
            folders.add(folder);
        }
        
        return folders;
    }
    
    @Override
    public boolean canContainFolders() {
        return true;
    }
    
    @Override
    public boolean canContainNestedObjects() {
        return true;
    }
    
    @Override
    public CommandResponse validateCreation(String uuaa, String name, String description, Object... additionalParams) {
        // Basic validations for container objects
        if (uuaa == null || !uuaa.matches("^[A-Z]{4}$")) {
            return CommandResponse.error("UUAA must be exactly 4 uppercase letters");
        }
        
        if (name == null || name.trim().isEmpty()) {
            return CommandResponse.error("Name cannot be empty");
        }
        
        if (name.contains(" ")) {
            return CommandResponse.error("Container name cannot contain spaces");
        }
        
        if (description == null || description.trim().isEmpty()) {
            return CommandResponse.error("Description cannot be empty");
        }
        
        return CommandResponse.success("Validation passed");
    }
    
    @Override
    public List<String> getFormPrompts() {
        return Arrays.asList(
            "Enter Application (UUAA) - 4 uppercase letters:",
            "Enter Deployment Unit name (no spaces):",
            "Enter Description:"
        );
    }
    
    @Override
    public int getFormStepsCount() {
        return getFormPrompts().size();
    }
    
    /**
     * Subclasses must define which folder types they support
     */
    @Override
    public abstract List<ComponentFolder.FolderType> getSupportedFolderTypes();
}