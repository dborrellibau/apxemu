package com.bank.education.apxcli.service.forms;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.form.FormBuilder;
import com.bank.education.apxcli.form.FormField;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for handling form input and validation
 */
@Service
public class FormInputService {
    private final ArchitectureOrchestrationService architectureService;
    private final FormPromptService formPromptService;
    private final FormProcessingService formProcessingService;
    private final FormValidationService formValidationService;
    private Map<String, FormState> activeSessions;
    
    public FormInputService(ArchitectureOrchestrationService architectureService,
        FormPromptService formPromptService,
        FormProcessingService formProcessingService,
        FormValidationService formValidationService) {
    this.architectureService = architectureService;
    this.formPromptService = formPromptService;
    this.formProcessingService = formProcessingService;
    this.formValidationService = formValidationService;
    }
    
    public void setActiveSessions(Map<String, FormState> sessions) {
        this.activeSessions = sessions;
    }
    
    public CommandResponse handleFormInput(String sessionId, String input, FormState formState, FormState sessionState) {
        String formType = formState.getFormType();
        int step = formState.getCurrentStep();
        
        // Get current field (single source of truth)
        List<FormField> fields = getFormFields(formType);
        FormField field = fields.get(step);

        // Validate input (delegated to FormValidationService)
        CommandResponse validation = formValidationService.validateInput(field, input, formType);
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
            // Save current directory before clearing session
            String currentDirectory = formState.getCurrentDirectory();
            
            // Process the completed form - now returns confirmation prompt
            CommandResponse result = formProcessingService.processCompleteForm(sessionId, formState, sessionState);
            
            // Clear the active form session (user is no longer in form mode)
            activeSessions.remove(sessionId);
            
            // Recreate session state to preserve directory navigation
            FormState newSessionState = new FormState();
            newSessionState.setCurrentDirectory(currentDirectory);
            
            // Copy confirmation flag if it was set (for continuation)
            if (sessionState.getAwaitingConfirmationFor() != null) {
                newSessionState.setAwaitingConfirmationFor(sessionState.getAwaitingConfirmationFor());
                // Copy pending data as well
                for (String key : sessionState.getFormData().keySet()) {
                    if (key.startsWith("pendingCreate_")) {
                        newSessionState.addData(key, sessionState.getData(key));
                    }
                }
            }
            
            activeSessions.put(sessionId, newSessionState);
            
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
