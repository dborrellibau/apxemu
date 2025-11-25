package com.bank.education.apxcli.strategy;

import com.bank.education.apxcli.model.ComponentFolder;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.dto.CommandResponse;

import java.util.List;

/**
 * Strategy interface for different deployment unit behaviors
 */
public interface DeploymentUnitStrategy {
    
    /**
     * Creates default folders for this deployment unit type
     */
    List<ComponentFolder> createDefaultFolders(DeploymentUnit deploymentUnit);
    
    /**
     * Validates if this deployment unit type can be created with given parameters
     */
    CommandResponse validateCreation(String uuaa, String name, String description, Object... additionalParams);
    
    /**
     * Indicates if this deployment unit type can contain folders
     */
    boolean canContainFolders();
    
    /**
     * Indicates if this deployment unit type can contain nested objects within folders
     */
    boolean canContainNestedObjects();
    
    /**
     * Gets the supported folder types for this deployment unit
     */
    List<ComponentFolder.FolderType> getSupportedFolderTypes();
    
    /**
     * Gets a human-readable description of this deployment unit type
     */
    String getDescription();
    
    /**
     * Gets the form prompts for creating this deployment unit type
     */
    List<String> getFormPrompts();
    
    /**
     * Gets the number of form steps required for creation
     */
    int getFormStepsCount();
}