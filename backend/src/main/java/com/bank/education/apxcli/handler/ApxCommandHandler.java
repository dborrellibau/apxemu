package com.bank.education.apxcli.handler;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.navigation.PathNavigationService;
import com.bank.education.apxcli.navigation.model.PathType;
import com.bank.education.apxcli.navigation.permission.CommandPermissionService;
import com.bank.education.apxcli.service.dependencies.DependencyCommandService;
import com.bank.education.apxcli.service.deletion.DeletionCommandService;
import com.bank.education.apxcli.service.info.InfoCommandService;
import com.bank.education.apxcli.service.inout.InOutCommandService;
import com.bank.education.apxcli.service.system.SystemCommandService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Handler for APX-prefixed commands (apx init, apx add, apx del, etc.)
 * 
 * Priority 90 - Second-to-last priority, executes only if no interactive flow is active.
 * This is the most complex handler as it routes to many different services.
 * 
 * Command Categories:
 * - Interactive wizards: init, add, add dep, add in/out, del, del in/out
 * - Information: show, debug-du, debug
 * - System: reset, reset-all, test
 * - Help: help
 * - Under development: build, check, config, etc.
 * 
 * This handler:
 * 1. Parses apx subcommands and arguments
 * 2. Validates permissions using CommandPermissionService
 * 3. Routes to appropriate services
 * 4. Sets session state flags for interactive flows
 */
@Component
public class ApxCommandHandler extends CommandHandler {
    
    public static final int PRIORITY = 90;
    
    private final InfoCommandService infoCommandService;
    private final SystemCommandService systemCommandService;
    private final DependencyCommandService dependencyCommandService;
    private final DeletionCommandService deletionCommandService;
    private final InOutCommandService inOutCommandService;
    private final PathNavigationService pathNavigationService;
    private final CommandPermissionService permissionService;
    private final Map<String, FormState> activeSessions;
    
    public ApxCommandHandler(InfoCommandService infoCommandService,
                            SystemCommandService systemCommandService,
                            DependencyCommandService dependencyCommandService,
                            DeletionCommandService deletionCommandService,
                            InOutCommandService inOutCommandService,
                            PathNavigationService pathNavigationService,
                            CommandPermissionService permissionService,
                            Map<String, FormState> activeSessions) {
        this.infoCommandService = infoCommandService;
        this.systemCommandService = systemCommandService;
        this.dependencyCommandService = dependencyCommandService;
        this.deletionCommandService = deletionCommandService;
        this.inOutCommandService = inOutCommandService;
        this.pathNavigationService = pathNavigationService;
        this.permissionService = permissionService;
        this.activeSessions = activeSessions;
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean canHandle(CommandRequest request, FormState sessionState) {
        String command = request.getCommand().toLowerCase().trim();
        return command.equals("apx");
    }
    
    @Override
    public CommandResponse handle(CommandRequest request, FormState sessionState) {
        String[] args = request.getArgs();
        
        if (args.length == 0) {
            return CommandResponse.error("Usage: apx <command>. Type 'apx help' for available commands.");
        }
        
        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        
        switch (subCommand) {
            case "help":
                return showHelp();
            
            case "init":
                return handleInit(sessionState);
            
            case "add":
                return handleAdd(sessionState, subArgs);
            
            case "del":
                return handleDel(sessionState, subArgs);
            
            case "show":
                return infoCommandService.handleShowCommand(subArgs, sessionState);
            
            case "debug-du":
                return infoCommandService.handleDebugDuCommand(subArgs);

            case "debug":
                return infoCommandService.handleDebugSessionsCommand(activeSessions);
            
            case "test":
                return CommandResponse.success("Test command works! Args: " + String.join(", ", subArgs));
            
            // Commands under development
            case "build":
            case "check":
            case "completion":
            case "config":
            case "deploy":
            case "formatter":
            case "mod":
            case "mvn":
            case "send":
            case "start":
            case "upgrade":
            case "version":
                return CommandResponse.error("This command is under development. The command 'apx " + subCommand + "' is not yet available.");
       
            default:
                return CommandResponse.error("Unknown apx command: " + subCommand + ". Type 'apx help' for available commands.");
        }
    }
    
    /**
     * Handles "apx init" - Shows 5-option menu for component creation at ROOT level
     */
    private CommandResponse handleInit(FormState sessionState) {
        // Validate permissions: apx init only allowed at ROOT
        PathType currentType = pathNavigationService.resolvePathType(sessionState.getCurrentDirectory());
        if (!permissionService.canCreateDeploymentUnit(currentType)) {
            return CommandResponse.error(permissionService.getPermissionDeniedMessage("apx init", currentType));
        }
        
        return systemCommandService.handleInitCommand(new String[0], sessionState);
    }
    
    /**
     * Handles "apx add" and "apx add dep/in/out" - Component/dependency creation
     */
    private CommandResponse handleAdd(FormState sessionState, String[] subArgs) {
        // Check for subcommands: dep, in, out
        if (subArgs.length > 0) {
            String addSubCommand = subArgs[0].toLowerCase();
            
            if ("dep".equals(addSubCommand)) {
                return dependencyCommandService.handleAddDepCommand(sessionState);
            } else if ("in".equals(addSubCommand)) {
                return inOutCommandService.handleAddIn(sessionState);
            } else if ("out".equals(addSubCommand)) {
                return inOutCommandService.handleAddOut(sessionState);
            }
            // Unknown subcommand, treat as normal apx add
        }
        
        // Normal "apx add" for components (shows 3-option menu: DTO, TRX, LIB)
        return handleAddComponent(sessionState);
    }
    
    /**
     * Handles "apx add dep" - Dependency creation wizard
     */

    
    /**
     * Handles "apx add" (component creation) - Shows 3-option menu
     */
    private CommandResponse handleAddComponent(FormState sessionState) {
        String currentDir = sessionState.getCurrentDirectory();
        PathType currentType = pathNavigationService.resolvePathType(currentDir);
        
        // Validate permissions
        if (!permissionService.canCreateComponent(currentType)) {
            return CommandResponse.error(permissionService.getPermissionDeniedMessage("apx add", currentType));
        }
        
        // Set flag to indicate we're awaiting component selection
        sessionState.setAwaitingComponentSelection(true);
        
        // Show menu for component type selection
        return CommandResponse.menu(
            "Select component type:",
            Arrays.asList(
                "1. DTO (Data Transfer Objects)",
                "2. Library (Library Components)",
                "3. Transaction (Business Transaction)"
            )
        );
    }
    
    /**
     * Handles "apx del" and "apx del in/out" - Component/input/output deletion
     */
    private CommandResponse handleDel(FormState sessionState, String[] subArgs) {
        // Check for subcommands: "del in" or "del out"
        if (subArgs.length > 0) {
            String delSubCommand = subArgs[0].toLowerCase();
            
            if ("in".equals(delSubCommand)) {
                return handleDelIn(sessionState);
            } else if ("out".equals(delSubCommand)) {
                return handleDelOut(sessionState);
            }
        }
        
        // Normal "apx del" (component deletion)
        PathType currentType = pathNavigationService.resolvePathType(sessionState.getCurrentDirectory());
        if (!permissionService.canDelete(currentType)) {
            return CommandResponse.error(permissionService.getPermissionDeniedMessage("apx del", currentType));
        }
        
        return deletionCommandService.handleDeleteCommand(sessionState);
    }
    
    /**
     * Handles "apx del in" - Delete transaction input
     */
    private CommandResponse handleDelIn(FormState sessionState) {
        PathType pathType = pathNavigationService.resolvePathType(sessionState.getCurrentDirectory());
        if (pathType != PathType.COMPONENT_IN_FOLDER && 
            pathType != PathType.COMPONENT_IN_DULIB && 
            pathType != PathType.COMPONENT_STANDALONE) {
            return CommandResponse.error("The 'del in' command can only be executed from a component");
        }
        
        return deletionCommandService.handleDeleteIn(sessionState);
    }
    
    /**
     * Handles "apx del out" - Delete transaction output
     */
    private CommandResponse handleDelOut(FormState sessionState) {
        PathType pathType = pathNavigationService.resolvePathType(sessionState.getCurrentDirectory());
        if (pathType != PathType.COMPONENT_IN_FOLDER && 
            pathType != PathType.COMPONENT_IN_DULIB && 
            pathType != PathType.COMPONENT_STANDALONE) {
            return CommandResponse.error("The 'del out' command can only be executed from a component");
        }
        
        return deletionCommandService.handleDeleteOut(sessionState);
    }
    
    /**
     * Returns help text with all available commands
     */
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
            "reset                    - Clean current session",
            "",
            "=== APX Commands ===",
            "apx init                 - Show interactive banking component menu",
            "apx add                  - Add component in current directory",
            "apx add dep              - Create dependency (interactive flow)",
            "apx del                  - Delete component (context-aware)",
            "apx del dep              - Delete dependency",
            "apx show <name>          - Show details of a deployment unit",
            "apx build                - Build transaction artifacts",
            "apx check                - Check the artifact",
            "apx completion           - Generate shell autocompletion script",
            "apx config               - APX CLI configuration",
            "apx deploy               - Deploy artifacts to local APX environment",
            "apx formatter            - Format descriptor to XML",
            "apx mod                  - Modify input or output configuration",
            "apx mvn                  - Execute Maven commands",
            "apx send                 - Send request",
            "apx start                - Start architecture",
            "apx upgrade              - Upgrade APX CLI",
            "apx version              - Show APX CLI version",
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
            "apx show customer-service - Show DU details"
        );
        
        return new CommandResponse(true, "Help", helpText, CommandResponse.ResponseType.INFO, null);
    }
    
}
