package com.bank.education.apxcli.strategy;

import com.bank.education.apxcli.model.DeploymentUnit;

import java.util.Map;
import java.util.HashMap;

/**
 * Factory for creating deployment unit strategies
 */
public class DeploymentUnitStrategyFactory {
    
    private static final Map<DeploymentUnit.DeploymentUnitType, DeploymentUnitStrategy> strategies = new HashMap<>();
    
    static {
        // Register container strategies (with folders)
        strategies.put(DeploymentUnit.DeploymentUnitType.DU_ONLINE, new DuOnlineStrategy());
        strategies.put(DeploymentUnit.DeploymentUnitType.DU_LIB, new DuLibStrategy());
        
        // Register simple strategies (without folders)
        strategies.put(DeploymentUnit.DeploymentUnitType.DTO, new DtoStrategy());
        strategies.put(DeploymentUnit.DeploymentUnitType.LIB, new LibStrategy());
        strategies.put(DeploymentUnit.DeploymentUnitType.LIB_IMPL, new LibImplStrategy());
        strategies.put(DeploymentUnit.DeploymentUnitType.TRX, new TrxStrategy());
    }
    
    /**
     * Gets the appropriate strategy for a deployment unit type
     */
    public static DeploymentUnitStrategy getStrategy(DeploymentUnit.DeploymentUnitType type) {
        DeploymentUnitStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for deployment unit type: " + type);
        }
        return strategy;
    }
    
    /**
     * Checks if a deployment unit type is supported
     */
    public static boolean isSupported(DeploymentUnit.DeploymentUnitType type) {
        return strategies.containsKey(type);
    }
    
    /**
     * Gets all supported deployment unit types
     */
    public static DeploymentUnit.DeploymentUnitType[] getSupportedTypes() {
        return strategies.keySet().toArray(new DeploymentUnit.DeploymentUnitType[0]);
    }
}