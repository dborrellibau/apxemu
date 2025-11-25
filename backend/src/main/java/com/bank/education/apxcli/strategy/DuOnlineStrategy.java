package com.bank.education.apxcli.strategy;

import com.bank.education.apxcli.model.ComponentFolder;

import java.util.Arrays;
import java.util.List;

/**
 * Strategy for DU-ONLINE deployment units
 * Contains: library, transactions, dto folders
 */
public class DuOnlineStrategy extends ContainerDeploymentUnitStrategy {
    
    @Override
    public List<ComponentFolder.FolderType> getSupportedFolderTypes() {
        return Arrays.asList(
            ComponentFolder.FolderType.LIBRARY,
            ComponentFolder.FolderType.TRANSACTIONS,
            ComponentFolder.FolderType.DTO
        );
    }
    
    @Override
    public String getDescription() {
        return "Deployment Unit Online - Main service container with library, transactions, and dto folders";
    }
    
    @Override
    public List<String> getFormPrompts() {
        return Arrays.asList(
            "Enter Application (UUAA) - 4 uppercase letters:",
            "Enter Deployment Unit name (no spaces):",
            "Enter Description:"
        );
    }
}