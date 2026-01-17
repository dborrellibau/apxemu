package com.bank.education.apxcli.service.deletion;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.navigation.PathNavigationService;
import com.bank.education.apxcli.navigation.model.NavigationPath;
import com.bank.education.apxcli.navigation.model.PathType;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import com.bank.education.apxcli.service.DiagramService;
import com.bank.education.apxcli.util.ConfirmationMessages;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    
    private final DeploymentUnitRepository deploymentUnitRepository;
    private final DiagramService diagramService;
    private final PathNavigationService pathNavigationService;
    
    public DeletionCommandService(
            DeploymentUnitRepository deploymentUnitRepository,
            DiagramService diagramService,
            PathNavigationService pathNavigationService) {
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
        PathType pathType = pathNavigationService.resolvePathType(currentDir);
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
     * Unified deletion menu for both DU_ONLINE and COMPONENT contexts
     * Shows same menu options but validates differently based on PathType
     * 
     * @param sessionState current session state
     * @param contextName name of DU or component
     * @param pathType current path type (DU_ONLINE or COMPONENT_*)
     */
private CommandResponse showDeletionMenu(FormState sessionState, String contextName, PathType pathType) {
    // Cabecera simple (se mostrará en azul)
    StringBuilder header = new StringBuilder();
    header.append("Seleccione el tipo de elemento a eliminar:\n");

    // Opciones (se mostrarán en amarillo, como los items de apx init)
    java.util.List<String> options = java.util.Arrays.asList(
        "1. dep     - Dependencia",
        "2. dto     - Data Transfer Object",
        "3. job     - Job",
        "4. lib     - Library",
        "5. trx     - Transaction",
        "6. util    - Utility"
    );

    // Store context based on PathType
    sessionState.addData("deletionPathType", pathType.name());
    if (pathType == PathType.DU_ONLINE) {
        sessionState.addData("deletionDU", contextName);
    } else {
        sessionState.addData("deletionComponent", contextName);
    }
    sessionState.addData("deletionStep", "type-selection");
    sessionState.setAwaitingDeletionSelection(true);

    // Usamos MENU para que el frontend pinte igual que apx init
    return CommandResponse.menu(header.toString(), options);
}
    
    /**
     * Level 2 (du-name/folder): Show component list from folder
     */
    private CommandResponse showFolderContextMenu(FormState sessionState, String duName, String folderName) {
        // Get all components in this folder
        List<DeploymentUnit> components = getComponentsInFolder(duName, folderName);
        
        if (components.isEmpty()) {
            sessionState.clearDeletionFlowData();
            return CommandResponse.error("No components found in folder '" + folderName + "'");
        }
        
        // Cabecera + instrucciones (se mostrarán en azul)
        StringBuilder header = new StringBuilder();
        header.append("Select component to delete:\n");
        
        // Opciones (se mostrarán en amarillo)
        java.util.List<String> options = new java.util.ArrayList<>();
        for (int i = 0; i < components.size(); i++) {
            DeploymentUnit component = components.get(i);
            options.add(String.format("%d. %s", i + 1, component.getName()));
        }
        
        // Store context for next step
        sessionState.addData("deletionContext", "folder-level");
        sessionState.addData("deletionDU", duName);
        sessionState.addData("deletionFolder", folderName);
        sessionState.addData("deletionStep", "component-selection");
        sessionState.addData("deletionComponentCount", String.valueOf(components.size()));
        sessionState.setAwaitingDeletionSelection(true);
        
        // Usamos MENU para que el frontend pinte igual que apx init
        return CommandResponse.menu(header.toString(), options);
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
        
        // Set confirmation flag with action string
        sessionState.setAwaitingConfirmationFor("delete-component-" + component.getId());
        
        return CommandResponse.info(ConfirmationMessages.STANDARD_CONFIRMATION);
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
        } else if ("dependency-selection".equals(deletionStep)) {
            return handleDependencySelection(sessionState, input);
        } else if ("inout-dto-input".equals(deletionStep)) {
            return handleInOutDtoInput(sessionState, input);
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
            
            // dep → show dependency list for component
            String componentName = sessionState.getData("deletionComponent");
            return showDependencyListForComponent(sessionState, componentName);
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
     * Shows numbered list of dependencies for a component
     * Used when executing "apx del" from component context and selecting "dep"
     */
    private CommandResponse showDependencyListForComponent(FormState sessionState, String componentName) {
        Optional<DeploymentUnit> componentOpt = deploymentUnitRepository.findByName(componentName);
        if (!componentOpt.isPresent()) {
            sessionState.clearDeletionFlowData();
            return CommandResponse.error("Component '" + componentName + "' not found");
        }
        
        DeploymentUnit component = componentOpt.get();
        Set<DeploymentUnit> dependencies = component.getDependencies();
        
        if (dependencies.isEmpty()) {
            sessionState.clearDeletionFlowData();
            return CommandResponse.info("Component '" + componentName + "' has no dependencies");
        }
        
        // Cabecera + instrucciones (se mostrarán en azul)
        StringBuilder header = new StringBuilder();
        header.append("Select dependency to remove:\n");
        
        // Opciones (se mostrarán en amarillo)
        List<DeploymentUnit> depList = new ArrayList<>(dependencies);
        java.util.List<String> options = new java.util.ArrayList<>();
        for (int i = 0; i < depList.size(); i++) {
            DeploymentUnit dep = depList.get(i);
            options.add(String.format("%d. %-20s [%s]", 
                i + 1, 
                dep.getName(), 
                dep.getType().getValue()
            ));
        }
        
        sessionState.addData("deletionStep", "dependency-selection");
        sessionState.addData("deletionDependencyCount", String.valueOf(depList.size()));
        
        // Usamos MENU para que el frontend pinte igual que apx init
        return CommandResponse.menu(header.toString(), options);
    }
    
    /**
     * Handles dependency selection from list
     * Uses standard confirmation system
     */
    private CommandResponse handleDependencySelection(FormState sessionState, String input) {
        String componentName = sessionState.getData("deletionComponent");
        int depCount = Integer.parseInt(sessionState.getData("deletionDependencyCount"));
        
        Optional<DeploymentUnit> componentOpt = deploymentUnitRepository.findByName(componentName);
        if (!componentOpt.isPresent()) {
            sessionState.clearDeletionFlowData();
            return CommandResponse.error("Component not found");
        }
        
        DeploymentUnit component = componentOpt.get();
        List<DeploymentUnit> dependencies = new ArrayList<>(component.getDependencies());
        
        // Parse selection (number or name)
        DeploymentUnit selectedDep = null;
        String inputTrimmed = input.trim();
        
        // Try as number first
        try {
            int selection = Integer.parseInt(inputTrimmed);
            if (selection >= 1 && selection <= depCount) {
                selectedDep = dependencies.get(selection - 1);
            }
        } catch (NumberFormatException e) {
            // Try as name
            for (DeploymentUnit dep : dependencies) {
                if (dep.getName().equalsIgnoreCase(inputTrimmed)) {
                    selectedDep = dep;
                    break;
                }
            }
        }
        
        if (selectedDep == null) {
            return CommandResponse.error("Error: The dependency '" + inputTrimmed + "' is not found");
        }
        
        // Store pending data for confirmation
        sessionState.addData("pendingDelDep_componentId", component.getId().toString());
        sessionState.addData("pendingDelDep_dependencyId", selectedDep.getId().toString());
        sessionState.addData("pendingDelDep_componentName", component.getName());
        sessionState.addData("pendingDelDep_dependencyName", selectedDep.getName());
        
        // Use standard confirmation system
        sessionState.setAwaitingConfirmationFor("delete-dependency-" + component.getId() + "-" + selectedDep.getId());
        
        // Clear deletion flow (now in confirmation)
        sessionState.clearDeletionFlowData();
        
        return CommandResponse.info(ConfirmationMessages.STANDARD_CONFIRMATION);
    }
    
    /**
     * Executes confirmed dependency deletion
     * Called by CommandParserService.handleConfirmation()
     * 
     * @param action format: "delete-dependency-{componentId}-{dependencyId}"
     * @param sessionState session state with pending data
     */
    @Transactional
    public CommandResponse executeConfirmedDependencyDelete(String action, FormState sessionState) {
        // Parse action string
        if (!action.startsWith("delete-dependency-")) {
            return CommandResponse.error("Invalid deletion action format: " + action);
        }
        
        String componentId = sessionState.getData("pendingDelDep_componentId");
        String dependencyId = sessionState.getData("pendingDelDep_dependencyId");
        String componentName = sessionState.getData("pendingDelDep_componentName");
        String dependencyName = sessionState.getData("pendingDelDep_dependencyName");
        
        if (componentId == null || dependencyId == null) {
            return CommandResponse.error("Missing dependency deletion data");
        }
        
        // Find component and dependency
        Optional<DeploymentUnit> componentOpt = deploymentUnitRepository.findById(Long.parseLong(componentId));
        Optional<DeploymentUnit> dependencyOpt = deploymentUnitRepository.findById(Long.parseLong(dependencyId));
        
        if (!componentOpt.isPresent() || !dependencyOpt.isPresent()) {
            return CommandResponse.error("Component or dependency not found");
        }
        
        DeploymentUnit component = componentOpt.get();
        DeploymentUnit dependency = dependencyOpt.get();
        
        // Remove ONLY the relationship (no soft delete of artifact)
        component.removeDependency(dependency);
        deploymentUnitRepository.save(component);
        
        // Notify diagram
        diagramService.notifyDiagramUpdate();
        
        // Clean pending data
        sessionState.getFormData().remove("pendingDelDep_componentId");
        sessionState.getFormData().remove("pendingDelDep_dependencyId");
        sessionState.getFormData().remove("pendingDelDep_componentName");
        sessionState.getFormData().remove("pendingDelDep_dependencyName");
        
        return CommandResponse.success("Dependency relationship removed successfully:\n" +
            "  Component: " + componentName + "\n" +
            "  Removed dependency: " + dependencyName);
    }
    
    /**
     * Handles "apx del in" command for removing transaction inputs
     */
    @Transactional(readOnly = true)
    public CommandResponse handleDeleteIn(FormState sessionState) {
        String currentDir = sessionState.getCurrentDirectory();
        NavigationPath path = pathNavigationService.createPath(currentDir);
        String componentName = path.getComponentName();
        
        Optional<DeploymentUnit> transactionOpt = deploymentUnitRepository.findByName(componentName);
        if (!transactionOpt.isPresent()) {
            return CommandResponse.error("Transaction not found");
        }
        
        DeploymentUnit transaction = transactionOpt.get();
        
        // Validate it's a TRX
        if (transaction.getType() != DeploymentUnit.DeploymentUnitType.TRX) {
            return CommandResponse.error("Error: The artifact is not a Transaction");
        }
        
        // Check if has inputs
        if (transaction.getInputs().isEmpty()) {
            return CommandResponse.info("Transaction '" + componentName + "' has no input DTOs");
        }
        
        // Show current inputs and request DTO name
        StringBuilder prompt = new StringBuilder();
        prompt.append("Current inputs:\n");
        for (DeploymentUnit input : transaction.getInputs()) {
            prompt.append("  - ").append(input.getName()).append("\n");
        }
        prompt.append("\n");
        prompt.append("Enter input DTO name to remove: ");
        
        // Set state
        sessionState.addData("inOutContext", "input");
        sessionState.addData("inOutTransaction", componentName);
        sessionState.addData("inOutTransactionId", transaction.getId().toString());
        sessionState.addData("deletionStep", "inout-dto-input");
        sessionState.setAwaitingDeletionSelection(true);
        
        return CommandResponse.info(prompt.toString());
    }
    
    /**
     * Handles "apx del out" command for removing transaction outputs
     */
    @Transactional(readOnly = true)
    public CommandResponse handleDeleteOut(FormState sessionState) {
        String currentDir = sessionState.getCurrentDirectory();
        NavigationPath path = pathNavigationService.createPath(currentDir);
        String componentName = path.getComponentName();
        
        Optional<DeploymentUnit> transactionOpt = deploymentUnitRepository.findByName(componentName);
        if (!transactionOpt.isPresent()) {
            return CommandResponse.error("Transaction not found");
        }
        
        DeploymentUnit transaction = transactionOpt.get();
        
        // Validate it's a TRX
        if (transaction.getType() != DeploymentUnit.DeploymentUnitType.TRX) {
            return CommandResponse.error("Error: The artifact is not a Transaction");
        }
        
        // Check if has outputs
        if (transaction.getOutputs().isEmpty()) {
            return CommandResponse.info("Transaction '" + componentName + "' has no output DTOs");
        }
        
        // Show current outputs and request DTO name
        StringBuilder prompt = new StringBuilder();
        prompt.append("Current outputs:\n");
        for (DeploymentUnit output : transaction.getOutputs()) {
            prompt.append("  - ").append(output.getName()).append("\n");
        }
        prompt.append("\n");
        prompt.append("Enter output DTO name to remove: ");
        
        // Set state
        sessionState.addData("inOutContext", "output");
        sessionState.addData("inOutTransaction", componentName);
        sessionState.addData("inOutTransactionId", transaction.getId().toString());
        sessionState.addData("deletionStep", "inout-dto-input");
        sessionState.setAwaitingDeletionSelection(true);
        
        return CommandResponse.info(prompt.toString());
    }
    
    /**
     * Handles DTO name input for del in/del out
     * Uses standard confirmation system
     */
    private CommandResponse handleInOutDtoInput(FormState sessionState, String dtoName) {
        String transactionName = sessionState.getData("inOutTransaction");
        String transactionId = sessionState.getData("inOutTransactionId");
        String context = sessionState.getData("inOutContext"); // "input" or "output"
        
        dtoName = dtoName.trim();
        
        if (dtoName.isEmpty()) {
            return CommandResponse.error("DTO name cannot be empty");
        }
        
        // Find the DTO
        Optional<DeploymentUnit> dtoOpt = deploymentUnitRepository.findByName(dtoName);
        if (!dtoOpt.isPresent()) {
            sessionState.clearDeletionFlowData();
            return CommandResponse.error("DTO '" + dtoName + "' not found");
        }
        
        DeploymentUnit dto = dtoOpt.get();
        
        // Verify DTO is in inputs/outputs list
        Optional<DeploymentUnit> transactionOpt = deploymentUnitRepository.findById(Long.parseLong(transactionId));
        if (!transactionOpt.isPresent()) {
            sessionState.clearDeletionFlowData();
            return CommandResponse.error("Transaction not found");
        }
        
        DeploymentUnit transaction = transactionOpt.get();
        boolean found = false;
        
        if ("input".equals(context)) {
            found = transaction.getInputs().contains(dto);
        } else if ("output".equals(context)) {
            found = transaction.getOutputs().contains(dto);
        }
        
        if (!found) {
            sessionState.clearDeletionFlowData();
            return CommandResponse.error("DTO '" + dtoName + "' is not in " + context + "s");
        }
        
        // Store pending data for confirmation
        sessionState.addData("pendingInOut_transactionId", transactionId);
        sessionState.addData("pendingInOut_dtoId", dto.getId().toString());
        sessionState.addData("pendingInOut_transactionName", transactionName);
        sessionState.addData("pendingInOut_dtoName", dtoName);
        sessionState.addData("pendingInOut_context", context);
        
        // Use standard confirmation system
        String actionPrefix = "input".equals(context) ? "delete-input-" : "delete-output-";
        sessionState.setAwaitingConfirmationFor(actionPrefix + transactionId + "-" + dto.getId());
        
        // Clear deletion flow
        sessionState.clearDeletionFlowData();
        
        return CommandResponse.info(ConfirmationMessages.STANDARD_CONFIRMATION);
    }
    
    /**
     * Executes confirmed input deletion
     * Called by CommandParserService.handleConfirmation()
     */
    @Transactional
    public CommandResponse executeConfirmedInputDelete(String action, FormState sessionState) {
        String transactionId = sessionState.getData("pendingInOut_transactionId");
        String dtoId = sessionState.getData("pendingInOut_dtoId");
        String transactionName = sessionState.getData("pendingInOut_transactionName");
        String dtoName = sessionState.getData("pendingInOut_dtoName");
        
        if (transactionId == null || dtoId == null) {
            return CommandResponse.error("Missing input deletion data");
        }
        
        Optional<DeploymentUnit> transactionOpt = deploymentUnitRepository.findById(Long.parseLong(transactionId));
        Optional<DeploymentUnit> dtoOpt = deploymentUnitRepository.findById(Long.parseLong(dtoId));
        
        if (!transactionOpt.isPresent() || !dtoOpt.isPresent()) {
            return CommandResponse.error("Transaction or DTO not found");
        }
        
        DeploymentUnit transaction = transactionOpt.get();
        DeploymentUnit dto = dtoOpt.get();
        
        // Remove input
        transaction.removeInput(dto);
        deploymentUnitRepository.save(transaction);
        
        // Notify diagram
        diagramService.notifyDiagramUpdate();
        
        // Clean pending data
        cleanupInOutPendingData(sessionState);
        
        return CommandResponse.success("Input removed successfully:\n" +
            "  Transaction: " + transactionName + "\n" +
            "  Removed input: " + dtoName);
    }
    
    /**
     * Executes confirmed output deletion
     * Called by CommandParserService.handleConfirmation()
     */
    @Transactional
    public CommandResponse executeConfirmedOutputDelete(String action, FormState sessionState) {
        String transactionId = sessionState.getData("pendingInOut_transactionId");
        String dtoId = sessionState.getData("pendingInOut_dtoId");
        String transactionName = sessionState.getData("pendingInOut_transactionName");
        String dtoName = sessionState.getData("pendingInOut_dtoName");
        
        if (transactionId == null || dtoId == null) {
            return CommandResponse.error("Missing output deletion data");
        }
        
        Optional<DeploymentUnit> transactionOpt = deploymentUnitRepository.findById(Long.parseLong(transactionId));
        Optional<DeploymentUnit> dtoOpt = deploymentUnitRepository.findById(Long.parseLong(dtoId));
        
        if (!transactionOpt.isPresent() || !dtoOpt.isPresent()) {
            return CommandResponse.error("Transaction or DTO not found");
        }
        
        DeploymentUnit transaction = transactionOpt.get();
        DeploymentUnit dto = dtoOpt.get();
        
        // Remove output
        transaction.removeOutput(dto);
        deploymentUnitRepository.save(transaction);
        
        // Notify diagram
        diagramService.notifyDiagramUpdate();
        
        // Clean pending data
        cleanupInOutPendingData(sessionState);
        
        return CommandResponse.success("Output removed successfully:\n" +
            "  Transaction: " + transactionName + "\n" +
            "  Removed output: " + dtoName);
    }
    
    /**
     * Helper to clean pending in/out data
     */
    private void cleanupInOutPendingData(FormState sessionState) {
        sessionState.getFormData().remove("pendingInOut_transactionId");
        sessionState.getFormData().remove("pendingInOut_dtoId");
        sessionState.getFormData().remove("pendingInOut_transactionName");
        sessionState.getFormData().remove("pendingInOut_dtoName");
        sessionState.getFormData().remove("pendingInOut_context");
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
        message.append("NOTE: If this component is referenced as a dependency elsewhere,\n");
        message.append("remember to remove those dependencies manually.");
        
        return CommandResponse.success(message.toString());
    }
}
