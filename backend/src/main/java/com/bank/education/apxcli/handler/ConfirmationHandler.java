package com.bank.education.apxcli.handler;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.deletion.DeletionCommandService;
import com.bank.education.apxcli.service.forms.FormProcessingService;
import com.bank.education.apxcli.service.dependencies.DependencyCommandService;
import org.springframework.stereotype.Component;

/**
 * Handler for confirmation prompts (Y/n responses)
 * 
 * Priority 10 (HIGHEST) - Must intercept all Y/n responses before any other handler.
 * Activated when sessionState.getAwaitingConfirmationFor() != null.
 * 
 * Confirmation Actions:
 * - delete-component-* → Component deletion
 * - delete-dependency-* → Dependency deletion
 * - delete-input-* → Transaction input deletion
 * - delete-output-* → Transaction output deletion
 * - create-component-* → Component creation
 * - create-dep-* → Dependency creation
 * 
 * Accepts:
 * - Y/y → Confirm
 * - Enter (empty) → Confirm
 * - n → Cancel
 * 
 * Delegates to:
 * - DeletionCommandService for delete-* actions
 * - FormProcessingService for create-component-* actions
 * - DependencyCommandService for create-dep-* actions
 */
@Component
public class ConfirmationHandler extends CommandHandler {
    
    public static final int PRIORITY = 10;
    
    private final DeletionCommandService deletionService;
    private final FormProcessingService formProcessingService;
    private final DependencyCommandService dependencyCommandService;
    
    public ConfirmationHandler(DeletionCommandService deletionService,
                              FormProcessingService formProcessingService,
                              DependencyCommandService dependencyCommandService) {
        this.deletionService = deletionService;
        this.formProcessingService = formProcessingService;
        this.dependencyCommandService = dependencyCommandService;
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean canHandle(CommandRequest request, FormState sessionState) {
        return sessionState.getAwaitingConfirmationFor() != null;
    }
    
    @Override
    public CommandResponse handle(CommandRequest request, FormState sessionState) {
        String action = sessionState.getAwaitingConfirmationFor();
        String inputLower = request.getCommand().trim().toLowerCase();
        
        // Check for explicit cancellation
        if ("n".equals(inputLower)) {
            // Create new session state without confirmation flag and pending data
            FormState newState = createCleanSessionState(sessionState);
            CommandResponse response = CommandResponse.success("Operation cancelled");
            response.setNewSessionState(newState);
            return response;
        }
        
        // Accept confirmation: Y, y, or Enter (empty)
        if ("y".equals(inputLower) || request.getCommand().trim().isEmpty()) {
            CommandResponse response = routeToService(action, sessionState);
            // Create new session state without confirmation flag and pending data
            FormState newState = createCleanSessionState(sessionState);
            response.setNewSessionState(newState);
            return response;
        }
        
        // Invalid response - keep waiting for confirmation (no state change)
        return CommandResponse.error("Invalid response. Enter Y to confirm, n to cancel, or press Enter to confirm.");
    }
    
    /**
     * Creates a new session state with confirmation cleared
     * Preserves directory but removes confirmation flag and pending data
     */
    private FormState createCleanSessionState(FormState oldState) {
        FormState newState = new FormState();
        newState.setCurrentDirectory(oldState.getCurrentDirectory());
        // awaitingConfirmationFor is null by default (cleared)
        // pendingCreate_* data is not copied (cleaned)
        return newState;
    }
    
    /**
     * Routes confirmed action to appropriate service based on action prefix
     */
    private CommandResponse routeToService(String action, FormState sessionState) {
        if (action.startsWith("delete-component-")) {
            return deletionService.executeConfirmedDelete(action, sessionState);
        }
        if (action.startsWith("delete-dependency-")) {
            return deletionService.executeConfirmedDependencyDelete(action, sessionState);
        }
        if (action.startsWith("delete-input-")) {
            return deletionService.executeConfirmedInputDelete(action, sessionState);
        }
        if (action.startsWith("delete-output-")) {
            return deletionService.executeConfirmedOutputDelete(action, sessionState);
        }
        if (action.startsWith("create-component-")) {
            return formProcessingService.executeConfirmedCreate(action, sessionState);
        }
        if (action.startsWith("create-dep-")) {
            return dependencyCommandService.executeConfirmedDependencyCreate(action, sessionState);
        }
        
        return CommandResponse.error("Unknown action: " + action);
    }
}
