package com.bank.education.apxcli.service.forms;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import com.bank.education.apxcli.util.ConfirmationMessages;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service responsible for processing completed forms and creating entities
 */
@Service
public class FormProcessingService {
    
    private final ArchitectureOrchestrationService architectureService;
    
    public FormProcessingService(ArchitectureOrchestrationService architectureService) {
        this.architectureService = architectureService;
    }
    
    /**
     * Process completed form - now shows confirmation before creating
     * @param sessionId session identifier
     * @param formState the completed form with all data
     * @param sessionState session state to store pending data
     * @return confirmation message or error
     */
    public CommandResponse processCompleteForm(String sessionId, FormState formState, FormState sessionState) {
        String formType = formState.getFormType();
        Map<String, String> data = formState.getFormData();
        String currentDir = formState.getCurrentDirectory();
        
        // Store all form data temporarily in sessionState for confirmation
        sessionState.addData("pendingCreate_formType", formType);
        sessionState.addData("pendingCreate_uuaa", data.get("uuaa"));
        sessionState.addData("pendingCreate_code", data.get("code"));
        sessionState.addData("pendingCreate_className", data.get("className"));
        sessionState.addData("pendingCreate_version", data.getOrDefault("version", "01"));
        sessionState.addData("pendingCreate_country", data.getOrDefault("country", "GL"));
        sessionState.addData("pendingCreate_description", data.get("description"));
        sessionState.addData("pendingCreate_deploymentUnit", data.get("deploymentUnit"));
        sessionState.addData("pendingCreate_duName", data.get("duName"));
        sessionState.addData("pendingCreate_currentDir", currentDir);
        
        // Set confirmation flag
        sessionState.setAwaitingConfirmationFor("create-component-" + formType);
        
        // Return confirmation prompt
        return CommandResponse.info(ConfirmationMessages.STANDARD_CONFIRMATION);
    }
    
    /**
     * Execute confirmed create operation after user confirms
     * @param action the action string from confirmation (e.g., "create-component-dto")
     * @param sessionState session state containing pending data
     * @return result of create operation
     */
    public CommandResponse executeConfirmedCreate(String action, FormState sessionState) {
        // Retrieve stored form data
        String formType = sessionState.getData("pendingCreate_formType");
        String uuaa = sessionState.getData("pendingCreate_uuaa");
        String code = sessionState.getData("pendingCreate_code");
        String className = sessionState.getData("pendingCreate_className");
        String version = sessionState.getData("pendingCreate_version");
        String country = sessionState.getData("pendingCreate_country");
        String description = sessionState.getData("pendingCreate_description");
        String deploymentUnit = sessionState.getData("pendingCreate_deploymentUnit");
        String duName = sessionState.getData("pendingCreate_duName");
        String currentDir = sessionState.getData("pendingCreate_currentDir");
        
        // Clean up pending data
        sessionState.getFormData().remove("pendingCreate_formType");
        sessionState.getFormData().remove("pendingCreate_uuaa");
        sessionState.getFormData().remove("pendingCreate_code");
        sessionState.getFormData().remove("pendingCreate_className");
        sessionState.getFormData().remove("pendingCreate_version");
        sessionState.getFormData().remove("pendingCreate_country");
        sessionState.getFormData().remove("pendingCreate_description");
        sessionState.getFormData().remove("pendingCreate_deploymentUnit");
        sessionState.getFormData().remove("pendingCreate_duName");
        sessionState.getFormData().remove("pendingCreate_currentDir");
        
        try {
            // Check if we have a duName stored (from apx add command)
            if (duName != null) {
                switch (formType) {
                    case "dto":
                        return architectureService.createDtoInFolder(
                            duName,
                            uuaa, 
                            code, 
                            className, 
                            description
                        );
                    case "lib":
                        return architectureService.createLibInFolder(
                            duName,
                            uuaa, 
                            code, 
                            description
                        );
                    case "trx":
                        return architectureService.createTrxInFolder(
                            duName,
                            uuaa, 
                            code, 
                            version,
                            country,
                            description
                        );
                }
            }
            
            // Check if we're in a specific directory and should create object within that DU
            if (!"root".equals(currentDir) && currentDir != null && currentDir.contains("/")) {
                String[] pathParts = currentDir.split("/");
                if (pathParts.length == 2) {
                    duName = pathParts[0];
                    String folder = pathParts[1];
                    
                    // Create object within the specific DU folder
                    switch (formType) {
                        case "dto":
                            return architectureService.createDtoInFolder(
                                duName,
                                uuaa, 
                                code, 
                                className, 
                                description
                            );
                        case "lib":
                            return architectureService.createLibInFolder(
                                duName,
                                uuaa, 
                                code, 
                                description
                            );
                        case "trx":
                            return architectureService.createTrxInFolder(
                                duName,
                                uuaa, 
                                code, 
                                version,
                                country,
                                description
                            );
                    }
                }
            }
            
            // Default behavior - create standalone objects (when in root)
            switch (formType) {
                case "dto":
                    return architectureService.createDto(
                        uuaa, 
                        code, 
                        className, 
                        description
                    );
                case "lib":
                    return architectureService.createLib(
                        uuaa, 
                        code, 
                        description
                    );
                case "trx":
                    return architectureService.createTrx(
                        uuaa, 
                        code, 
                        version,
                        country,
                        description
                    );
                case "du-online":
                    return architectureService.createDuOnline(
                        uuaa, 
                        deploymentUnit, 
                        description
                    );
                case "du-lib":
                    return architectureService.createDuLib(
                        uuaa, 
                        code, 
                        description
                    );
                default:
                    return CommandResponse.error("Unknown form type: " + formType);
            }
        } catch (Exception e) {
            return CommandResponse.error("Error creating " + formType + ": " + e.getMessage());
        }
    }
}
