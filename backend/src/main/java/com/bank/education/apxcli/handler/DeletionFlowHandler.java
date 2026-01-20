package com.bank.education.apxcli.handler;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.deletion.DeletionCommandService;
import org.springframework.stereotype.Component;

/**
 * Handler for deletion flow menu selection
 * 
 * Priority 20 - High priority for interactive deletion menu.
 * Activated after user executes "apx del" and sees numbered list of components.
 * 
 * Delegates to DeletionCommandService for:
 * - Parsing user selection (numeric or name)
 * - Confirming deletion
 * - Executing soft delete
 */
@Component
public class DeletionFlowHandler extends CommandHandler {
    
    public static final int PRIORITY = 20;
    
    private final DeletionCommandService deletionService;
    
    public DeletionFlowHandler(DeletionCommandService deletionService) {
        this.deletionService = deletionService;
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean canHandle(CommandRequest request, FormState sessionState) {
        return sessionState.isAwaitingDeletionSelection();
    }
    
    @Override
    public CommandResponse handle(CommandRequest request, FormState sessionState) {
        return handleDeletionSelection(sessionState, request.getCommand());
    }

        /**
     * ETAPA 5: Process user selection in deletion flow
     * Handles type selection, component selection, etc.
     */
    public CommandResponse handleDeletionSelection(FormState sessionState, String input) {
        String deletionStep = sessionState.getData("deletionStep");

        if ("type-selection".equals(deletionStep)) {
            return deletionService.handleDeleteTypeSelection(sessionState, input);
        } else if ("component-selection".equals(deletionStep)) {
            return deletionService.handleComponentSelection(sessionState, input);
        } else if ("dependency-selection".equals(deletionStep)) {
            return deletionService.handleDependencySelection(sessionState, input);
        } else if ("inout-dto-input".equals(deletionStep)) {
            return deletionService.handleInOutDtoInput(sessionState, input);
        }

        // Unknown step
        sessionState.clearDeletionFlowData();
        return CommandResponse.error("Unknown deletion step. Flow cancelled.");
    }
}
