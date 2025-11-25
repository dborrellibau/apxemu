package com.bank.education.apxcli.strategy;

import com.bank.education.apxcli.model.ComponentFolder;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.dto.CommandResponse;

import java.util.Arrays;
import java.util.List;

/**
 * Base strategy for deployment units that DON'T contain folders
 * These are simple objects like DTO, LIB, TRX
 */
public abstract class SimpleDeploymentUnitStrategy implements DeploymentUnitStrategy {
    
    @Override
    public List<ComponentFolder> createDefaultFolders(DeploymentUnit deploymentUnit) {
        // Simple deployment units don't have folders
        return java.util.Collections.emptyList();
    }
    
    @Override
    public boolean canContainFolders() {
        return false;
    }
    
    @Override
    public boolean canContainNestedObjects() {
        return false;
    }
    
    @Override
    public List<ComponentFolder.FolderType> getSupportedFolderTypes() {
        return java.util.Collections.emptyList(); // No folder types supported
    }
    
    @Override
    public CommandResponse validateCreation(String uuaa, String name, String description, Object... additionalParams) {
        // Basic validations for simple objects
        if (uuaa == null || !uuaa.matches("^[A-Z]{4}$")) {
            return CommandResponse.error("UUAA must be exactly 4 uppercase letters");
        }
        
        if (name == null || name.trim().isEmpty()) {
            return CommandResponse.error("Name cannot be empty");
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
            "Enter Code - 3 digits (001-999):",
            "Enter Description:"
        );
    }
    
    @Override
    public int getFormStepsCount() {
        return getFormPrompts().size();
    }
}