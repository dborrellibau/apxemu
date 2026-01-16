package com.bank.education.apxcli.service.forms;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import com.bank.education.apxcli.service.validation.MenuValidationService;

import javafx.scene.control.Menu;

import org.springframework.stereotype.Service;

/**
 * Service responsible for handling "apx add" command workflow
 * Manages component type selection menu (DTO, Transaction, Library)
 */
@Service
public class AddComponentService {
    
    private final ArchitectureOrchestrationService architectureService;
    private final FormPromptService formPromptService;
    private final MenuValidationService menuValidationService;
    
    public AddComponentService(ArchitectureOrchestrationService architectureService,
                              FormPromptService formPromptService,
                              MenuValidationService menuValidationService) {
        this.architectureService = architectureService;
        this.formPromptService = formPromptService;
        this.menuValidationService = menuValidationService;
    }
    
    public CommandResponse handleComponentSelection(String input, FormState sessionState) {
        String currentDir = sessionState.getCurrentDirectory();
        String lowerInput = input.toLowerCase().trim();
        
         // Validar y mapear usando MenuValidationService
        if (!menuValidationService.isValidAddComponentSelection(lowerInput, 3)) {
            CommandResponse error = CommandResponse.error("Invalid selection. Enter 1-3 or type name (dto/transaction/library)");
            error.setPrompt(sessionState.getCurrentPrompt());
            return error;
        }

        String componentType = menuValidationService.getAddComponentTypeForSelection(lowerInput);
        
        // Clear the awaiting flag
        sessionState.setAwaitingComponentSelection(false);
        
        // Get UUAA from DU-ONLINE
        String duUuaa = architectureService.getDeploymentUnitUuaa(currentDir);
        
        if (duUuaa == null) {
            CommandResponse error = CommandResponse.error("Could not retrieve UUAA from deployment unit: " + currentDir);
            error.setPrompt(sessionState.getCurrentPrompt());
            return error;
        }
        
        // Start form session with UUAA pre-filled
        return startFormSessionWithUuaa(componentType, currentDir, duUuaa, sessionState);
    }
    
    public CommandResponse startFormSession(String formType, String currentDirectory) {
        // Create new FormState for form
        FormState formState = new FormState(formType);
        formState.setCurrentDirectory(currentDirectory);
        
        // Get first prompt
        CommandResponse response = formPromptService.getNextFormPrompt(formState);
        
        // Attach new state so CommandParserService can replace session
        response.setNewSessionState(formState);
        
        return response;
    }
    
    private CommandResponse startFormSessionWithUuaa(String formType, String duName, 
                                                     String uuaa, FormState sessionState) {
        String currentDirectory = sessionState.getCurrentDirectory();
        
        // Create new FormState with UUAA pre-filled
        FormState formState = new FormState(formType);
        formState.setCurrentDirectory(currentDirectory);
        formState.addData("uuaa", uuaa); // Pre-fill UUAA
        formState.addData("duName", duName); // Store DU name for later use
        formState.nextStep(); // Skip UUAA step
        
        // Get the next form prompt
        CommandResponse nextPrompt = formPromptService.getNextFormPrompt(formState);
        
        // Attach new state so CommandParserService can replace session
        nextPrompt.setNewSessionState(formState);
        
        // Prepend UUAA information to the response message
        String uuaaMessage = "UUAA: " + uuaa;
        if (nextPrompt.getMessage() != null && !nextPrompt.getMessage().isEmpty()) {
            nextPrompt.setMessage(uuaaMessage + "\n" + nextPrompt.getMessage());
        } else {
            nextPrompt.setMessage(uuaaMessage);
        }
        
        // Set the correct prompt based on current directory
        nextPrompt.setPrompt(sessionState.getCurrentPrompt());
        
        return nextPrompt;
    }
}
