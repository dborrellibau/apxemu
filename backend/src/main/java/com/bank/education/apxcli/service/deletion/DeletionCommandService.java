package com.bank.education.apxcli.service.deletion;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.navigation.PathNavigationService;
import com.bank.education.apxcli.navigation.model.NavigationPath;
import com.bank.education.apxcli.navigation.model.PathType;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import com.bank.education.apxcli.service.DiagramService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for handling "apx del" interactive deletion command
 * Supports context-aware deletion from navigation levels 0-3
 * 
 * Level 0 (root): Delete DU (du-online/du-lib)
 * Level 1 (du-name): Delete folders (dto/lib/trx) or special (dep/job/util)
 * Level 2 (du-name/folder): Delete components inside folder
 * Level 3 (du-name/folder/component): Delete component directly
 */
@Service
public class DeletionCommandService {
    
    private final ArchitectureOrchestrationService architectureService;
    private final DeploymentUnitRepository deploymentUnitRepository;
    private final DiagramService diagramService;
    private final PathNavigationService pathNavigationService;
    
    public DeletionCommandService(
            ArchitectureOrchestrationService architectureService,
            DeploymentUnitRepository deploymentUnitRepository,
            DiagramService diagramService,
            PathNavigationService pathNavigationService) {
        this.architectureService = architectureService;
        this.deploymentUnitRepository = deploymentUnitRepository;
        this.diagramService = diagramService;
        this.pathNavigationService = pathNavigationService;
    }
    
    /**
     * Main entry point for "apx del" command
     * Detects navigation level and shows appropriate deletion menu
     * 
     * Only allowed from: DU_ONLINE or COMPONENT_*
     */
    public CommandResponse handleDeleteCommand(FormState sessionState) {
        String currentDir = sessionState.getCurrentDirectory();
        PathType pathType = getCurrentPathType(currentDir);
        NavigationPath path = pathNavigationService.createPath(currentDir);
        
        // VALIDATION: Only allow from DU_ONLINE or COMPONENT_*
        if (pathType == PathType.ROOT || 
            pathType == PathType.DU_LIB || 
            pathType == PathType.FOLDER) {
            return CommandResponse.error("The 'del' command can only be executed from DU-ONLINE or within a component");
        }
        
        if (pathType == PathType.DU_ONLINE) {
            // From DU_ONLINE: show menu (dto/lib/trx valid, rest error)
            String duName = path.getDuName();
            return showDeletionMenu(sessionState, duName, pathType);
        } else if (pathType == PathType.COMPONENT_IN_FOLDER || 
                   pathType == PathType.COMPONENT_IN_DULIB || 
                   pathType == PathType.COMPONENT_STANDALONE) {
            // From COMPONENT: show menu (only dep valid, rest error)
            String componentName = path.getComponentName();
            return showDeletionMenu(sessionState, componentName, pathType);
        }
        
        return CommandResponse.error("Invalid navigation level for deletion");
    }
    
    /**
     * Level 0 (root): Show DU deletion menu
     * Options: 1. du-online, 2. du-lib
     */
    private CommandResponse showRootDeletionMenu(FormState sessionState) {
        StringBuilder menu = new StringBuilder();
        menu.append("╔══════════════════════════════════════════════════════════╗\n");
        menu.append("║          ELIMINACIÓN DE DEPLOYMENT UNIT                  ║\n");
        menu.append("╚══════════════════════════════════════════════════════════╝\n");
        menu.append("\n");
        menu.append("Seleccione el tipo de deployment unit a eliminar:\n");
        menu.append("\n");
        menu.append("  1. du-online    - Online Deployment Unit\n");
        menu.append("  2. du-lib       - Library Deployment Unit\n");
        menu.append("\n");
        menu.append("Ingrese el número de opción o el tipo: ");
        
        // Set deletion context
        sessionState.addData("deletionContext", "root");
        sessionState.addData("deletionStep", "type-selection");
        sessionState.setAwaitingDeletionSelection(true);
        
        return CommandResponse.info(menu.toString());
    }
    
    /**     * Unified deletion menu for both DU_ONLINE and COMPONENT contexts
     * Shows same menu options but validates differently based on PathType
     * 
     * @param sessionState current session state
     * @param contextName name of DU or component
     * @param pathType current path type (DU_ONLINE or COMPONENT_*)
     */
    private CommandResponse showDeletionMenu(FormState sessionState, String contextName, PathType pathType) {
        StringBuilder menu = new StringBuilder();
        menu.append("╔══════════════════════════════════════════════════════════╗\n");
        menu.append("║          ELIMINACIÓN EN: ").append(String.format("%-32s", contextName)).append("║\n");
        menu.append("╚══════════════════════════════════════════════════════════╝\n");
        menu.append("\n");
        menu.append("Seleccione el tipo de elemento a eliminar:\n");
        menu.append("\n");
        menu.append("  1. dep     - Dependencia\n");
        menu.append("  2. dto     - Data Transfer Object\n");
        menu.append("  3. job     - Job\n");
        menu.append("  4. lib     - Library\n");
        menu.append("  5. trx     - Transaction\n");
        menu.append("  6. util    - Utility\n");
        menu.append("\n");
        menu.append("Ingrese el número de opción o el tipo: ");
        
        // Store context based on PathType
        sessionState.addData("deletionPathType", pathType.name());
        if (pathType == PathType.DU_ONLINE) {
            sessionState.addData("deletionDU", contextName);
        } else {
            sessionState.addData("deletionComponent", contextName);
        }
        sessionState.addData("deletionStep", "type-selection");
        sessionState.setAwaitingDeletionSelection(true);
        
        return CommandResponse.info(menu.toString());
    }
    
    /**
     * Level 2 (du-name/folder): Show component list from folder
     */
    private CommandResponse showFolderContextMenu(FormState sessionState, String duName, String folderName) {
        System.out.println("=== DEBUG showFolderContextMenu ===");
        System.out.println("duName: " + duName);
        System.out.println("folderName: " + folderName);
        
        // Get all components in this folder
        List<DeploymentUnit> components = getComponentsInFolder(duName, folderName);
        System.out.println("Components found: " + components.size());
        
        if (components.isEmpty()) {
            System.out.println("No components found - showing debug message");
            sessionState.clearDeletionFlowData(); // Clear state on error
            
            // Debug: Show what folders exist
            DeploymentUnit du = deploymentUnitRepository.findByName(duName).orElse(null);
            if (du != null) {
                StringBuilder debugMsg = new StringBuilder();
                debugMsg.append("No components found in folder '").append(folderName).append("'.\n\n");
                debugMsg.append("Available folders in ").append(duName).append(":\n");
                for (com.bank.education.apxcli.model.ComponentFolder folder : du.getComponentFolders()) {
                    debugMsg.append("  - ").append(folder.getType().name()).append(" (")
                            .append(folder.getContainedUnits().size()).append(" components)\n");
                }
                System.out.println("Debug message to send: " + debugMsg.toString());
                return CommandResponse.error(debugMsg.toString());
            }
            
            System.out.println("DU not found - returning generic error");
            return CommandResponse.error("No components found in folder '" + folderName + "'");
        }
        
        // Build selection menu
        StringBuilder menu = new StringBuilder();
        menu.append("╔══════════════════════════════════════════════════════════╗\n");
        menu.append("║          SELECCIONAR COMPONENTE A ELIMINAR               ║\n");
        menu.append("╚══════════════════════════════════════════════════════════╝\n");
        menu.append("\n");
        menu.append("Ubicación: ").append(duName).append("/").append(folderName).append("\n");
        menu.append("\n");
        
        for (int i = 0; i < components.size(); i++) {
            DeploymentUnit component = components.get(i);
            menu.append(String.format("  %d. %s", i + 1, component.getName()));
            if (component.getDescription() != null && !component.getDescription().isEmpty()) {
                menu.append(" - ").append(component.getDescription());
            }
            menu.append("\n");
        }
        
        menu.append("\n");
        menu.append("Ingrese el número o nombre del componente: ");
        
        // Store context for next step
        sessionState.addData("deletionContext", "folder-level");
        sessionState.addData("deletionDU", duName);
        sessionState.addData("deletionFolder", folderName);
        sessionState.addData("deletionStep", "component-selection");
        sessionState.addData("deletionComponentCount", String.valueOf(components.size()));
        sessionState.setAwaitingDeletionSelection(true);
        
        return CommandResponse.info(menu.toString());
    }
    
    /**
     * Level 3 (du-name/folder/component): Direct deletion confirmation
     */
    private CommandResponse showComponentDeletionConfirmation(FormState sessionState, String duName, String folderName, String componentName) {
        // Find the component
        DeploymentUnit component = deploymentUnitRepository.findByName(componentName).orElse(null);
        if (component == null) {
            return CommandResponse.error("Component '" + componentName + "' not found");
        }
        
        // Check if already deleted
        if (component.isDeleted()) {
            return CommandResponse.error("Component '" + componentName + "' is already marked as deleted");
        }
        
        // Build confirmation message
        StringBuilder message = new StringBuilder();
        message.append("╔══════════════════════════════════════════════════════════╗\n");
        message.append("║          CONFIRMACIÓN DE ELIMINACIÓN                     ║\n");
        message.append("╚══════════════════════════════════════════════════════════╝\n");
        message.append("\n");
        message.append("Componente: ").append(componentName).append("\n");
        message.append("Ubicación: ").append(duName).append("/").append(folderName).append("\n");
        message.append("Tipo: ").append(component.getType().getValue()).append("\n");
        if (component.getDescription() != null) {
            message.append("Descripción: ").append(component.getDescription()).append("\n");
        }
        message.append("\n");
        message.append("\u001B[33mNOTA: Si este elemento es dependencia de otro, recordá eliminar\n");
        message.append("      la dependencia manualmente con el comando apropiado.\u001B[0m\n");
        message.append("\n");
        message.append("¿Confirmar eliminación? (Y/n): ");
        
        // Set confirmation flag with action string
        sessionState.setAwaitingConfirmationFor("delete-component-" + component.getId());
        
        return CommandResponse.info(message.toString());
    }
    
    /**
     * ETAPA 5: Process user selection in deletion flow
     * Handles type selection, component selection, etc.
     */
    @Transactional
    public CommandResponse handleDeletionSelection(FormState sessionState, String input) {
        String deletionStep = sessionState.getData("deletionStep");
        
        if ("type-selection".equals(deletionStep)) {
            return handleTypeSelection(sessionState, input);
        } else if ("component-selection".equals(deletionStep)) {
            return handleComponentSelection(sessionState, input);
        }
        
        // Unknown step
        sessionState.clearDeletionFlowData();
        return CommandResponse.error("Unknown deletion step. Flow cancelled.");
    }
    
    /**
     * Handle type selection based on PathType context
     * Validates allowed types based on where command was executed
     */
    private CommandResponse handleTypeSelection(FormState sessionState, String input) {
        String inputLower = input.trim().toLowerCase();
        String selectedType = null;
        
        // Parse input to type
        if ("1".equals(inputLower) || "dep".equals(inputLower)) {
            selectedType = "dep";
        } else if ("2".equals(inputLower) || "dto".equals(inputLower)) {
            selectedType = "dto";
        } else if ("3".equals(inputLower) || "job".equals(inputLower)) {
            selectedType = "job";
        } else if ("4".equals(inputLower) || "lib".equals(inputLower)) {
            selectedType = "lib";
        } else if ("5".equals(inputLower) || "trx".equals(inputLower)) {
            selectedType = "trx";
        } else if ("6".equals(inputLower) || "util".equals(inputLower)) {
            selectedType = "util";
        } else {
            return CommandResponse.error("Invalid option. Please enter 1-6 or the type name (dep/dto/job/lib/trx/util)");
        }
        
        // Get PathType from context
        String pathTypeStr = sessionState.getData("deletionPathType");
        PathType pathType = PathType.valueOf(pathTypeStr);
        
        // VALIDATION BASED ON PATHTYPE
        if (pathType == PathType.DU_ONLINE) {
            // From DU_ONLINE: only dto, lib, trx are valid
            if ("dep".equals(selectedType)) {
                sessionState.clearDeletionFlowData();
                return CommandResponse.error("Dependencies cannot be managed at DU level. Navigate to a component.");
            } else if ("job".equals(selectedType) || "util".equals(selectedType)) {
                sessionState.clearDeletionFlowData();
                return CommandResponse.error("Not implemented yet");
            }
            
            // dto, lib, trx → show component list
            String duName = sessionState.getData("deletionDU");
            return showComponentListForType(sessionState, duName, selectedType);
            
        } else if (pathType == PathType.COMPONENT_IN_FOLDER || 
                   pathType == PathType.COMPONENT_IN_DULIB || 
                   pathType == PathType.COMPONENT_STANDALONE) {
            // From COMPONENT: only dep is valid
            if (!"dep".equals(selectedType)) {
                sessionState.clearDeletionFlowData();
                return CommandResponse.error("Not implemented yet");
            }
            
            // dep → show dependency list for component (TODO FASE 5)
            String componentName = sessionState.getData("deletionComponent");
            sessionState.clearDeletionFlowData();
            return CommandResponse.error("Dependency deletion from component not yet implemented");
        }
        
        sessionState.clearDeletionFlowData();
        return CommandResponse.error("Invalid context");
    }
    
    /**
     * Handle component selection from list
     */
    private CommandResponse handleComponentSelection(FormState sessionState, String input) {
        String duName = sessionState.getData("deletionDU");
        String folderName = sessionState.getData("deletionFolder");
        int componentCount = Integer.parseInt(sessionState.getData("deletionComponentCount"));
        
        // Get components list
        List<DeploymentUnit> components = getComponentsInFolder(duName, folderName);
        
        // Parse selection (number or name)
        DeploymentUnit selectedComponent = null;
        String inputTrimmed = input.trim();
        
        // Try as number first
        try {
            int selection = Integer.parseInt(inputTrimmed);
            if (selection >= 1 && selection <= componentCount) {
                selectedComponent = components.get(selection - 1);
            } else {
                return CommandResponse.error("Invalid selection. Please enter a number between 1 and " + componentCount);
            }
        } catch (NumberFormatException e) {
            // Try as name
            for (DeploymentUnit component : components) {
                if (component.getName().equalsIgnoreCase(inputTrimmed)) {
                    selectedComponent = component;
                    break;
                }
            }
            if (selectedComponent == null) {
                return CommandResponse.error("Component '" + inputTrimmed + "' not found. Please enter a valid number or component name.");
            }
        }
        
        // Clear deletion flow and show confirmation
        sessionState.clearDeletionFlowData();
        return showComponentDeletionConfirmation(sessionState, duName, folderName, selectedComponent.getName());
    }
    
    /**
     * Show list of DUs for deletion (root context)
     */
    private CommandResponse showDUListForDeletion(FormState sessionState, String duType) {
        // TODO ETAPA 5: List DUs by type
        sessionState.clearDeletionFlowData();
        return CommandResponse.error("DU listing for deletion not yet implemented");
    }
    
    /**
     * Show list of dependencies for deletion (du-level context, dep option)
     */
    private CommandResponse showDependencyListForDeletion(FormState sessionState, String duName) {
        // TODO ETAPA 5: List dependencies
        sessionState.clearDeletionFlowData();
        return CommandResponse.error("Dependency deletion not yet implemented");
    }
    
    /**
     * Show list of components by type (dto/lib/trx)
     */
    private CommandResponse showComponentListForType(FormState sessionState, String duName, String folderType) {
        // Redirect to folder context menu WITHOUT clearing state
        // The state will be managed by showFolderContextMenu
        return showFolderContextMenu(sessionState, duName, folderType);
    }
    
    /**
     * Helper: Get all components in a specific folder
     */
    private List<DeploymentUnit> getComponentsInFolder(String duName, String folderName) {
        DeploymentUnit du = deploymentUnitRepository.findByName(duName).orElse(null);
        if (du == null) {
            return new java.util.ArrayList<>();
        }
        
        // Normalize folder name to match ComponentFolder enum
        String normalizedFolderName = normalizeFolderName(folderName);
        
        // Find the folder by name
        for (com.bank.education.apxcli.model.ComponentFolder folder : du.getComponentFolders()) {
            String folderTypeName = folder.getType().name();
            if (folderTypeName.equalsIgnoreCase(normalizedFolderName)) {
                // Return non-deleted components
                return folder.getContainedUnits().stream()
                    .filter(unit -> !unit.isDeleted())
                    .collect(java.util.stream.Collectors.toList());
            }
        }
        
        return new java.util.ArrayList<>();
    }
    
    /**
     * Helper: Normalize folder name for enum matching
     * Maps user input like "dto", "lib", "trx" to ComponentFolder.FolderType enum names
     */
    private String normalizeFolderName(String folderName) {
        String lower = folderName.toLowerCase();
        switch (lower) {
            case "dto":
            case "dtos":
                return "DTO";
            case "lib":
            case "library":
            case "libs":
                return "LIBRARY";
            case "trx":
            case "transaction":
            case "transactions":
                return "TRANSACTIONS";
            default:
                return folderName.toUpperCase();
        }
    }
    
    /**
     * Helper: Get all dependencies in a DU
     */
    private List<DeploymentUnit> getDependenciesInDU(String duName) {
        DeploymentUnit du = deploymentUnitRepository.findByName(duName).orElse(null);
        if (du == null) {
            return new java.util.ArrayList<>();
        }
        
        // Return non-deleted dependencies
        return du.getDependencies().stream()
            .filter(dep -> !dep.isDeleted())
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * ETAPA 6: Execute confirmed deletion (soft delete)
     * Action format: "delete-component-123" where 123 is the component ID
     */
    public CommandResponse executeConfirmedDelete(String action, FormState sessionState) {
        // Parse action string like "delete-component-123"
        if (!action.startsWith("delete-component-")) {
            return CommandResponse.error("Invalid deletion action format: " + action);
        }
        
        String idStr = action.substring("delete-component-".length());
        Long componentId;
        try {
            componentId = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            return CommandResponse.error("Invalid component ID in action: " + action);
        }
        
        // Find the component
        DeploymentUnit component = deploymentUnitRepository.findById(componentId).orElse(null);
        if (component == null) {
            return CommandResponse.error("Component with ID " + componentId + " not found");
        }
        
        // Check if already deleted
        if (component.isDeleted()) {
            return CommandResponse.error("Component '" + component.getName() + "' is already marked as deleted");
        }
        
        // Perform soft delete
        String componentName = component.getName();
        String componentType = component.getType().getValue();
        component.setDeleted(true);
        deploymentUnitRepository.save(component);
        
        // If we're currently inside the deleted component, navigate to parent
        String currentDir = sessionState.getCurrentDirectory();
        if (currentDir != null && !"root".equals(currentDir)) {
            NavigationPath currentPath = pathNavigationService.createPath(currentDir);
            if (currentPath != null && currentPath.getType() == PathType.COMPONENT_IN_FOLDER ||
                currentPath.getType() == PathType.COMPONENT_IN_DULIB ||
                currentPath.getType() == PathType.COMPONENT_STANDALONE) {
                // Check if the current component matches the deleted one
                if (componentName.equals(currentPath.getComponentName())) {
                    // Navigate to parent (the folder containing this component)
                    String parentPath = currentPath.getDuName();
                    if (currentPath.getFolderName() != null) {
                        parentPath = parentPath + "/" + currentPath.getFolderName();
                    }
                    sessionState.setCurrentDirectory(parentPath);
                }
            }
        }
        
        // Notify frontend to update diagram
        diagramService.notifyDiagramUpdate();
        
        // Build success message
        StringBuilder message = new StringBuilder();
        message.append("✓ Component successfully marked as deleted\n");
        message.append("\n");
        message.append("Component: ").append(componentName).append("\n");
        message.append("Type: ").append(componentType).append("\n");
        message.append("\n");
        message.append("\u001B[33mNOTE: If this component is referenced as a dependency elsewhere,\n");
        message.append("remember to remove those dependencies manually.\u001B[0m");
        
        return CommandResponse.success(message.toString());
    }
    
    /**
     * Helper method to get PathType from currentDirectory string.
     * Converts legacy string format to PathType.
     */
    private PathType getCurrentPathType(String currentDir) {
        if (currentDir == null || "root".equals(currentDir) || currentDir.trim().isEmpty()) {
            return PathType.ROOT;
        }
        
        NavigationPath path = pathNavigationService.createPath(currentDir);
        return path != null ? path.getType() : PathType.ROOT;
    }
}
