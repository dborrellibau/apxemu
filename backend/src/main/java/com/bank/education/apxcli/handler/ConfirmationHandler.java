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
        sessionState.setAwaitingConfirmationFor(null); // Clear flag immediately
        
        String inputLower = request.getCommand().trim().toLowerCase();
        
        // Check for explicit cancellation
        if ("n".equals(inputLower)) {
            cleanupPendingConfirmationData(sessionState);
            return CommandResponse.success("Operation cancelled");
        }
        
        // Accept confirmation: Y, y, or Enter (empty)
        if ("y".equals(inputLower) || request.getCommand().trim().isEmpty()) {
            return routeToService(action, sessionState);
        }
        
        // Invalid response - re-prompt
        sessionState.setAwaitingConfirmationFor(action); // Restore flag
        return CommandResponse.error("Invalid response. Enter Y to confirm, n to cancel, or press Enter to confirm.");
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
    
    /**
     * Cleans up temporary data stored during confirmation flow
     * Removes all pendingCreate_*, pendingDep_*, pendingDelDep_*, and pendingInOut_* keys
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
        
        // Clean up pending deletion dependency data (from apx del dep)
        sessionState.getFormData().remove("pendingDelDep_componentId");
        sessionState.getFormData().remove("pendingDelDep_dependencyId");
        sessionState.getFormData().remove("pendingDelDep_componentName");
        sessionState.getFormData().remove("pendingDelDep_dependencyName");
        
        // Clean up pending in/out deletion data (from apx del in/out)
        sessionState.getFormData().remove("pendingInOut_transactionId");
        sessionState.getFormData().remove("pendingInOut_dtoId");
        sessionState.getFormData().remove("pendingInOut_transactionName");
        sessionState.getFormData().remove("pendingInOut_dtoName");
        sessionState.getFormData().remove("pendingInOut_context");
    }
}
