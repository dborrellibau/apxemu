package com.bank.education.apxcli.handler;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.forms.AddComponentService;
import com.bank.education.apxcli.service.validation.MenuValidationService;
import org.springframework.stereotype.Component;

/**
 * Handler for "apx init" menu selection (1-5 or type names)
 * 
 * Priority 30 - Handles selection from 5-option menu:
 * 1. DU Online
 * 2. DU Lib
 * 3. DTO
 * 4. LIB
 * 5. TRX
 * 
 * Activated after user executes "apx init" at ROOT level.
 * Accepts numeric (1-5) or type names (du-online, du-lib, dto, lib, trx).
 * 
 * Delegates to AddComponentService.startFormSession() to begin form wizard.
 */
@Component
public class InitMenuHandler extends CommandHandler {

    public static final int PRIORITY = 30;

    private final AddComponentService addComponentService;
    private final MenuValidationService menuValidationService;

    public InitMenuHandler(AddComponentService addComponentService,
            MenuValidationService menuValidationService) {
        this.addComponentService = addComponentService;
        this.menuValidationService = menuValidationService;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public boolean canHandle(CommandRequest request, FormState sessionState) {
        return sessionState.isAwaitingInitSelection();
    }

    @Override
    public CommandResponse handle(CommandRequest request, FormState sessionState) {
        String input = request.getCommand().trim().toLowerCase();

        if (menuValidationService.isNotImplemented(input)) {
            return CommandResponse.error("Not implemented yet. This component type is under construction.");
        }

        if (!menuValidationService.isValidInitSelection(input, 5)) {
            return CommandResponse.error(
                    "Invalid selection. Please choose again:");
        }

        String formType = menuValidationService.getTypeForSelection(input);

        sessionState.setAwaitingInitSelection(false);
        return addComponentService.startFormSession(
                formType,
                sessionState.getCurrentDirectory());
    }
}
