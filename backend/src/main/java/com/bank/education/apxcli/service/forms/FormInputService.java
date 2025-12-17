package com.bank.education.apxcli.service.forms;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.form.FormBuilder;
import com.bank.education.apxcli.form.FormField;
import com.bank.education.apxcli.form.FormField.FieldType;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for handling form input and validation
 */
@Service
public class FormInputService {
    
    private final ArchitectureOrchestrationService architectureService;
    private final FormPromptService formPromptService;
    private final FormProcessingService formProcessingService;
    private Map<String, FormState> activeSessions;
    
    public FormInputService(ArchitectureOrchestrationService architectureService,
                           FormPromptService formPromptService,
                           FormProcessingService formProcessingService) {
        this.architectureService = architectureService;
        this.formPromptService = formPromptService;
        this.formProcessingService = formProcessingService;
    }
    
    public void setActiveSessions(Map<String, FormState> sessions) {
        this.activeSessions = sessions;
    }
    
    public CommandResponse handleFormInput(String sessionId, String input, FormState formState, FormState sessionState) {
        String formType = formState.getFormType();
        int step = formState.getCurrentStep();
        
        // Validate input based on current step and form type
        CommandResponse validation = validateFormInput(formState, step, input);
        if (!validation.isSuccess()) {
            // Return error but keep the same form step
            validation.setPrompt(sessionState.getCurrentPrompt());
            return validation;
        }
        
        // Store the input
        String fieldName = getFieldNameForStep(formType, step);
        formState.addData(fieldName, input.trim());
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
    
    private String getFieldNameForStep(String formType, int step) {
        List<FormField> formFields = getFormFields(formType);
        if (step >= 0 && step < formFields.size()) {
            return formFields.get(step).getName();
        }
        return "unknown";
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
    
    private CommandResponse validateFormInput(FormState formState, int step, String input) {
        String formType = formState.getFormType();
        List<FormField> formFields = getFormFields(formType);
        
        if (step >= formFields.size()) {
            return CommandResponse.error("Invalid form step");
        }
        
        FormField field = formFields.get(step);
        
        // Basic validation using field type
        if (input == null || input.trim().isEmpty()) {
            if (field.isRequired()) {
                return CommandResponse.error("Input cannot be empty. Try again:");
            }
            return CommandResponse.success("Valid input");
        }
        
        String trimmed = input.trim();
        
        switch (field.getType()) {
            case UUAA:
                if (!trimmed.matches("^[A-Z]{4}$")) {
                    return CommandResponse.error("UUAA must be exactly 4 uppercase letters. Try again:");
                }
                break;
            case CODE:
                if (!trimmed.matches("^\\d{3}$")) {
                    return CommandResponse.error("Code must be exactly 3 digits (001-999). Try again:");
                }
                // Check for unique code by type
                if (architectureService.containableExists(trimmed, formType)) {
                    return CommandResponse.error("Code " + trimmed + " already exists for " + formType.toUpperCase() + ". Try again:");
                }
                break;
            case VERSION:
                if (!trimmed.matches("^\\d{2}$")) {
                    return CommandResponse.error("Version must be exactly 2 digits (01-99). Try again:");
                }
                int version = Integer.parseInt(trimmed);
                if (version < 1 || version > 99) {
                    return CommandResponse.error("Version must be between 01 and 99. Try again:");
                }
                break;
            case COUNTRY_SELECT:
                String upperInput = trimmed.toUpperCase();
                if (!field.getOptions().contains(upperInput)) {
                    return CommandResponse.error("Invalid country. Valid options: " + String.join(", ", field.getOptions()) + ". Try again:");
                }
                break;
            case CLASS_NAME:
                if (!trimmed.matches("^[A-Za-z][A-Za-z0-9_]*$")) {
                    return CommandResponse.error("Class name must start with a letter and contain only letters, numbers, and underscores. Try again:");
                }
                break;
            case DEPLOYMENT_UNIT:
                if (trimmed.contains(" ")) {
                    return CommandResponse.error("Deployment Unit name cannot contain spaces. Try again:");
                }
                break;
            case DESCRIPTION:
                if (trimmed.length() < 5) {
                    return CommandResponse.error("Description must be at least 5 characters long. Try again:");
                }
                break;
        }
        
        return CommandResponse.success("Valid input");
    }
}
