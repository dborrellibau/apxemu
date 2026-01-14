package com.bank.education.apxcli.handler;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.inout.InOutCommandService;
import org.springframework.stereotype.Component;

/**
 * Handler for in/out transaction flow (2-step process)
 * 
 * Priority 60 - Handles interactive in/out creation flow for TRX components.
 * Activated by "apx add in" or "apx add out" commands.
 * 
 * Two-step flow:
 * 1. Selection mode: Choose from existing DTOs or create new one
 * 2. DTO name input: If creating new, enter DTO name
 * 
 * State tracking flags:
 * - isInOutSelectionMode() - User choosing option 1-3
 * - isAwaitingInOutDtoName() - User entering new DTO name
 * 
 * Delegates to InOutCommandService for all flow logic.
 */
@Component
public class InOutFlowHandler extends CommandHandler {
    
    public static final int PRIORITY = 60;
    
    private final InOutCommandService inOutService;
    
    public InOutFlowHandler(InOutCommandService inOutService) {
        this.inOutService = inOutService;
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean canHandle(CommandRequest request, FormState sessionState) {
        return sessionState.isInOutSelectionMode() || 
               sessionState.isAwaitingInOutDtoName();
    }
    
    @Override
    public CommandResponse handle(CommandRequest request, FormState sessionState) {
        String input = request.getCommand();
        
        // Step 1: Option selection (existing DTO, new DTO, cancel)
        if (sessionState.isInOutSelectionMode()) {
            return inOutService.handleOptionSelection(sessionState, input);
        }
        
        // Step 2: New DTO name input
        if (sessionState.isAwaitingInOutDtoName()) {
            return inOutService.handleDtoNameInput(sessionState, input);
        }
        
        return CommandResponse.error("Invalid in/out flow state");
    }
}
