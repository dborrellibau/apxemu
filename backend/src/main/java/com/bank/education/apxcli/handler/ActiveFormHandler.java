package com.bank.education.apxcli.handler;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.forms.FormInputService;
import org.springframework.stereotype.Component;

/**
 * Handler for active form sessions (multi-step component creation forms)
 * 
 * Priority 70 - Processes user input while a form is active.
 * Activated after "apx init" or "apx add" starts a form wizard.
 * 
 * Forms track:
 * - formType (du-online, dto, lib, trx, etc.)
 * - currentStep (which field is being filled)
 * - formData (accumulated field values)
 * 
 * Each input is validated against current field requirements, then:
 * - Stored in formData
 * - Step advanced
 * - Next prompt returned
 * 
 * When complete (all fields filled), delegates to FormProcessingService
 * to create entity and request confirmation.
 * 
 * Delegates to FormInputService.handleFormInput() for all form logic.
 */
@Component
public class ActiveFormHandler extends CommandHandler {
    
    public static final int PRIORITY = 70;
    
    private final FormInputService formInputService;
    
    public ActiveFormHandler(FormInputService formInputService) {
        this.formInputService = formInputService;
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean canHandle(CommandRequest request, FormState sessionState) {
        // Handler activates if there's an active form with a formType set
        return sessionState.getFormType() != null;
    }
    
    @Override
    public CommandResponse handle(CommandRequest request, FormState sessionState) {
        // Delegate all form input processing to FormInputService
        // sessionState already contains the active form data
        return formInputService.handleFormInput(
            request.getSessionId(),
            request.getCommand(),
            sessionState,  // This IS the active form state
            sessionState   // Also needed for directory context
        );
    }
}
