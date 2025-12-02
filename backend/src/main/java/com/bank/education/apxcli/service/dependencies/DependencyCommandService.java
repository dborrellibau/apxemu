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
        // Verify component exists
        if (!deploymentUnitRepository.findByName(componentName).isPresent()) {
            sessionState.clearDependencyFlowData();
            return CommandResponse.error("Component '" + componentName + "' not found");
        }
        
        // Detect type from artifact ID using validation service
        String detectedType = validationService.detectTypeFromArtifactId(componentName);
        
        if (detectedType == null) {
            sessionState.clearDependencyFlowData();
            return CommandResponse.error("Cannot detect component type from '" + componentName + "'. Invalid artifact ID format.");
        }
        
        // Store source component info
        sessionState.addData("depSourceComponent", componentName);
        sessionState.addData("depSourceType", detectedType);
        
        // Get allowed dependency types for this source type
        List<String> allowedTypes = getAllowedDependencyTypes(detectedType);
        
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            sessionState.clearDependencyFlowData();
            return CommandResponse.error("No valid dependency types available for " + detectedType + " components");
        }
        
        // Build menu
        StringBuilder menu = new StringBuilder();
        menu.append("Select dependency type for ").append(componentName).append(" (").append(detectedType).append("):\n");
        for (int i = 0; i < allowedTypes.size(); i++) {
            menu.append((i + 1)).append(". ").append(allowedTypes.get(i)).append("\n");
        }
        menu.append("\nEnter type number or name:");
        
        // Set flag
        sessionState.setAwaitingDependencyTypeSelection(true);
        
        return CommandResponse.info(menu.toString());
    }
    
    /**
     * Handles user selection of dependency type from menu
     */
    public CommandResponse handleDependencyTypeSelection(FormState sessionState, String input) {
        String sourceType = sessionState.getData("depSourceType");
        if (sourceType == null) {
            sessionState.clearDependencyFlowData();
            return CommandResponse.error("Session error: source type not found. Please try again.");
        }
        
        // Get allowed types
        List<String> allowedTypes = getAllowedDependencyTypes(sourceType);
        
        String selectedType = null;
        
        // Check if input is a number (menu selection)
        if (input.matches("^\\d+$")) {
            int selection = Integer.parseInt(input);
            if (selection < 1 || selection > allowedTypes.size()) {
                return CommandResponse.error("Invalid selection. Please enter a number between 1 and " + allowedTypes.size());
            }
            selectedType = allowedTypes.get(selection - 1);
        } else {
            // Check if input matches a type name (case insensitive)
            for (String type : allowedTypes) {
                if (type.equalsIgnoreCase(input)) {
                    selectedType = type;
                    break;
                }
            }
            
            if (selectedType == null) {
                return CommandResponse.error("Invalid type '" + input + "'. Please enter a valid type name or number.");
            }
        }
        
        // Type selected successfully
        sessionState.setAwaitingDependencyTypeSelection(false);
        sessionState.setAwaitingDependencyArtifactId(true);
        sessionState.addData("depTargetType", selectedType);
        
        return CommandResponse.info("Enter artifact ID of the dependency (" + selectedType + "):");
    }
    
    /**
     * Handles user input of artifact ID and creates the dependency
     */
    public CommandResponse handleArtifactIdInput(FormState sessionState, String artifactId) {
        String sourceComponent = sessionState.getData("depSourceComponent");
        String targetType = sessionState.getData("depTargetType");
        
        if (sourceComponent == null || targetType == null) {
            sessionState.clearDependencyFlowData();
            return CommandResponse.error("Session error: missing dependency data. Please try again.");
        }
        
        artifactId = artifactId.trim();
        
        // Validation 1: Validate artifact ID format matches expected type
        ArtifactIdValidationService.ValidationResult validationResult = 
            validationService.validateArtifactId(artifactId, targetType);
        
        if (!validationResult.isSuccess()) {
            return CommandResponse.error(validationResult.getErrorMessage());
        }
        
        // Validation 2: Check if it's a LIB_IMPL (not allowed as dependency target)
        if (validationService.isLibImpl(artifactId)) {
            sessionState.clearDependencyFlowData();
            return CommandResponse.error("LIB_IMPL components cannot be used as dependency targets. Use the base LIB instead.");
        }
        
        // All specific validations passed - delegate to DependencyManagementService
        // (it will handle existence checks, duplicates, and circular dependency validation)
        CommandResponse response = dependencyManagementService.createDependency(sourceComponent, artifactId);
        
        // Clear dependency flow data regardless of success/failure
        sessionState.clearDependencyFlowData();
        
        return response;
    }
    
    /**
     * Gets list of allowed dependency types based on source component type
     * 
     * Dependency rules:
     * - DTO can depend on: DTO
     * - LIB can depend on: DTO
     * - LIB_IMPL can depend on: LIB, DTO
     * - TRX can depend on: LIB, DTO
     */
    private List<String> getAllowedDependencyTypes(String sourceType) {
        List<String> allowedTypes = new java.util.ArrayList<>();
        
        switch (sourceType) {
            case ArtifactIdValidationService.TYPE_DTO:
                // DTO -> DTO
                allowedTypes.add(ArtifactIdValidationService.TYPE_DTO);
                break;
                
            case ArtifactIdValidationService.TYPE_LIB:
                // LIB -> DTO
                allowedTypes.add(ArtifactIdValidationService.TYPE_DTO);
                break;
                
            case ArtifactIdValidationService.TYPE_LIB_IMPL:
                // LIB_IMPL -> LIB, DTO
                allowedTypes.add(ArtifactIdValidationService.TYPE_LIB);
                allowedTypes.add(ArtifactIdValidationService.TYPE_DTO);
                break;
                
            case ArtifactIdValidationService.TYPE_TRX:
                // TRX -> LIB, DTO
                allowedTypes.add(ArtifactIdValidationService.TYPE_LIB);
                allowedTypes.add(ArtifactIdValidationService.TYPE_DTO);
                break;
                
            default:
                // Unknown type, return empty list
                break;
        }
        
        return allowedTypes;
    }
}
