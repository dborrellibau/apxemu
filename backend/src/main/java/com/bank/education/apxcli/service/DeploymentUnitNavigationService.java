package com.bank.education.apxcli.service;

import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for handling navigation logic and folder validation
 * for different types of deployment units with caching for performance
 */
@Service
public class DeploymentUnitNavigationService {
    
    private final DeploymentUnitRepository repository;
    private final Map<String, DeploymentUnit.DeploymentUnitType> typeCache = new ConcurrentHashMap<>();
    
    public DeploymentUnitNavigationService(DeploymentUnitRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Get deployment unit type with caching for performance
     */
    public DeploymentUnit.DeploymentUnitType getTypeWithCache(String duName) {
        return typeCache.computeIfAbsent(duName, this::queryDatabaseForType);
    }
    
    /**
     * Validate if a folder name is valid for the given deployment unit
     */
    public boolean isValidFolder(String duName, String folderName) {
        Optional<DeploymentUnit> duOpt = repository.findByName(duName);
        if (!duOpt.isPresent()) {
            return false;
        }
        
        DeploymentUnit du = duOpt.get();
        DeploymentUnit.DeploymentUnitType duType = du.getType();
        
        switch (duType) {
            case DU_ONLINE:
                return isValidOnlineFolder(folderName);
            case DU_LIB:
                return isValidLibFolder(folderName, du);
            default:
                return false;
        }
    }
    
    /**
     * Get list of valid folder names for a deployment unit
     */
    public List<String> getValidFolders(String duName) {
        Optional<DeploymentUnit> duOpt = repository.findByName(duName);
        if (!duOpt.isPresent()) {
            return Collections.emptyList();
        }
        
        DeploymentUnit du = duOpt.get();
        DeploymentUnit.DeploymentUnitType duType = du.getType();
        
        switch (duType) {
            case DU_ONLINE:
                return Arrays.asList("dto", "transactions", "library");
            case DU_LIB:
                return getLibFolderNames(du);
            default:
                return Collections.emptyList();
        }
    }
    
    /**
     * Get error message for invalid folder navigation
     */
    public String getInvalidFolderErrorMessage(String duName) {
        List<String> validFolders = getValidFolders(duName);
        return "Invalid folder. Valid folders: " + String.join(", ", validFolders);
    }
    
    /**
     * Map user folder name to internal folder type for DU-LIB
     */
    public String mapToInternalFolderType(String duName, String userFolderName) {
        DeploymentUnit.DeploymentUnitType duType = getTypeWithCache(duName);
        
        if (duType == DeploymentUnit.DeploymentUnitType.DU_LIB) {
            // For DU-LIB: detect by IMPL suffix
            return userFolderName.endsWith("IMPL") ? "impl" : "base";
        }
        
        // For DU-ONLINE: direct mapping
        return userFolderName.toLowerCase();
    }
    
    /**
     * Clear cache for a specific deployment unit (useful for testing or updates)
     */
    public void clearCache(String duName) {
        typeCache.remove(duName);
    }
    
    /**
     * Clear entire cache
     */
    public void clearAllCache() {
        typeCache.clear();
    }
    
    // ========== PRIVATE METHODS ==========
    
    private DeploymentUnit.DeploymentUnitType queryDatabaseForType(String duName) {
        return repository.findByName(duName)
            .map(DeploymentUnit::getType)
            .orElse(null);
    }
    
    private boolean isValidOnlineFolder(String folderName) {
        return "dto".equals(folderName) || 
               "transactions".equals(folderName) || 
               "library".equals(folderName);
    }
    
    private boolean isValidLibFolder(String folderName, DeploymentUnit du) {
        List<String> validNames = getLibFolderNames(du);
        return validNames.contains(folderName);
    }
    
    private List<String> getLibFolderNames(DeploymentUnit du) {
        // Generate names using same logic as DuLibStrategy
        // Format: UUAA + "R" + code (e.g., UUAAR002)
        String baseName = du.getUuaa() + "R" + du.getCode();
        String implName = baseName + "IMPL";
        
        return Arrays.asList(baseName, implName);
    }
}