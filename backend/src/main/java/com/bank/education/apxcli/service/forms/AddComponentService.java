package com.bank.education.apxcli.service.forms;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service responsible for handling "apx add" command workflow
 * Manages component type selection menu (DTO, Transaction, Library)
 */
@Service
public class AddComponentService {
    
    private final ArchitectureOrchestrationService architectureService;
    private final FormPromptService formPromptService;
    private Map<String, FormState> activeSessions;
    
    public AddComponentService(ArchitectureOrchestrationService architectureService,
                              FormPromptService formPromptService) {
        this.architectureService = architectureService;
        this.formPromptService = formPromptService;
    }
    
    public void setActiveSessions(Map<String, FormState> sessions) {
        this.activeSessions = sessions;
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
        System.out.println("[AddComponentService.startFormSession] sessionId=" + sessionId + ", formType=" + formType);
        System.out.println("[AddComponentService] activeSessions Map identity: " + System.identityHashCode(activeSessions));
        
        // Clear any existing form session but preserve directory state
        activeSessions.remove(sessionId);
        
        FormState formState = new FormState(formType);
        formState.setCurrentDirectory(currentDirectory);
        activeSessions.put(sessionId, formState);
        
        System.out.println("[AddComponentService] AFTER put - contains sessionId: " + activeSessions.containsKey(sessionId));
        System.out.println("[AddComponentService] AFTER put - formState.formType: " + formState.getFormType());
        
        return formPromptService.getNextFormPrompt(formState);
    }
    
    private CommandResponse startFormSessionWithUuaa(String sessionId, String formType, String duName, 
                                                     String uuaa, FormState sessionState) {
        String currentDirectory = sessionState.getCurrentDirectory();
        
        // Clear any existing form session but preserve directory state
        activeSessions.remove(sessionId);
        
        FormState formState = new FormState(formType);
        formState.setCurrentDirectory(currentDirectory);
        formState.addData("uuaa", uuaa); // Pre-fill UUAA
        formState.addData("duName", duName); // Store DU name for later use
        formState.nextStep(); // Skip UUAA step
        
        activeSessions.put(sessionId, formState);
        
        // Get the next form prompt
        CommandResponse nextPrompt = formPromptService.getNextFormPrompt(formState);
        
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
