package com.bank.education.apxcli.service.forms;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.form.FormField;
import com.bank.education.apxcli.service.ContainableValidationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for validating form field inputs
 */
@Service
public class FormValidationService {
    
    private final ContainableValidationService validationService;
    
    public FormValidationService(ContainableValidationService validationService) {
        this.validationService = validationService;
    }
    
    public CommandResponse validateInput(FormField field, String input, String formType, FormState formState) {
        if (input == null || input.trim().isEmpty()) {
            if (field.isRequired()) {
                return CommandResponse.error("Input cannot be empty. Try again:");
            }
            return CommandResponse.success("Valid input");
        }
        
        String trimmed = input.trim();
        
        switch (field.getType()) {
            case UUAA:
                return validateUUAA(trimmed);
            case CODE:
                return validateCode(trimmed, formType, formState);
            case VERSION:
                return validateVersion(trimmed);
            case COUNTRY_SELECT:
                return validateCountry(trimmed, field.getOptions());
            case CLASS_NAME:
                return validateClassName(trimmed);
            case DESCRIPTION:
                return validateDescription(trimmed);
            case DEPLOYMENT_UNIT:
                return validateDeploymentUnit(trimmed);
            default:
                return CommandResponse.success("Valid input");
        }
    }

    public String normalizeInput(FormField field, String input) {
        if (input == null) return null;
    
        String trimmed = input.trim();
    
        switch (field.getType()) {
            case UUAA:
                return trimmed.toUpperCase();
            default:
                return trimmed;
        }
    }
    
    private CommandResponse validateUUAA(String input) {
        String upper = input.toUpperCase();
        if (!upper.matches("^[A-Z]{4}$")) {
            return CommandResponse.error("UUAA must be exactly 4 letters (A-Z). Try again:");
        }
        return CommandResponse.success("Valid input");
    }
    
    private CommandResponse validateCode(String input, String formType, FormState formState) {
        if (!input.matches("^\\d{3}$")) {
            return CommandResponse.error("Code must be exactly 3 digits (001-999). Try again:");
        }
        
        // Special validation for DTO: check code+UUAA in context (standalone vs folder)
        if ("dto".equals(formType) && formState != null) {
            String uuaa = formState.getData("uuaa");
            if (uuaa == null || uuaa.trim().isEmpty()) {
                // UUAA not entered yet, skip context-specific validation
                // Will be validated later when UUAA is available
                return CommandResponse.success("Valid input");
            }
            
            String currentDir = formState.getCurrentDirectory();
            if (currentDir == null) {
                currentDir = "root";
            }
            
            // If in root: validate standalone DTO
            if ("root".equals(currentDir)) {
                if (validationService.isDtoCodeAndUuaaExistsInStandalone(input, uuaa)) {
                    return CommandResponse.error("DTO with code '" + input + "' and UUAA '" + uuaa + "' already exists in root. Try again:");
                }
            }
            // If in a folder: validation will happen at creation time in createInContainer
            // The creation service will handle the folder-specific validation
            
            return CommandResponse.success("Valid input");
        }
        
        // For other types (lib, trx), keep existing global validation
        if (validationService.containableExists(input, formType)) {
            return CommandResponse.error("Code " + input + " already exists for " + formType.toUpperCase() + ". Try again:");
        }
        
        return CommandResponse.success("Valid input");
    }
    
    private CommandResponse validateVersion(String input) {
        if (!input.matches("^\\d{2}$")) {
            return CommandResponse.error("Version must be exactly 2 digits (01-99). Try again:");
        }
        
        int version = Integer.parseInt(input);
        if (version < 1 || version > 99) {
            return CommandResponse.error("Version must be between 01 and 99. Try again:");
        }
        
        return CommandResponse.success("Valid input");
    }
    
    private CommandResponse validateCountry(String input, List<String> validCountries) {
        String upperInput = input.toUpperCase();
        if (!validCountries.contains(upperInput)) {
            return CommandResponse.error("Invalid country. Valid options: " + String.join(", ", validCountries) + ". Try again:");
        }
        return CommandResponse.success("Valid input");
    }
    
    private CommandResponse validateClassName(String input) {
        if (!input.matches("^[A-Za-z][A-Za-z0-9_]*$")) {
            return CommandResponse.error("Class name must start with a letter and contain only letters, numbers, and underscores. Try again:");
        }
        return CommandResponse.success("Valid input");
    }
    
    private CommandResponse validateDescription(String input) {
        int nonWhitespaceChars = input.replaceAll("\\s+", "").length();
        if (nonWhitespaceChars < 5) {
            return CommandResponse.error("Description must be at least 5 non-space characters long. Try again:");
        }
        return CommandResponse.success("Valid input");
    }
    
    private CommandResponse validateDeploymentUnit(String input) {
        if (input.contains(" ")) {
            return CommandResponse.error("Deployment Unit name cannot contain spaces. Try again:");
        }
        return CommandResponse.success("Valid input");
    }
}