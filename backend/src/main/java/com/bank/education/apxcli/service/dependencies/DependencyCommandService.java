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
        // Get all components in this DU
        List<String> components = architectureService.listAllComponentsInDU(duName);
        
        if (components.isEmpty()) {
            return CommandResponse.error("No components found in deployment unit '" + duName + "'");
        }
        
        // Build menu message
        StringBuilder menu = new StringBuilder();
        menu.append("Select source component for dependency:\n");
        for (int i = 0; i < components.size(); i++) {
            menu.append((i + 1)).append(". ").append(components.get(i)).append("\n");
        }
        menu.append("\nEnter component number or name:");
        
        // Set flag and store DU name
        sessionState.setAwaitingDependencySourceSelection(true);
        sessionState.addData("depSourceDU", duName);
        
        return CommandResponse.info(menu.toString());
    }
    
    /**
     * Handles user input when selecting source component (levels 1-2)
     */
    public CommandResponse handleSourceComponentInput(FormState sessionState, String input) {
        String duName = sessionState.getData("depSourceDU");
        if (duName == null) {
            sessionState.clearDependencyFlowData();
            return CommandResponse.error("Session error: deployment unit not found. Please try again.");
        }
        
        // Get components list again
        List<String> components = architectureService.listAllComponentsInDU(duName);
        
        String selectedComponent = null;
        
        // Check if input is a number (menu selection)
        if (input.matches("^\\d+$")) {
            int selection = Integer.parseInt(input);
            if (selection < 1 || selection > components.size()) {
                return CommandResponse.error("Invalid selection. Please enter a number between 1 and " + components.size());
            }
            selectedComponent = components.get(selection - 1);
        } else {
            // Check if input matches a component name
            for (String component : components) {
                if (component.equalsIgnoreCase(input)) {
                    selectedComponent = component;
                    break;
                }
            }
            
            if (selectedComponent == null) {
                return CommandResponse.error("Component '" + input + "' not found. Please enter a valid component name or number.");
            }
        }
        
        // Component selected successfully, now start dependency flow
        sessionState.setAwaitingDependencySourceSelection(false);
        sessionState.addData("depSourceComponent", selectedComponent);
        
        return startDependencyFlow(sessionState, selectedComponent);
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
