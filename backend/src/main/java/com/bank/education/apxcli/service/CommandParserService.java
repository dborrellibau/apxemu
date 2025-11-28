package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.form.FormBuilder;
import com.bank.education.apxcli.form.FormField;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CommandParserService {
    
    private final ArchitectureOrchestrationService architectureService;
    private final Map<String, FormState> activeSessions = new ConcurrentHashMap<>();
    
    public CommandParserService(ArchitectureOrchestrationService architectureService) {
        this.architectureService = architectureService;
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
        
        // Check if user is in an active form session
        FormState activeForm = activeSessions.get(sessionId);
        if (activeForm != null && activeForm.getFormType() != null) {
            // User is in a form, treat any input as form data
            CommandResponse response = handleFormInput(sessionId, originalInput, activeForm);
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
            CommandResponse response = startFormSession(sessionId, formType);
            response.setPrompt(sessionState.getCurrentPrompt());
            return response;
        }
        
        CommandResponse response;
        
        // Standard terminal commands (without apx prefix)
        if ("cd".equals(command)) {
            response = handleCdCommand(sessionId, args);
        } else if ("pwd".equals(command)) {
            response = handlePwdCommand(sessionId);
        } else if ("ls".equals(command)) {
            response = handleLsCommand(sessionId, args);
        } else if ("clear".equals(command)) {
            response = handleClearCommand();
        } else if ("exit".equals(command)) {
            response = handleExitCommand();
        } 
        // APX-prefixed commands
        else if ("apx".equals(command)) {
            response = handleApxCommand(sessionId, args);
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
    
    private CommandResponse handleInitCommand(String[] args) {
        // Only allow init without arguments - force interactive menu
        if (args.length > 0) {
            return CommandResponse.error("Use 'init' without arguments to access the interactive menu.");
        }
        
        return CommandResponse.menu(
            "Select banking component type to initialize:",
            Arrays.asList(
                "1. du-online  - Deployment Unit Online (main service)",
                "2. du-lib     - Deployment Unit Library (base + impl)",
                "3. dto        - Data Transfer Object", 
                "4. lib        - Library component (creates base + impl)",
                "5. trx        - Transaction component"
            )
        );
    }
    
    private CommandResponse handleListCommand(String[] args) {
        String type = args.length > 0 ? args[0] : null;
        return architectureService.listDeploymentUnits(type);
    }
    
    private CommandResponse handleDepCommand(String[] args) {
        if (args.length < 2) {
            return CommandResponse.error("Dep command requires source and target names");
        }
        
        String sourceName = args[0];
        String targetName = args[1];
        
        return architectureService.createDependency(sourceName, targetName);
    }
    
    private CommandResponse handleShowCommand(String[] args) {
        if (args.length < 1) {
            return CommandResponse.error("Show command requires deployment unit name");
        }
        
        String name = args[0];
        return architectureService.getDeploymentUnitDetails(name);
    }
    
    private CommandResponse handleClearCommand() {
        return architectureService.clearAllDeploymentUnits();
    }
    
    private CommandResponse handleCdCommand(String sessionId, String[] args) {
        FormState sessionState = getOrCreateSessionState(sessionId);
        String currentDir = sessionState.getCurrentDirectory();
        
        if (args.length == 0) {
            // cd without arguments shows current directory
            return CommandResponse.success("Current directory: " + 
                ("root".equals(currentDir) ? "/vether" : "/vether/" + currentDir));
        }
        
        if (args.length != 1) {
            return CommandResponse.error("Usage: cd <directory>, cd .. (go back), or cd (show current directory)");
        }
        
        String target = args[0];
        
        // Handle cd .. (go back)
        if ("..".equals(target)) {
            if ("root".equals(currentDir)) {
                return CommandResponse.error("Already at root directory");
            }
            
            // If we're in a subfolder, go back to parent
            if (currentDir.contains("/")) {
                String[] parts = currentDir.split("/");
                if (parts.length == 2) {
                    // From du/folder back to root
                    sessionState.setCurrentDirectory("root");
                    return CommandResponse.success("Changed directory to /vether");
                }
                // If there were more levels, go up one level
                sessionState.setCurrentDirectory(parts[0]);
                return CommandResponse.success("Changed directory to /vether/" + parts[0]);
            } else {
                // From deployment unit back to root
                sessionState.setCurrentDirectory("root");
                return CommandResponse.success("Changed directory to /vether");
            }
        }
        
        // Handle absolute path navigation (du-name/folder)
        if (target.contains("/")) {
            String[] pathParts = target.split("/");
            
            if (pathParts.length != 2) {
                return CommandResponse.error("Invalid path format. Use: <du-name>/<folder>");
            }
            
            String duName = pathParts[0];
            String folder = pathParts[1];
            
            // Validate that the DU exists
            if (!architectureService.containableExists(duName, null)) {
                return CommandResponse.error("Deployment unit '" + duName + "' does not exist");
            }
            
            // Validate folder name
            if (!folder.equals("dto") && !folder.equals("transactions") && !folder.equals("library")) {
                return CommandResponse.error("Invalid folder. Valid folders: dto, transactions, library");
            }
            
            sessionState.setCurrentDirectory(duName + "/" + folder);
            return CommandResponse.success("Changed directory to /vether/" + target);
        }
        
        // Handle single directory navigation
        if ("root".equals(currentDir)) {
            // From root, can only navigate to existing deployment units
            if (!architectureService.containableExists(target, null)) {
                return CommandResponse.error("Deployment unit '" + target + "' does not exist. Use 'apx list' to see available deployment units.");
            }
            
            sessionState.setCurrentDirectory(target);
            return CommandResponse.success("Changed directory to /vether/" + target);
        } else if (!currentDir.contains("/")) {
            // From deployment unit, can navigate to folders
            String duName = currentDir;
            
            // Validate folder name
            if (!target.equals("dto") && !target.equals("transactions") && !target.equals("library")) {
                return CommandResponse.error("Invalid folder. Valid folders: dto, transactions, library");
            }
            
            sessionState.setCurrentDirectory(duName + "/" + target);
            return CommandResponse.success("Changed directory to /vether/" + duName + "/" + target);
        } else {
            // From folder level, cannot navigate further down
            return CommandResponse.error("Cannot navigate deeper. Use 'cd ..' to go back or provide a full path.");
        }
    }
    
    private CommandResponse handleAddCommand(String sessionId, String[] args) {
        FormState sessionState = getOrCreateSessionState(sessionId);
        String currentDir = sessionState.getCurrentDirectory();
        
        if ("root".equals(currentDir)) {
            return CommandResponse.error("Cannot use 'add' in root directory. Navigate to a folder first using 'cd <du-name>/<folder>'");
        }
        
        String[] pathParts = currentDir.split("/");
        if (pathParts.length != 2) {
            return CommandResponse.error("Invalid current directory state");
        }
        
        String duName = pathParts[0];
        String folder = pathParts[1];
        
        // Map folder to component type
        String componentType;
        switch (folder) {
            case "dto":
                componentType = "dto";
                break;
            case "transactions":
                componentType = "trx";
                break;
            case "lib":
                componentType = "lib";
                break;
            default:
                return CommandResponse.error("Invalid folder for adding components: " + folder);
        }
        
        // Start form session for the appropriate component type
        return startFormSession(sessionId, componentType);
    }
    
    private FormState getOrCreateSessionState(String sessionId) {
        FormState state = activeSessions.get(sessionId);
        if (state == null) {
            state = new FormState();
            activeSessions.put(sessionId, state);
        }
        return state;
    }
    
    private CommandResponse handleDebugDuCommand(String[] args) {
        if (args.length == 0) {
            return CommandResponse.error("Usage: debug-du <du-name>");
        }
        
        String duName = args[0];
        return architectureService.debugDeploymentUnit(duName);
    }
    
    private CommandResponse handlePwdCommand(String sessionId) {
        FormState sessionState = getOrCreateSessionState(sessionId);
        String currentDir = sessionState.getCurrentDirectory();
        String displayPath = "root".equals(currentDir) ? "/vether" : "/vether/" + currentDir;
        return CommandResponse.success(displayPath);
    }
    
    private CommandResponse handleResetSessionCommand(String sessionId) {
        activeSessions.remove(sessionId);
        return CommandResponse.success("Session reset. You can now start new forms.");
    }
    
    private CommandResponse handleResetAllSessionsCommand() {
        int cleared = activeSessions.size();
        activeSessions.clear();
        return CommandResponse.success("Cleared " + cleared + " sessions. System fully reset.");
    }
    
    private CommandResponse handleDebugSessionsCommand() {
        StringBuilder debug = new StringBuilder("Session Debug Info:\n");
        debug.append("Total active sessions: ").append(activeSessions.size()).append("\n");
        
        if (activeSessions.isEmpty()) {
            debug.append("No active sessions - system ready for new commands.");
        } else {
            for (Map.Entry<String, FormState> entry : activeSessions.entrySet()) {
                FormState state = entry.getValue();
                debug.append("Session ").append(entry.getKey())
                     .append(": type=").append(state.getFormType())
                     .append(", step=").append(state.getCurrentStep())
                     .append(", complete=").append(state.isComplete()).append("\n");
            }
        }
        
        return CommandResponse.info(debug.toString());
    }
    
    private CommandResponse startFormSession(String sessionId, String formType) {
        // Get current directory from existing session state
        FormState existingState = getOrCreateSessionState(sessionId);
        String currentDirectory = existingState.getCurrentDirectory();
        
        // Clear any existing form session but preserve directory state
        activeSessions.remove(sessionId);
        
        FormState formState = new FormState(formType);
        formState.setCurrentDirectory(currentDirectory); // Preserve directory
        activeSessions.put(sessionId, formState);
        return getNextFormPrompt(formState);
    }
    
    private CommandResponse getNextFormPrompt(FormState formState) {
        String formType = formState.getFormType();
        int step = formState.getCurrentStep();
        
        switch (formType) {
            case "dto":
                return getDtoFormPrompt(step);
            case "lib":
                return getLibFormPrompt(step);
            case "trx":
                return getTrxFormPrompt(step);
            case "du-online":
                return getDuOnlineFormPrompt(step);
            case "du-lib":
                return getDuLibFormPrompt(step);
            default:
                return CommandResponse.error("Unknown form type: " + formType);
        }
    }
    
    private CommandResponse getDtoFormPrompt(int step) {
        List<FormField> formFields = FormBuilder.createDtoForm();
        
        if (step >= 0 && step < formFields.size()) {
            FormField field = formFields.get(step);
            return CommandResponse.form(field.getPrompt(), field.getName());
        }
        return CommandResponse.error("Invalid DTO form step");
    }
    
    private CommandResponse getLibFormPrompt(int step) {
        List<FormField> formFields = FormBuilder.createLibForm();
        
        if (step >= 0 && step < formFields.size()) {
            FormField field = formFields.get(step);
            return CommandResponse.form(field.getPrompt(), field.getName());
        }
        return CommandResponse.error("Invalid LIB form step");
    }
    
    private CommandResponse getTrxFormPrompt(int step) {
        List<FormField> formFields = FormBuilder.createTrxForm();
        
        if (step >= 0 && step < formFields.size()) {
            FormField field = formFields.get(step);
            return CommandResponse.form(field.getPrompt(), field.getName());
        }
        return CommandResponse.error("Invalid TRX form step");
    }
    
    private CommandResponse getDuOnlineFormPrompt(int step) {
        List<FormField> formFields = FormBuilder.createDuOnlineForm();
        
        if (step >= 0 && step < formFields.size()) {
            FormField field = formFields.get(step);
            return CommandResponse.form(field.getPrompt(), field.getName());
        }
        return CommandResponse.error("Invalid DU-ONLINE form step");
    }
    
    private CommandResponse getDuLibFormPrompt(int step) {
        List<FormField> formFields = FormBuilder.createDuLibForm();
        
        if (step >= 0 && step < formFields.size()) {
            FormField field = formFields.get(step);
            return CommandResponse.form(field.getPrompt(), field.getName());
        }
        return CommandResponse.error("Invalid DU-LIB form step");
    }
    
    private CommandResponse handleFormInput(String sessionId, String input, FormState formState) {
        String formType = formState.getFormType();
        int step = formState.getCurrentStep();
        
        // Validate input based on current step and form type
        CommandResponse validation = validateFormInput(formState, step, input);
        if (!validation.isSuccess()) {
            // Return error but keep the same form step
            FormState sessionState = getOrCreateSessionState(sessionId);
            validation.setPrompt(sessionState.getCurrentPrompt());
            return validation;
        }
        
        // Store the input
        String fieldName = getFieldNameForStep(formType, step);
        formState.addData(fieldName, input.trim());
        formState.nextStep();
        
        // Check if form is complete
        if (formState.isComplete()) {
            // Save current directory before clearing session
            String currentDirectory = formState.getCurrentDirectory();
            
            // Clear session BEFORE processing to avoid any interference
            activeSessions.remove(sessionId);
            CommandResponse result = processCompleteForm(sessionId, formState);
            
            // Restore directory state after processing
            FormState sessionState = getOrCreateSessionState(sessionId);
            sessionState.setCurrentDirectory(currentDirectory);
            result.setPrompt(sessionState.getCurrentPrompt());
            return result;
        }
        
        // Get next prompt
        CommandResponse nextPrompt = getNextFormPrompt(formState);
        FormState sessionState = getOrCreateSessionState(sessionId);
        nextPrompt.setPrompt(sessionState.getCurrentPrompt());
        return nextPrompt;
    }
    
    private String getFieldNameForStep(String formType, int step) {
        List<FormField> formFields = getFormFields(formType);
        if (step >= 0 && step < formFields.size()) {
            return formFields.get(step).getName();
        }
        return "unknown";
    }
    
    private List<FormField> getFormFields(String formType) {
        switch (formType) {
            case "dto": return FormBuilder.createDtoForm();
            case "lib": return FormBuilder.createLibForm();
            case "trx": return FormBuilder.createTrxForm();
            case "du-online": return FormBuilder.createDuOnlineForm();
            case "du-lib": return FormBuilder.createDuLibForm();
            default: return Arrays.asList();
        }
    }
    
    private CommandResponse validateFormInput(FormState formState, int step, String input) {
        String formType = formState.getFormType();
        List<FormField> formFields = getFormFields(formType);
        
        if (step >= formFields.size()) {
            return CommandResponse.error("Invalid form step");
        }
        
        FormField field = formFields.get(step);
        
        // Basic validation using field type
        if (input == null || input.trim().isEmpty()) {
            if (field.isRequired()) {
                return CommandResponse.error("Input cannot be empty. Try again:");
            }
            return CommandResponse.success("Valid input");
        }
        
        String trimmed = input.trim();
        
        switch (field.getType()) {
            case UUAA:
                if (!trimmed.matches("^[A-Z]{4}$")) {
                    return CommandResponse.error("UUAA must be exactly 4 uppercase letters. Try again:");
                }
                break;
            case CODE:
                if (!trimmed.matches("^\\d{3}$")) {
                    return CommandResponse.error("Code must be exactly 3 digits (001-999). Try again:");
                }
                // Check for unique code by type
                if (architectureService.containableExists(trimmed, formType)) {
                    return CommandResponse.error("Code " + trimmed + " already exists for " + formType.toUpperCase() + ". Try again:");
                }
                break;
            case VERSION:
                if (!trimmed.matches("^\\d{2}$")) {
                    return CommandResponse.error("Version must be exactly 2 digits (01-99). Try again:");
                }
                int version = Integer.parseInt(trimmed);
                if (version < 1 || version > 99) {
                    return CommandResponse.error("Version must be between 01 and 99. Try again:");
                }
                break;
            case COUNTRY_SELECT:
                String upperInput = trimmed.toUpperCase();
                if (!field.getOptions().contains(upperInput)) {
                    return CommandResponse.error("Invalid country. Valid options: " + String.join(", ", field.getOptions()) + ". Try again:");
                }
                break;
            case CLASS_NAME:
                if (!trimmed.matches("^[A-Za-z][A-Za-z0-9_]*$")) {
                    return CommandResponse.error("Class name must start with a letter and contain only letters, numbers, and underscores. Try again:");
                }
                break;
            case DEPLOYMENT_UNIT:
                if (trimmed.contains(" ")) {
                    return CommandResponse.error("Deployment Unit name cannot contain spaces. Try again:");
                }
                break;
            case DESCRIPTION:
                if (trimmed.length() < 5) {
                    return CommandResponse.error("Description must be at least 5 characters long. Try again:");
                }
                break;
        }
        
        return CommandResponse.success("Valid input");
    }
    
    private CommandResponse processCompleteForm(String sessionId, FormState formState) {
        String formType = formState.getFormType();
        Map<String, String> data = formState.getFormData();
        String currentDir = formState.getCurrentDirectory();
        
        try {
            // Check if we're in a specific directory and should create object within that DU
            if (!"root".equals(currentDir) && currentDir.contains("/")) {
                String[] pathParts = currentDir.split("/");
                if (pathParts.length == 2) {
                    String duName = pathParts[0];
                    String folder = pathParts[1];
                    
                    // Create object within the specific DU folder
                    switch (formType) {
                        case "dto":
                            return architectureService.createDtoInFolder(
                                duName,
                                data.get("uuaa"), 
                                data.get("code"), 
                                data.get("className"), 
                                data.get("description")
                            );
                        case "lib":
                            return architectureService.createLibInFolder(
                                duName,
                                data.get("uuaa"), 
                                data.get("code"), 
                                data.get("description")
                            );
                        case "trx":
                            return architectureService.createTrxInFolder(
                                duName,
                                data.get("uuaa"), 
                                data.get("code"), 
                                data.getOrDefault("version", "01"),
                                data.getOrDefault("country", "GL"),
                                data.get("description")
                            );
                    }
                }
            }
            
            // Default behavior - create standalone objects (when in root)
            switch (formType) {
                case "dto":
                    return architectureService.createDto(
                        data.get("uuaa"), 
                        data.get("code"), 
                        data.get("className"), 
                        data.get("description")
                    );
                case "lib":
                    return architectureService.createLib(
                        data.get("uuaa"), 
                        data.get("code"), 
                        data.get("description")
                    );
                case "trx":
                    return architectureService.createTrx(
                        data.get("uuaa"), 
                        data.get("code"), 
                        data.getOrDefault("version", "01"),
                        data.getOrDefault("country", "GL"),
                        data.get("description")
                    );
                case "du-online":
                    return architectureService.createDuOnline(
                        data.get("uuaa"), 
                        data.get("deploymentUnit"), 
                        data.get("description")
                    );
                case "du-lib":
                    return architectureService.createDuLib(
                        data.get("uuaa"), 
                        data.get("code"), 
                        data.get("description")
                    );
                default:
                    return CommandResponse.error("Unknown form type: " + formType);
            }
        } catch (Exception e) {
            return CommandResponse.error("Error creating " + formType + ": " + e.getMessage());
        }
    }
    
    /**
     * Universal method to create objects from form data
     * Simplifies the creation logic by routing to appropriate ArchitectureService methods
     */
    private CommandResponse createObjectFromFormData(String formType, Map<String, String> data, String currentDir) {
        try {
            // Check if we're in a specific directory and should create object within that DU
            if (!"root".equals(currentDir) && currentDir.contains("/")) {
                String[] pathParts = currentDir.split("/");
                if (pathParts.length == 2) {
                    String duName = pathParts[0];
                    
                    // Create object within the specific DU folder
                    switch (formType) {
                        case "dto":
                            return architectureService.createDtoInFolder(
                                duName, data.get("uuaa"), data.get("code"), 
                                data.get("className"), data.get("description")
                            );
                        case "lib":
                            return architectureService.createLibInFolder(
                                duName, data.get("uuaa"), data.get("code"), data.get("description")
                            );
                        case "trx":
                            return architectureService.createTrxInFolder(
                                duName, data.get("uuaa"), data.get("code"), 
                                data.getOrDefault("version", "01"), data.getOrDefault("country", "GL"),
                                data.get("description")
                            );
                        default:
                            return CommandResponse.error("Cannot create " + formType + " inside a deployment unit.");
                    }
                }
            }
            
            // Default behavior - create standalone objects using simplified ArchitectureService methods
            switch (formType) {
                case "dto":
                    return architectureService.createDto(
                        data.get("uuaa"), data.get("code"), 
                        data.get("className"), data.get("description")
                    );
                case "lib":
                    return architectureService.createLib(
                        data.get("uuaa"), data.get("code"), data.get("description")
                    );
                case "trx":
                    return architectureService.createTrx(
                        data.get("uuaa"), data.get("code"), 
                        data.getOrDefault("version", "01"), data.getOrDefault("country", "GL"),
                        data.get("description")
                    );
                case "du-online":
                    return architectureService.createDuOnline(
                        data.get("uuaa"), data.get("deploymentUnit"), data.get("description")
                    );
                case "du-lib":
                    return architectureService.createDuLib(
                        data.get("uuaa"), data.get("code"), data.get("description")
                    );
                default:
                    return CommandResponse.error("Unknown form type: " + formType);
            }
            return CommandResponse.error("Error creating " + formType + ": " + e.getMessage());
        }
    }
    
    private CommandResponse handleApxCommand(String sessionId, String[] args) {
        if (args.length == 0) {
            return CommandResponse.error("Usage: apx <command>. Type 'apx help' for available commands.");
        }
        
        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        
        switch (subCommand) {
            case "help":
                return showHelp();
            case "init":
                return handleInitCommand(subArgs);
            case "add":
                return handleAddCommand(sessionId, subArgs);
            case "list":
                return handleListCommand(subArgs);
            case "dep":
                return handleDepCommand(subArgs);
            case "show":
                return handleShowCommand(subArgs);
            case "debug-du":
                return handleDebugDuCommand(subArgs);
            case "reset":
                return handleResetSessionCommand(sessionId);
            case "reset-all":
                return handleResetAllSessionsCommand();
            case "debug":
                return handleDebugSessionsCommand();
            case "test":
                return CommandResponse.success("Test command works! Args: " + String.join(", ", subArgs));
            default:
                return CommandResponse.error("Unknown apx command: " + subCommand + ". Type 'apx help' for available commands.");
        }
    }
    
    private CommandResponse handleLsCommand(String sessionId, String[] args) {
        FormState sessionState = getOrCreateSessionState(sessionId);
        String currentDir = sessionState.getCurrentDirectory();
        
        if ("root".equals(currentDir)) {
            // List deployment units in root
            return architectureService.listDeploymentUnits(null);
        } else if (!currentDir.contains("/")) {
            // In deployment unit, list folders
            List<String> folders = Arrays.asList(
                "dto/        - Data Transfer Objects folder",
                "transactions/ - Business transactions folder", 
                "library/     - Library components folder"
            );
            
            CommandResponse response = new CommandResponse(true, "Contents of " + currentDir + ":", 
                folders, CommandResponse.ResponseType.INFO, null);
            return response;
        } else {
            // In specific folder, list components within that folder
            String[] pathParts = currentDir.split("/");
            String duName = pathParts[0];
            String folder = pathParts[1];
            
            return architectureService.listComponentsInFolder(duName, folder);
        }
    }
    
    private CommandResponse handleExitCommand() {
        return CommandResponse.success("Goodbye! Session terminated.");
    }
}