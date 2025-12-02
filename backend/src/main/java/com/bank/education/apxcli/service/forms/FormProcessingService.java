package com.bank.education.apxcli.service.forms;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
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
    
    public CommandResponse processCompleteForm(String sessionId, FormState formState) {
        String formType = formState.getFormType();
        Map<String, String> data = formState.getFormData();
        String currentDir = formState.getCurrentDirectory();
        
        try {
            // Check if we have a duName stored (from apx add command)
            String duName = data.get("duName");
            
            // If duName is present, create component within that DU
            if (duName != null) {
                switch (formType) {
                    case "dto":
                        return architectureService.createDtoInFolder(
                            duName,
                            data.get("uuaa"), 
                            data.get("code"), 
                            data.get("className"), 
                            data.get("description")
                        );
                    case "lib":
                        return architectureService.createLibInFolder(
                            duName,
                            data.get("uuaa"), 
                            data.get("code"), 
                            data.get("description")
                        );
                    case "trx":
                        return architectureService.createTrxInFolder(
                            duName,
                            data.get("uuaa"), 
                            data.get("code"), 
                            data.getOrDefault("version", "01"),
                            data.getOrDefault("country", "GL"),
                            data.get("description")
                        );
                }
            }
            
            // Check if we're in a specific directory and should create object within that DU
            if (!"root".equals(currentDir) && currentDir.contains("/")) {
                String[] pathParts = currentDir.split("/");
                if (pathParts.length == 2) {
                    duName = pathParts[0];
                    String folder = pathParts[1];
                    
                    // Create object within the specific DU folder
                    switch (formType) {
                        case "dto":
                            return architectureService.createDtoInFolder(
                                duName,
                                data.get("uuaa"), 
                                data.get("code"), 
                                data.get("className"), 
                                data.get("description")
                            );
                        case "lib":
                            return architectureService.createLibInFolder(
                                duName,
                                data.get("uuaa"), 
                                data.get("code"), 
                                data.get("description")
                            );
                        case "trx":
                            return architectureService.createTrxInFolder(
                                duName,
                                data.get("uuaa"), 
                                data.get("code"), 
                                data.getOrDefault("version", "01"),
                                data.getOrDefault("country", "GL"),
                                data.get("description")
                            );
                    }
                }
            }
            
            // Default behavior - create standalone objects (when in root)
            switch (formType) {
                case "dto":
                    return architectureService.createDto(
                        data.get("uuaa"), 
                        data.get("code"), 
                        data.get("className"), 
                        data.get("description")
                    );
                case "lib":
                    return architectureService.createLib(
                        data.get("uuaa"), 
                        data.get("code"), 
                        data.get("description")
                    );
                case "trx":
                    return architectureService.createTrx(
                        data.get("uuaa"), 
                        data.get("code"), 
                        data.getOrDefault("version", "01"),
                        data.getOrDefault("country", "GL"),
                        data.get("description")
                    );
                case "du-online":
                    return architectureService.createDuOnline(
                        data.get("uuaa"), 
                        data.get("deploymentUnit"), 
                        data.get("description")
                    );
                case "du-lib":
                    return architectureService.createDuLib(
                        data.get("uuaa"), 
                        data.get("code"), 
                        data.get("description")
                    );
                default:
                    return CommandResponse.error("Unknown form type: " + formType);
            }
        } catch (Exception e) {
            return CommandResponse.error("Error creating " + formType + ": " + e.getMessage());
        }
    }
}
