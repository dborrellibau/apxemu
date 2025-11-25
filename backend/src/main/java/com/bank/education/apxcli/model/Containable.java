package com.bank.education.apxcli.model;

import java.util.List;

/**
 * Interface for objects that can contain other objects (DeploymentUnits or ComponentFolders)
 * This allows both DeploymentUnit and ComponentFolder to act as containers
 */
public interface Containable {
    
    /**
     * Gets the unique identifier for this containable object
     */
    Long getId();
    
    /**
     * Gets the name/display text for this containable object
     */
    String getName();
    
    /**
     * Gets the type of this container
     */
    ContainerType getContainerType();
    
    /**
     * Gets all child DeploymentUnits contained in this object
     */
    List<DeploymentUnit> getChildDeploymentUnits();
    
    /**
     * Gets all child ComponentFolders contained in this object
     */
    List<ComponentFolder> getChildComponentFolders();
    
    /**
     * Adds a DeploymentUnit as a child of this container
     */
    void addChildDeploymentUnit(DeploymentUnit unit);
    
    /**
     * Adds a ComponentFolder as a child of this container
     */
    void addChildComponentFolder(ComponentFolder folder);
    
    /**
     * Removes a DeploymentUnit from this container
     */
    void removeChildDeploymentUnit(DeploymentUnit unit);
    
    /**
     * Removes a ComponentFolder from this container
     */
    void removeChildComponentFolder(ComponentFolder folder);
    
    /**
     * Checks if this container can contain the specified type
     */
    boolean canContain(ContainerType type);
    
    /**
     * Gets the parent container (null if this is a root object)
     */
    Containable getParentContainer();
    
    /**
     * Sets the parent container for this object
     */
    void setParentContainer(Containable parent);
}