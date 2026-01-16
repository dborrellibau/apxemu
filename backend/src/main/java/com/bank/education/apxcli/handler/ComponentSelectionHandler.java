package com.bank.education.apxcli.handler;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.forms.AddComponentService;
import org.springframework.stereotype.Component;

/**
 * Handler for component selection menu after "apx add"
 * 
 * Priority 40 - Handles selection from 3-option menu:
 * 1. DTO
 * 2. LIB
 * 3. TRX
 * 
 * Activated after user executes "apx add" in valid location (DU_ONLINE, DU_LIB, or FOLDER).
 * 
 * Delegates to AddComponentService for:
 * - Parsing selection (1-3 or component name)
 * - Starting form wizard for selected component type
 */
@Component
public class ComponentSelectionHandler extends CommandHandler {
    
    public static final int PRIORITY = 40;
    
    private final AddComponentService addComponentService;
    
    public ComponentSelectionHandler(AddComponentService addComponentService) {
        this.addComponentService = addComponentService;
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean canHandle(CommandRequest request, FormState sessionState) {
        return sessionState.isAwaitingComponentSelection();
    }
    
    @Override
    public CommandResponse handle(CommandRequest request, FormState sessionState) {
        return addComponentService.handleComponentSelection(
            request.getCommand(),
            sessionState
        );
    }
}
