package com.bank.education.apxcli.navigation.model;

/**
 * Enum representing the type of a navigation path in the virtual filesystem
 * 
 * Structure:
 * ROOT (level 0)
 *   ├─ DU_ONLINE (level 1)
 *   │    └─ FOLDER (level 2)
 *   │         └─ COMPONENT_IN_FOLDER (level 3)
 *   ├─ DU_LIB (level 1)
 *   │    └─ COMPONENT_IN_DULIB (level 2)
 *   └─ COMPONENT_STANDALONE (level 1)
 */
public enum PathType {
    /**
     * Root directory (/vether)
     * Level 0
     */
    ROOT,
    
    /**
     * Online Deployment Unit (DU_ONLINE type)
     * Example: customer-service
     * Level 1
     */
    DU_ONLINE,
    
    /**
     * Library Deployment Unit (DU_LIB type)
     * Example: payment-lib
     * Level 1
     */
    DU_LIB,
    
    /**
     * Folder within a DU_ONLINE
     * Example: dto, lib, transactions
     * Level 2
     */
    FOLDER,
    
    /**
     * Component inside a folder (within DU_ONLINE)
     * Example: customer-service/dto/customer-dto
     * Level 3
     */
    COMPONENT_IN_FOLDER,
    
    /**
     * Component directly inside a DU_LIB
     * Example: payment-lib/UUAAR001 or payment-lib/UUAAR001IMPL
     * Level 2
     */
    COMPONENT_IN_DULIB,
    
    /**
     * Standalone component created at root level
     * Example: customer-dto (DTO/LIB/TRX without container)
     * Level 1
     */
    COMPONENT_STANDALONE;
    
    /**
     * Check if this type represents a component
     */
    public boolean isComponent() {
        return this == COMPONENT_IN_FOLDER || 
               this == COMPONENT_IN_DULIB || 
               this == COMPONENT_STANDALONE;
    }
    
    /**
     * Check if this type represents a deployment unit
     */
    public boolean isDeploymentUnit() {
        return this == DU_ONLINE || this == DU_LIB;
    }
    
    /**
     * Check if this type can have children
     */
    public boolean canHaveChildren() {
        return this == ROOT || 
               this == DU_ONLINE || 
               this == DU_LIB || 
               this == FOLDER;
    }
}
