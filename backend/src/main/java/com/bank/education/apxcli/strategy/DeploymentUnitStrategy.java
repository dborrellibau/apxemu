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
    
    /**
     * Indicates if this deployment unit type supports input/output management
     * Only TRX components support this feature
     * 
     * @return true if supports in/out management, false otherwise
     */
    default boolean supportsInOutManagement() {
        return false;
    }
    
    /**
     * Adds a DTO as input to a transaction
     * 
     * @param unit The transaction unit
     * @param dto The DTO to add as input
     * @return CommandResponse with result
     */
    default CommandResponse addInput(DeploymentUnit unit, DeploymentUnit dto) {
        return CommandResponse.error("This component type does not support input management");
    }
    
    /**
     * Adds a DTO as output to a transaction
     * 
     * @param unit The transaction unit
     * @param dto The DTO to add as output
     * @return CommandResponse with result
     */
    default CommandResponse addOutput(DeploymentUnit unit, DeploymentUnit dto) {
        return CommandResponse.error("This component type does not support output management");
    }
    
    /**
     * Removes a DTO from transaction inputs
     * 
     * @param unit The transaction unit
     * @param dto The DTO to remove from inputs
     * @return CommandResponse with result
     */
    default CommandResponse removeInput(DeploymentUnit unit, DeploymentUnit dto) {
        return CommandResponse.error("This component type does not support input management");
    }
    
    /**
     * Removes a DTO from transaction outputs
     * 
     * @param unit The transaction unit
     * @param dto The DTO to remove from outputs
     * @return CommandResponse with result
     */
    default CommandResponse removeOutput(DeploymentUnit unit, DeploymentUnit dto) {
        return CommandResponse.error("This component type does not support output management");
    }
}