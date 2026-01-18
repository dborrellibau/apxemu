package com.bank.education.apxcli.service.forms;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.form.FormBuilder;
import com.bank.education.apxcli.form.FormField;
import com.bank.education.apxcli.dto.FormState;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

/**
 * Service responsible for handling form input and validation
 */
@Service
public class FormInputService {
    private final FormPromptService formPromptService;
    private final FormProcessingService formProcessingService;
    private final FormValidationService formValidationService;
    
    public FormInputService(FormPromptService formPromptService,
        FormProcessingService formProcessingService,
        FormValidationService formValidationService) {
    this.formPromptService = formPromptService;
    this.formProcessingService = formProcessingService;
    this.formValidationService = formValidationService;
    }
    
    public CommandResponse handleFormInput(String sessionId, String input, FormState formState, FormState sessionState) {
        String formType = formState.getFormType();
        int step = formState.getCurrentStep();
        
        // Get current field (single source of truth)
        List<FormField> fields = getFormFields(formType);
        FormField field = fields.get(step);

        // Validate input (delegated to FormValidationService)
        CommandResponse validation = formValidationService.validateInput(field, input, formType, formState);
        if (!validation.isSuccess()) {
            validation.setPrompt(sessionState.getCurrentPrompt());
            return validation;
        }

        // Normalize + store canonical value (delegated)
        String normalized = formValidationService.normalizeInput(field, input);
        formState.addData(field.getName(), normalized);
        formState.nextStep();
        
        // Check if form is complete
        if (formState.isComplete()) {
            // Save current directory before clearing form
            String currentDirectory = formState.getCurrentDirectory();
            
            // Process the completed form - this modifies sessionState with confirmation data
            CommandResponse result = formProcessingService.processCompleteForm(sessionId, formState, sessionState);
            
            // Create new session state (form cleared, preserve directory)
            FormState newSessionState = new FormState();
            newSessionState.setCurrentDirectory(currentDirectory);
            
            // CRITICAL: Copy confirmation flag that was JUST SET by processCompleteForm
            // processCompleteForm sets awaitingConfirmationFor in sessionState
            if (sessionState.getAwaitingConfirmationFor() != null) {
                newSessionState.setAwaitingConfirmationFor(sessionState.getAwaitingConfirmationFor());
                // Copy pending data as well
                for (String key : sessionState.getFormData().keySet()) {
                    if (key.startsWith("pendingCreate_")) {
                        newSessionState.addData(key, sessionState.getData(key));
                    }
                }
            }
            
            // Attach new state so CommandParserService can replace session
            result.setNewSessionState(newSessionState);
            result.setPrompt(newSessionState.getCurrentPrompt());
            return result;
        }
        
        // Get next prompt
        CommandResponse nextPrompt = formPromptService.getNextFormPrompt(formState);
        nextPrompt.setPrompt(sessionState.getCurrentPrompt());
        return nextPrompt;
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
    
}
