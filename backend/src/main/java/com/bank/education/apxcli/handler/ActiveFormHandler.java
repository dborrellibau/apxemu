package com.bank.education.apxcli.handler;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.forms.FormInputService;
import org.springframework.stereotype.Component;

import java.util.Map;

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
    private Map<String, FormState> activeSessions;
    
    public ActiveFormHandler(FormInputService formInputService) {
        this.formInputService = formInputService;
    }
    
    public void setActiveSessions(Map<String, FormState> sessions) {
        this.activeSessions = sessions;
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean canHandle(CommandRequest request, FormState sessionState) {
        String sessionId = request.getSessionId();
        
        // DEBUG: Log session lookup
        System.out.println("[ActiveFormHandler] Checking canHandle for session: " + sessionId);
        System.out.println("[ActiveFormHandler] activeSessions Map identity: " + System.identityHashCode(activeSessions));
        System.out.println("[ActiveFormHandler] activeSessions contents: " + activeSessions.keySet());
        
        FormState activeForm = activeSessions.get(sessionId);
        System.out.println("[ActiveFormHandler] activeForm: " + activeForm);
        System.out.println("[ActiveFormHandler] activeForm formType: " + (activeForm != null ? activeForm.getFormType() : "null"));
        
        // Handler activates if there's an active form with a formType set
        boolean canHandle = activeForm != null && activeForm.getFormType() != null;
        System.out.println("[ActiveFormHandler] canHandle result: " + canHandle);
        return canHandle;
    }
    
    @Override
    public CommandResponse handle(CommandRequest request, FormState sessionState) {
        String sessionId = request.getSessionId();
        FormState activeForm = activeSessions.get(sessionId);
        
        // Delegate all form input processing to FormInputService
        return formInputService.handleFormInput(
            sessionId,
            request.getCommand(),
            activeForm,
            sessionState
        );
    }
}
