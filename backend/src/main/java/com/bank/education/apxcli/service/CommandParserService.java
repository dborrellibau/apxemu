package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.navigation.PathNavigationService;
import com.bank.education.apxcli.navigation.model.NavigationPath;
import com.bank.education.apxcli.navigation.model.PathType;
import com.bank.education.apxcli.navigation.permission.CommandPermissionService;
import com.bank.education.apxcli.service.forms.ComponentSelectionService;
import com.bank.education.apxcli.service.forms.FormInputService;
import com.bank.education.apxcli.service.forms.FormProcessingService;
import com.bank.education.apxcli.service.info.InfoCommandService;
import com.bank.education.apxcli.service.navigation.NavigationCommandService;
import com.bank.education.apxcli.service.system.SystemCommandService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main command parser orchestrator - delegates to specialized services
 * Maintains session state and routes commands to appropriate handlers
 */
@Service
public class CommandParserService {
    
    private final NavigationCommandService navigationService;
    private final ComponentSelectionService componentSelectionService;
    private final FormInputService formInputService;
    private final FormProcessingService formProcessingService;
    private final InfoCommandService infoCommandService;
    private final SystemCommandService systemCommandService;
    private final ArchitectureOrchestrationService architectureService;
    private final DeploymentUnitNavigationService directoryNavigationService;
    private final com.bank.education.apxcli.service.dependencies.DependencyCommandService dependencyCommandService;
    private final com.bank.education.apxcli.service.deletion.DeletionCommandService deletionCommandService;
    private final PathNavigationService pathNavigationService;
    private final CommandPermissionService permissionService;
    
    private final Map<String, FormState> activeSessions = new ConcurrentHashMap<>();
    
    public CommandParserService(NavigationCommandService navigationService,
                               ComponentSelectionService componentSelectionService,
                               FormInputService formInputService,
                               FormProcessingService formProcessingService,
                               InfoCommandService infoCommandService,
                               SystemCommandService systemCommandService,
                               ArchitectureOrchestrationService architectureService,
                               DeploymentUnitNavigationService directoryNavigationService,
                               com.bank.education.apxcli.service.dependencies.DependencyCommandService dependencyCommandService,
                               com.bank.education.apxcli.service.deletion.DeletionCommandService deletionCommandService,
                               PathNavigationService pathNavigationService,
                               CommandPermissionService permissionService) {
        this.navigationService = navigationService;
        this.componentSelectionService = componentSelectionService;
        this.formInputService = formInputService;
        this.formProcessingService = formProcessingService;
        this.infoCommandService = infoCommandService;
        this.systemCommandService = systemCommandService;
        this.architectureService = architectureService;
        this.directoryNavigationService = directoryNavigationService;
        this.dependencyCommandService = dependencyCommandService;
        this.deletionCommandService = deletionCommandService;
        this.pathNavigationService = pathNavigationService;
        this.permissionService = permissionService;
        
        // Share activeSessions with form services
        this.componentSelectionService.setActiveSessions(activeSessions);
        this.formInputService.setActiveSessions(activeSessions);
        
        // Clear any residual sessions on startup
        this.activeSessions.clear();
    }
    
    public CommandResponse parseCommand(CommandRequest request) {
        String originalInput = request.getCommand().trim();
        String command = originalInput.toLowerCase();
        String[] args = request.getArgs();
        String sessionId = request.getSessionId() != null ? request.getSessionId() : "default";
        
        // Get or create session state to track directory
        FormState sessionState = getOrCreateSessionState(sessionId);
        
        // PRIORITY 1: Check for pending confirmation (highest priority)
        if (sessionState.getAwaitingConfirmationFor() != null) {
            CommandResponse response = handleConfirmation(sessionState, originalInput);
            response.setPrompt(sessionState.getCurrentPrompt());
            return response;
        }
        
        // Check if user is in deletion flow
        if (sessionState.isAwaitingDeletionSelection()) {
            CommandResponse response = deletionCommandService.handleDeletionSelection(sessionState, originalInput);
            response.setPrompt(sessionState.getCurrentPrompt());
            return response;
        }
        
        // Check if user is waiting for component selection after apx add
        if (sessionState.isAwaitingComponentSelection()) {
            return componentSelectionService.handleComponentSelection(sessionId, originalInput, sessionState);
        }
        
        // Check if user is in dependency flow (ETAPA 9 - new functionality)
        if (sessionState.isAwaitingDependencySourceSelection()) {
            CommandResponse response = dependencyCommandService.handleSourceComponentInput(sessionState, originalInput);
            response.setPrompt(sessionState.getCurrentPrompt());
            return response;
        }
        if (sessionState.isAwaitingDependencyTypeSelection()) {
            CommandResponse response = dependencyCommandService.handleDependencyTypeSelection(sessionState, originalInput);
            response.setPrompt(sessionState.getCurrentPrompt());
            return response;
        }
        if (sessionState.isAwaitingDependencyArtifactId()) {
            CommandResponse response = dependencyCommandService.handleArtifactIdInput(sessionState, originalInput);
            response.setPrompt(sessionState.getCurrentPrompt());
            return response;
        }
        
        // Check if user is in an active form session
        FormState activeForm = activeSessions.get(sessionId);
        if (activeForm != null && activeForm.getFormType() != null) {
            // User is in a form, treat any input as form data
            CommandResponse response = formInputService.handleFormInput(sessionId, originalInput, activeForm, sessionState);
            response.setPrompt(sessionState.getCurrentPrompt());
            return response;
        }
        
        // Check if user selected from menu (numbers or type names)
        if (command.matches("^(\\d+|du-online|du-lib|dto|lib|trx|util|job|du-batch)$")) {
            // Clear any residual session before starting new form
            activeSessions.remove(sessionId);
            
            String formType = command;
            if (command.matches("^\\d+$")) {
                int selection = Integer.parseInt(command);
                switch (selection) {
                    case 1: formType = "du-online"; break;
                    case 2: formType = "du-lib"; break;
                    case 3: formType = "dto"; break;
                    case 4: formType = "lib"; break;
                    case 5: formType = "trx"; break;
                    case 6: formType = "util"; break;
                    case 7: formType = "job"; break;
                    case 8: formType = "du-batch"; break;
                    default: 
                        CommandResponse errorResponse = CommandResponse.error("Invalid selection. Please choose 1-8.");
                        errorResponse.setPrompt(sessionState.getCurrentPrompt());
                        return errorResponse;
                }
            }
            // Check if selected option is under construction
            if ("util".equals(formType) || "job".equals(formType) || "du-batch".equals(formType)) {
                CommandResponse response = CommandResponse.error("Esta opcion en proceso de construccion. Selecciona nuevamente una opcion valida para continuar.");
                response.setPrompt(sessionState.getCurrentPrompt());
                return response;
            }

            CommandResponse response = componentSelectionService.startFormSession(sessionId, formType, sessionState.getCurrentDirectory());
            response.setPrompt(sessionState.getCurrentPrompt());
            return response;
        }
        
        CommandResponse response;
        
        // Standard terminal commands (without apx prefix)
        if ("cd".equals(command)) {
            response = navigationService.handleCdCommand(sessionState, args);
        } else if ("pwd".equals(command)) {
            response = navigationService.handlePwdCommand(sessionState);
        } else if ("ls".equals(command)) {
            response = navigationService.handleLsCommand(sessionState, args);
        } else if ("clear".equals(command)) {
            response = systemCommandService.handleClearCommand();
        } else if ("exit".equals(command)) {
            response = systemCommandService.handleExitCommand();
        } 
        // APX-prefixed commands
        else if ("apx".equals(command)) {
            response = handleApxCommand(sessionId, sessionState, args);
        } 
        // Legacy support - suggest using apx prefix
        else if ("help".equals(command) || "init".equals(command) || "add".equals(command) || 
                 "list".equals(command) || "dep".equals(command) || "show".equals(command) ||
                 "del".equals(command) || "delete".equals(command) ||
                 "debug-du".equals(command) || "reset".equals(command) || "reset-all".equals(command) ||
                 "debug".equals(command) || "test".equals(command)) {
            response = CommandResponse.error("Command '" + command + "' requires 'apx' prefix. Use: apx " + command + 
                " (Type 'apx help' for available commands)");
        } else {
            response = CommandResponse.error("Unknown command: " + command + ". Type 'apx help' for available commands.");
        }
        
        // Set the current prompt based on session state
        response.setPrompt(sessionState.getCurrentPrompt());
        return response;
    }
    
    private CommandResponse handleApxCommand(String sessionId, FormState sessionState, String[] args) {
        if (args.length == 0) {
            CommandResponse response = CommandResponse.error("Usage: apx <command>. Type 'apx help' for available commands.");
            response.setPrompt(sessionState.getCurrentPrompt());
            return response;
        }
        
        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        
        CommandResponse response;
        
        switch (subCommand) {
            case "help":
                response = showHelp();
                break;
            case "init":
                // Validar permisos: apx init solo permitido en ROOT
                PathType currentType = getCurrentPathType(sessionState.getCurrentDirectory());
                if (!permissionService.canCreateDeploymentUnit(currentType)) {
                    response = CommandResponse.error(permissionService.getPermissionDeniedMessage("apx init", currentType));
                } else {
                    response = systemCommandService.handleInitCommand(subArgs);
                }
                break;
            case "add":
                // Check if it's "apx add dep" (ETAPA 9 - new functionality)
                if (subArgs.length > 0 && "dep".equalsIgnoreCase(subArgs[0])) {
                    // Validar permisos: apx add dep solo permitido en componentes
                    PathType currentTypeForDep = getCurrentPathType(sessionState.getCurrentDirectory());
                    if (!permissionService.canCreateDependency(currentTypeForDep)) {
                        response = CommandResponse.error(permissionService.getPermissionDeniedMessage("apx add dep", currentTypeForDep));
                    } else {
                        response = dependencyCommandService.handleAddDepCommand(sessionState);
                    }
                } else {
                    // Otherwise, normal "apx add" for components
                    response = handleAddCommand(sessionId, sessionState, subArgs);
                }
                break;
            case "del":
                // Validar permisos: apx del no permitido en ROOT
                PathType currentTypeForDel = getCurrentPathType(sessionState.getCurrentDirectory());
                if (!permissionService.canDelete(currentTypeForDel)) {
                    response = CommandResponse.error(permissionService.getPermissionDeniedMessage("apx del", currentTypeForDel));
                } else {
                    response = deletionCommandService.handleDeleteCommand(sessionState);
                }
                break;
            case "list":
                response = infoCommandService.handleListCommand(subArgs);
                break;
            case "dep":
                response = handleDepCommand(subArgs);
                break;
            case "show":
                response = infoCommandService.handleShowCommand(subArgs, sessionState);
                break;
            case "debug-du":
                response = infoCommandService.handleDebugDuCommand(subArgs);
                break;
            case "reset":
                response = systemCommandService.handleResetSessionCommand(sessionId, activeSessions);
                break;
            case "reset-all":
                response = systemCommandService.handleResetAllSessionsCommand(activeSessions);
                break;
            case "debug":
                response = infoCommandService.handleDebugSessionsCommand(activeSessions);
                break;
            case "test":
                response = CommandResponse.success("Test command works! Args: " + String.join(", ", subArgs));
                break;
            default:
                response = CommandResponse.error("Unknown apx command: " + subCommand + ". Type 'apx help' for available commands.");
                break;
        }
        
        response.setPrompt(sessionState.getCurrentPrompt());
        return response;
    }
    
    private CommandResponse handleAddCommand(String sessionId, FormState sessionState, String[] args) {
        String currentDir = sessionState.getCurrentDirectory();
        
        // Obtener PathType actual usando PathNavigationService
        PathType currentType = getCurrentPathType(currentDir);
        
        // Validar permisos usando CommandPermissionService
        if (!permissionService.canCreateComponent(currentType)) {
            CommandResponse response = CommandResponse.error(permissionService.getPermissionDeniedMessage("apx add", currentType));
            response.setPrompt(sessionState.getCurrentPrompt());
            return response;
        }
        
        String duName;
        
        // Determinar duName según el nivel usando NavigationPath
        if (currentType == PathType.DU_LIB || currentType == PathType.DU_ONLINE) {
            // Nivel 1: estamos en un DU
            duName = currentDir;
        } else if (currentType == PathType.FOLDER) {
            // Nivel 2: estamos en una carpeta, obtener duName de NavigationPath
            NavigationPath path = pathNavigationService.createPath(currentDir);
            duName = path.getDuName();
        } else {
            CommandResponse response = CommandResponse.error("Cannot use 'apx add' in current location");
            response.setPrompt(sessionState.getCurrentPrompt());
            return response;
        }
        
        // Verify the DU exists
        if (!architectureService.deploymentUnitExists(duName)) {
            CommandResponse response = CommandResponse.error("Deployment unit '" + duName + "' does not exist");
            response.setPrompt(sessionState.getCurrentPrompt());
            return response;
        }
        
        // Get DU type to check if it's DU-LIB (not allowed)
        DeploymentUnit.DeploymentUnitType duType = directoryNavigationService.getTypeWithCache(duName);
        if (duType == DeploymentUnit.DeploymentUnitType.DU_LIB) {
            CommandResponse response = CommandResponse.error("Cannot add components to DU-LIB deployment units");
            response.setPrompt(sessionState.getCurrentPrompt());
            return response;
        }
        
        // Set flag to indicate we're awaiting component selection
        sessionState.setAwaitingComponentSelection(true);
        
        // Show menu for component type selection
        CommandResponse response = CommandResponse.menu(
            "Select component type:",
            Arrays.asList(
                "1. DTO (Data Transfer Objects)",
                "2. Transaction (Business Transaction)",
                "3. Library (Library Components)"
            )
        );
        response.setPrompt(sessionState.getCurrentPrompt());
        return response;
    }
    
    private CommandResponse handleDepCommand(String[] args) {
        if (args.length < 2) {
            return CommandResponse.error("Dep command requires source and target names");
        }
        
        String sourceName = args[0];
        String targetName = args[1];
        
        return architectureService.createDependency(sourceName, targetName);
    }
    
    private FormState getOrCreateSessionState(String sessionId) {
        FormState state = activeSessions.get(sessionId);
        if (state == null) {
            state = new FormState();
            activeSessions.put(sessionId, state);
        }
        return state;
    }
    
    /**
     * Handles Y/n confirmation responses for any pending action
     * Generic handler that dispatches to appropriate service based on action prefix
     */
    private CommandResponse handleConfirmation(FormState sessionState, String input) {
        String action = sessionState.getAwaitingConfirmationFor();
        sessionState.setAwaitingConfirmationFor(null); // Clear flag immediately
        
        String inputLower = input.trim().toLowerCase();
        
        // Check for explicit cancellation
        if ("n".equals(inputLower)) {
            // Clean up any pending data
            cleanupPendingConfirmationData(sessionState);
            return CommandResponse.success("Operation cancelled");
        }
        
        // Accept confirmation: Y, y, or Enter (empty)
        if ("y".equals(inputLower) || input.trim().isEmpty()) {
            // Dispatch to appropriate service based on action prefix
            if (action.startsWith("delete-")) {
                return deletionCommandService.executeConfirmedDelete(action, sessionState);
            }
            if (action.startsWith("create-component-")) {
                return formProcessingService.executeConfirmedCreate(action, sessionState);
            }
            if (action.startsWith("create-dep-")) {
                return dependencyCommandService.executeConfirmedDependencyCreate(action, sessionState);
            }
            
            return CommandResponse.error("Unknown action: " + action);
        }
        
        // Invalid response - re-prompt
        sessionState.setAwaitingConfirmationFor(action); // Restore flag
        return CommandResponse.error("Invalid response. Enter Y to confirm, n to cancel, or press Enter to confirm.");
    }
    
    /**
     * Cleans up temporary data stored during confirmation flow
     */
    private void cleanupPendingConfirmationData(FormState sessionState) {
        // Clean up pending create data (from apx init/add)
        sessionState.getFormData().remove("pendingCreate_formType");
        sessionState.getFormData().remove("pendingCreate_uuaa");
        sessionState.getFormData().remove("pendingCreate_code");
        sessionState.getFormData().remove("pendingCreate_className");
        sessionState.getFormData().remove("pendingCreate_version");
        sessionState.getFormData().remove("pendingCreate_country");
        sessionState.getFormData().remove("pendingCreate_description");
        sessionState.getFormData().remove("pendingCreate_deploymentUnit");
        sessionState.getFormData().remove("pendingCreate_duName");
        sessionState.getFormData().remove("pendingCreate_currentDir");
        
        // Clean up pending dependency data (from apx add dep)
        sessionState.getFormData().remove("pendingDep_source");
        sessionState.getFormData().remove("pendingDep_target");
    }
    
    private CommandResponse showHelp() {
        List<String> helpText = Arrays.asList(
            "V-Ether - Available Commands:",
            "",
            "=== Standard Terminal Commands ===",
            "cd <directory>           - Navigate to deployment unit or folder",
            "cd ..                    - Go back to parent directory", 
            "cd                       - Show current directory",
            "pwd                      - Show current directory path",
            "ls                       - List contents of current directory",
            "clear                    - Clear terminal screen",
            "exit                     - Exit the terminal",
            "",
            "=== APX Commands ===",
            "apx init                 - Show interactive banking component menu",
            "apx add                  - Add component in current directory",
            "apx add dep              - Create dependency (interactive flow)",
            "apx del                  - Delete component (context-aware)",
            "apx list [type]          - List deployment units",
            "apx show <name>          - Show details of a deployment unit", 
            "apx help                 - Show this help message",
            "",
            "=== Navigation Examples ===",
            "cd customer-service      - Navigate to deployment unit",
            "cd dto                   - Navigate to dto folder (from DU)",
            "cd customer-service/dto  - Direct navigation to folder",
            "cd ..                    - Go back one level",
            "",
            "=== APX Examples ===",
            "apx init                 - Start interactive component creation",
            "apx add dep              - Create dependency (guided workflow)",
            "apx del                  - Delete component (menu varies by location)",
            "apx list du-online       - List online deployment units",
            "apx show customer-service - Show DU details"
        );
        
        return new CommandResponse(true, "Help", helpText, CommandResponse.ResponseType.INFO, null);
    }
    
    /**
     * Helper method to get PathType from currentDirectory string.
     * Converts legacy string format to PathType for permission validation.
     */
    private PathType getCurrentPathType(String currentDir) {
        if (currentDir == null || "root".equals(currentDir) || currentDir.trim().isEmpty()) {
            return PathType.ROOT;
        }
        
        NavigationPath path = pathNavigationService.createPath(currentDir);
        return path != null ? path.getType() : PathType.ROOT;
    }
}
