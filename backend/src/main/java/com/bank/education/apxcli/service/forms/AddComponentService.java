package com.bank.education.apxcli.service.forms;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import org.springframework.stereotype.Service;

/**
 * Service responsible for handling "apx add" command workflow
 * Manages component type selection menu (DTO, Transaction, Library)
 */
@Service
public class AddComponentService {
    
    private final ArchitectureOrchestrationService architectureService;
    private final FormPromptService formPromptService;
    
    public AddComponentService(ArchitectureOrchestrationService architectureService,
                              FormPromptService formPromptService) {
        this.architectureService = architectureService;
        this.formPromptService = formPromptService;
    }
    
    public CommandResponse handleComponentSelection(String sessionId, String input, FormState sessionState) {
        String currentDir = sessionState.getCurrentDirectory();
        
        // Parse selection (1-3 or dto/transaction/library)
        String componentType = null;
        String lowerInput = input.toLowerCase().trim();
        
        if (lowerInput.matches("^[1-3]$")) {
            int selection = Integer.parseInt(lowerInput);
            switch (selection) {
                case 1: componentType = "dto"; break;
                case 2: componentType = "trx"; break;
                case 3: componentType = "lib"; break;
            }
        } else if ("dto".equals(lowerInput)) {
            componentType = "dto";
        } else if ("transaction".equals(lowerInput)) {
            componentType = "trx";
        } else if ("library".equals(lowerInput)) {
            componentType = "lib";
        }
        
        if (componentType == null) {
            CommandResponse error = CommandResponse.error("Invalid selection. Enter 1-3 or type name (dto/transaction/library)");
            error.setPrompt(sessionState.getCurrentPrompt());
            return error;
        }
        
        // Clear the awaiting flag
        sessionState.setAwaitingComponentSelection(false);
        
        // Get UUAA from DU-ONLINE
        String duName = currentDir;
        String duUuaa = architectureService.getDeploymentUnitUuaa(duName);
        
        if (duUuaa == null) {
            CommandResponse error = CommandResponse.error("Could not retrieve UUAA from deployment unit: " + duName);
            error.setPrompt(sessionState.getCurrentPrompt());
            return error;
        }
        
        // Start form session with UUAA pre-filled
        return startFormSessionWithUuaa(sessionId, componentType, duName, duUuaa, sessionState);
    }
    
    public CommandResponse startFormSession(String sessionId, String formType, String currentDirectory) {
        // Create new FormState for form wizard
        FormState formState = new FormState(formType);
        formState.setCurrentDirectory(currentDirectory);
        
        // Get first prompt
        CommandResponse response = formPromptService.getNextFormPrompt(formState);
        
        // Attach new state so CommandParserService can replace session
        response.setNewSessionState(formState);
        
        return response;
    }
    
    private CommandResponse startFormSessionWithUuaa(String sessionId, String formType, String duName, 
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
