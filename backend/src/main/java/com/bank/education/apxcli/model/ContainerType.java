package com.bank.education.apxcli.model;

/**
 * Enum representing the different types of containers in the system
 */
public enum ContainerType {
    
    /**
     * A DeploymentUnit acting as a container (like DU-LIB, DU-ONLINE)
     */
    DEPLOYMENT_UNIT,
    
    /**
     * A ComponentFolder acting as a container (like dto/, lib/, transactions/)
     */
    COMPONENT_FOLDER,
    
    /**
     * A simple DeploymentUnit that typically doesn't contain other objects
     */
    SIMPLE_OBJECT,
    
    /**
     * Root container (for system-wide operations)
     */
    ROOT;
    
    /**
     * Checks if this container type can contain the specified child type
     */
    public boolean canContain(ContainerType childType) {
        switch (this) {
            case DEPLOYMENT_UNIT:
                // DeploymentUnits can contain both folders and other deployment units
                return childType == COMPONENT_FOLDER || childType == DEPLOYMENT_UNIT || childType == SIMPLE_OBJECT;
            
            case COMPONENT_FOLDER:
                // ComponentFolders can contain deployment units and simple objects
                return childType == DEPLOYMENT_UNIT || childType == SIMPLE_OBJECT;
            
            case ROOT:
                // Root can contain anything
                return true;
                
            case SIMPLE_OBJECT:
                // Simple objects typically don't contain other objects
                return false;
                
            default:
                return false;
        }
    }
    
    /**
     * Gets a human-readable description of this container type
     */
    public String getDescription() {
        switch (this) {
            case DEPLOYMENT_UNIT:
                return "Deployment Unit Container";
            case COMPONENT_FOLDER:
                return "Component Folder";
            case SIMPLE_OBJECT:
                return "Simple Object";
            case ROOT:
                return "Root Container";
            default:
                return "Unknown";
        }
    }
}