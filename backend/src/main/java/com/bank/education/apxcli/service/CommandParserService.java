package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.service.forms.ComponentSelectionService;
import com.bank.education.apxcli.service.forms.FormInputService;
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
    private final InfoCommandService infoCommandService;
    private final SystemCommandService systemCommandService;
    private final ArchitectureOrchestrationService architectureService;
    private final DeploymentUnitNavigationService directoryNavigationService;
    
    private final Map<String, FormState> activeSessions = new ConcurrentHashMap<>();
    
    public CommandParserService(NavigationCommandService navigationService,
                               ComponentSelectionService componentSelectionService,
                               FormInputService formInputService,
                               InfoCommandService infoCommandService,
                               SystemCommandService systemCommandService,
                               ArchitectureOrchestrationService architectureService,
                               DeploymentUnitNavigationService directoryNavigationService) {
        this.navigationService = navigationService;
        this.componentSelectionService = componentSelectionService;
        this.formInputService = formInputService;
        this.infoCommandService = infoCommandService;
        this.systemCommandService = systemCommandService;
        this.architectureService = architectureService;
        this.directoryNavigationService = directoryNavigationService;
        
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
        
        // Check if user is waiting for component selection after apx add
        if (sessionState.isAwaitingComponentSelection()) {
            return componentSelectionService.handleComponentSelection(sessionId, originalInput, sessionState);
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
        if (command.matches("^(\\d+|du-online|du-lib|dto|lib|trx)$")) {
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
                    default: 
                        CommandResponse errorResponse = CommandResponse.error("Invalid selection. Please choose 1-5.");
                        errorResponse.setPrompt(sessionState.getCurrentPrompt());
                        return errorResponse;
                }
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
            return CommandResponse.error("Usage: apx <command>. Type 'apx help' for available commands.");
        }
        
        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        
        switch (subCommand) {
            case "help":
                return showHelp();
            case "init":
                return systemCommandService.handleInitCommand(subArgs);
            case "add":
                return handleAddCommand(sessionId, sessionState, subArgs);
            case "list":
                return infoCommandService.handleListCommand(subArgs);
            case "dep":
                return handleDepCommand(subArgs);
            case "show":
                return infoCommandService.handleShowCommand(subArgs);
            case "debug-du":
                return infoCommandService.handleDebugDuCommand(subArgs);
            case "reset":
                return systemCommandService.handleResetSessionCommand(sessionId, activeSessions);
            case "reset-all":
                return systemCommandService.handleResetAllSessionsCommand(activeSessions);
            case "debug":
                return infoCommandService.handleDebugSessionsCommand(activeSessions);
            case "test":
                return CommandResponse.success("Test command works! Args: " + String.join(", ", subArgs));
            default:
                return CommandResponse.error("Unknown apx command: " + subCommand + ". Type 'apx help' for available commands.");
        }
    }
    
    private CommandResponse handleAddCommand(String sessionId, FormState sessionState, String[] args) {
        String currentDir = sessionState.getCurrentDirectory();
        
        // Block apx add in root directory
        if ("root".equals(currentDir)) {
            return CommandResponse.error("Cannot use 'apx add' in root directory. Navigate to a deployment unit first using 'cd <du-name>'");
        }
        
        String[] pathParts = currentDir.split("/");
        
        // Block apx add in folders (level 2) - only allowed at DU level (level 1)
        if (pathParts.length > 1) {
            return CommandResponse.error("Cannot use 'apx add' here. Navigate to the deployment unit level to add components.");
        }
        
        String duName = pathParts[0];
        
        // Verify the DU exists
        if (!architectureService.deploymentUnitExists(duName)) {
            return CommandResponse.error("Deployment unit '" + duName + "' does not exist");
        }
        
        // Get DU type to check if it's DU-LIB (not allowed)
        DeploymentUnit.DeploymentUnitType duType = directoryNavigationService.getTypeWithCache(duName);
        if (duType == DeploymentUnit.DeploymentUnitType.DU_LIB) {
            return CommandResponse.error("Cannot add components to DU-LIB deployment units");
        }
        
        // Set flag to indicate we're awaiting component selection
        sessionState.setAwaitingComponentSelection(true);
        
        // Show menu for component type selection
        return CommandResponse.menu(
            "Select component type:",
            Arrays.asList(
                "1. DTO (Data Transfer Objects)",
                "2. Transaction (Business Transaction)",
                "3. Library (Library Components)"
            )
        );
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
            "apx list [type]          - List deployment units",
            "apx dep <source> <target> - Create dependency between units",
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
            "apx list du-online       - List online deployment units",
            "apx dep customer-service account-service - Create dependency",
            "apx show customer-service - Show DU details"
        );
        
        return new CommandResponse(true, "Help", helpText, CommandResponse.ResponseType.INFO, null);
    }
}
