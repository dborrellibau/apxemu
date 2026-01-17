package com.bank.education.apxcli.service.dependencies;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.navigation.PathNavigationService;
import com.bank.education.apxcli.navigation.model.NavigationPath;
import com.bank.education.apxcli.navigation.model.PathType;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import com.bank.education.apxcli.service.validation.ArtifactIdValidationService;
import com.bank.education.apxcli.service.validation.MenuValidationService;
import com.bank.education.apxcli.util.ConfirmationMessages;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for handling "apx add dep" interactive command flow
 * Supports dependency creation from navigation levels 1, 2, and 3
 */
@Service
public class DependencyCommandService {

    private final MenuValidationService menuValidationService;

    private final ArchitectureOrchestrationService architectureService;
    private final DependencyManagementService dependencyManagementService;
    private final DeploymentUnitRepository deploymentUnitRepository;
    private final ArtifactIdValidationService validationService;
    private final PathNavigationService pathNavigationService;

    public DependencyCommandService(
            ArchitectureOrchestrationService architectureService,
            DependencyManagementService dependencyManagementService,
            DeploymentUnitRepository deploymentUnitRepository,
            ArtifactIdValidationService validationService,
            PathNavigationService pathNavigationService, MenuValidationService menuValidationService) {
        this.architectureService = architectureService;
        this.dependencyManagementService = dependencyManagementService;
        this.deploymentUnitRepository = deploymentUnitRepository;
        this.validationService = validationService;
        this.pathNavigationService = pathNavigationService;
        this.menuValidationService = menuValidationService;
    }

    /**
     * Main entry point for "apx add dep" command
     * ONLY allowed from component level (COMPONENT_IN_FOLDER, COMPONENT_IN_DULIB,
     * COMPONENT_STANDALONE)
     * 
     * Must be inside a specific component to create a dependency
     */
    public CommandResponse handleAddDepCommand(FormState sessionState) {
        String currentDir = sessionState.getCurrentDirectory();

        // Get PathType and NavigationPath
        PathType pathType = pathNavigationService.resolvePathType(currentDir);
        NavigationPath path = pathNavigationService.createPath(currentDir);

        // Only allow from component level
        if (pathType.canCreateDependency()) {
            // Level 3: component - start dependency flow
            String componentName = path.getComponentName();
            return startDependencyFlow(sessionState, componentName);
        } else {
            return CommandResponse.error("Command 'apx add dep' can only be executed from within a component.");
        }
    }
    /**
     * Detects component type from path and shows dependency type menu
     */
    public CommandResponse startDependencyFlow(FormState sessionState, String componentName) {
        // Buscar el componente en el repositorio
        java.util.Optional<DeploymentUnit> duOpt = deploymentUnitRepository.findByName(componentName);
        if (!duOpt.isPresent()) {
            sessionState.clearDependencyFlowData();
            return CommandResponse.error("Component '" + componentName + "' not found");
        }
        DeploymentUnit du = duOpt.get();

        // Detectar el tipo usando el campo 'type' de DeploymentUnit
        DeploymentUnit.DeploymentUnitType duType = du.getType();

        String detectedType = duType != null ? duType.getValue() : null;

        if (detectedType == null) {
            sessionState.clearDependencyFlowData();
            return CommandResponse
                    .error("Cannot detect component type from '" + componentName + "'. Invalid artifact ID format.");
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
        menu.append("Select dependency type for ").append(componentName).append(" (").append(detectedType)
                .append("):\n");
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

        String selectedType = null;

        if (!menuValidationService.isValidAddDepSelection(input, sourceType)) {
            return CommandResponse.error("Invalid selection '" + input + "'. Please enter a valid type name or number.");
        }

        selectedType = menuValidationService.getAddDepSourceTypeForSelection(input);

        if (selectedType == null) {
            return CommandResponse.error("Invalid type '" + input + "'. Please enter a valid type name or number.");
        }

        // Type selected successfully
        sessionState.setAwaitingDependencyTypeSelection(false);
        sessionState.setAwaitingDependencyArtifactId(true);
        sessionState.addData("depTargetType", selectedType);

        return CommandResponse.info("Enter artifact ID of the dependency (" + selectedType + "):");
    }

    /**
     * Handles user input of artifact ID - now shows confirmation before creating
     */
    public CommandResponse handleArtifactIdInput(FormState sessionState, String artifactId) {
        String sourceComponent = sessionState.getData("depSourceComponent");
        String targetType = sessionState.getData("depTargetType");

        if (sourceComponent == null || targetType == null) {
            sessionState.clearDependencyFlowData();
            return CommandResponse.error("Session error: missing dependency data. Please try again.");
        }

        artifactId = artifactId.trim();

        // Buscar el componente destino en el repositorio
        java.util.Optional<DeploymentUnit> targetOpt = deploymentUnitRepository.findByName(artifactId);
        if (!targetOpt.isPresent()) {
            return CommandResponse.error("Component '" + artifactId + "' not found.");
        }
        DeploymentUnit targetDU = targetOpt.get();
        DeploymentUnit.DeploymentUnitType actualTargetType = targetDU.getType();
        String actualTargetTypeValue = actualTargetType != null ? actualTargetType.getValue() : null;

        // Validar que el tipo real coincide con el tipo esperado
        if (actualTargetTypeValue == null || !actualTargetTypeValue.equalsIgnoreCase(targetType)) {
            return CommandResponse.error("Component '" + artifactId + "' is not of type '" + targetType + "'.");
        }

        // Store pending dependency data for confirmation
        sessionState.addData("pendingDep_source", sourceComponent);
        sessionState.addData("pendingDep_target", artifactId);

        // Set confirmation flag
        sessionState.setAwaitingConfirmationFor("create-dep-" + sourceComponent + "-" + artifactId);

        // Return confirmation prompt
        return CommandResponse.info(ConfirmationMessages.STANDARD_CONFIRMATION);
    }

    /**
     * Execute confirmed dependency creation after user confirms
     * 
     * @param action       the action string from confirmation
     * @param sessionState session state containing pending data
     * @return result of create operation
     */
    public CommandResponse executeConfirmedDependencyCreate(String action, FormState sessionState) {
        String sourceComponent = sessionState.getData("pendingDep_source");
        String targetArtifactId = sessionState.getData("pendingDep_target");

        // Clear all dependency flow data
        sessionState.clearDependencyFlowData();
        sessionState.getFormData().remove("pendingDep_source");
        sessionState.getFormData().remove("pendingDep_target");

        if (sourceComponent == null || targetArtifactId == null) {
            return CommandResponse.error("Session error: missing dependency data");
        }

        // Create the dependency
        return dependencyManagementService.createDependency(sourceComponent, targetArtifactId);
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
            case "dto":
            case "lib":
                // LIB -> DTO
                allowedTypes.add("DTO");
                break;
            case "lib_impl":
            case "trx":
                // TRX -> LIB, DTO
                allowedTypes.add("DTO");
                allowedTypes.add("LIB");
                break;
            default:
                // Unknown type, return empty list
                break;
        }

        return allowedTypes;
    }

}
