package com.bank.education.apxcli.service.dependencies;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import com.bank.education.apxcli.service.validation.ArtifactIdValidationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for handling "apx add dep" interactive command flow
 * Supports dependency creation from navigation levels 1, 2, and 3
 */
@Service
public class DependencyCommandService {
    
    private final ArchitectureOrchestrationService architectureService;
    private final DependencyManagementService dependencyManagementService;
    private final DeploymentUnitRepository deploymentUnitRepository;
    private final ArtifactIdValidationService validationService;
    
    public DependencyCommandService(
            ArchitectureOrchestrationService architectureService,
            DependencyManagementService dependencyManagementService,
            DeploymentUnitRepository deploymentUnitRepository,
            ArtifactIdValidationService validationService) {
        this.architectureService = architectureService;
        this.dependencyManagementService = dependencyManagementService;
        this.deploymentUnitRepository = deploymentUnitRepository;
        this.validationService = validationService;
    }
    
    /**
     * Main entry point for "apx add dep" command
     * Detects navigation level and starts appropriate flow
     * 
     * Level 0 (root): Error - must be inside a DU
     * Level 1 (du-name): Show component selection
     * Level 2 (du-name/folder): Show component selection
     * Level 3 (du-name/folder/component): Start dependency type selection
     */
    public CommandResponse handleAddDepCommand(FormState sessionState) {
        String currentDir = sessionState.getCurrentDirectory();
        
        // Level 0: root - cannot create dependency from root
        if ("root".equals(currentDir)) {
            return CommandResponse.error("Cannot create dependency from root. Navigate to a deployment unit first (cd <du-name>)");
        }
        
        // Parse navigation level
        String[] pathParts = currentDir.split("/");
        int level = pathParts.length;
        
        if (level == 1) {
            // Level 1: cd du-name
            return showSourceComponentSelection(sessionState, pathParts[0]);
        } else if (level == 2) {
            // Level 2: cd du-name/folder
            return showSourceComponentSelection(sessionState, pathParts[0]);
        } else if (level == 3) {
            // Level 3: cd du-name/folder/component
            return startDependencyFlow(sessionState, pathParts[2]);
        } else {
            return CommandResponse.error("Invalid navigation level for dependency creation");
        }
    }
    
    /**
     * Shows component selection menu for levels 1 and 2
     * User must select which component will be the source of the dependency
     */
    public CommandResponse showSourceComponentSelection(FormState sessionState, String duName) {
        // TODO: Implement in ETAPA 5
        return CommandResponse.error("Component selection not yet implemented (ETAPA 5)");
    }
    
    /**
     * Handles user input when selecting source component (levels 1-2)
     */
    public CommandResponse handleSourceComponentInput(FormState sessionState, String input) {
        // TODO: Implement in ETAPA 5
        return CommandResponse.error("Component selection handler not yet implemented (ETAPA 5)");
    }
    
    /**
     * Starts dependency flow from level 3 (already at component level)
     * Detects component type from path and shows dependency type menu
     */
    public CommandResponse startDependencyFlow(FormState sessionState, String componentName) {
        // TODO: Implement in ETAPA 6
        return CommandResponse.error("Level 3 flow not yet implemented (ETAPA 6)");
    }
    
    /**
     * Handles user selection of dependency type from menu
     */
    public CommandResponse handleDependencyTypeSelection(FormState sessionState, String input) {
        // TODO: Implement in ETAPA 7
        return CommandResponse.error("Type selection not yet implemented (ETAPA 7)");
    }
    
    /**
     * Handles user input of artifact ID and creates the dependency
     */
    public CommandResponse handleArtifactIdInput(FormState sessionState, String artifactId) {
        // TODO: Implement in ETAPA 8
        return CommandResponse.error("Artifact ID input not yet implemented (ETAPA 8)");
    }
    
    /**
     * Gets list of allowed dependency types based on source component type
     */
    private List<String> getAllowedDependencyTypes(String sourceType) {
        // TODO: Implement in ETAPA 7
        return null;
    }
}
