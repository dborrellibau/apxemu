package com.bank.education.apxcli.service.tutorial;

import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.model.tutorial.StepValidationType;
import com.bank.education.apxcli.model.tutorial.TutorialStep;
import com.bank.education.apxcli.navigation.PathNavigationService;
import com.bank.education.apxcli.navigation.model.PathType;
import org.springframework.stereotype.Service;

/**
 * Service responsible for validating tutorial step completion.
 * Handles different validation types (exact match, regex, path, custom).
 */
@Service
public class TutorialValidatorService {
    
    private final PathNavigationService pathNavigationService;
    
    public TutorialValidatorService(PathNavigationService pathNavigationService) {
        this.pathNavigationService = pathNavigationService;
    }
    
    /**
     * Validates if the executed command fulfills the step objective.
     * 
     * @param command Executed command
     * @param step Tutorial step with validation rules
     * @param sessionState Current session state
     * @return true if step is completed
     */
    public boolean validate(String command, TutorialStep step, FormState sessionState) {
        if (command == null || step == null) {
            return false;
        }
        
        StepValidationType validationType = step.getValidationType();
        
        switch (validationType) {
            case EXACT_MATCH:
                return validateExactMatch(command, step);
                
            case REGEX_PATTERN:
                return validateRegex(command, step);
                
            case PATH_VALIDATION:
                return validatePath(sessionState, step);
                
            case CUSTOM_VALIDATOR:
                return customValidate(command, step, sessionState);
                
            default:
                return false;
        }
    }
    
    /**
     * Validates exact command match (case-insensitive).
     */
    private boolean validateExactMatch(String command, TutorialStep step) {
        String expectedCommand = step.getExpectedCommand();
        if (expectedCommand == null) {
            return false;
        }
        return command.trim().equalsIgnoreCase(expectedCommand.trim());
    }
    
    /**
     * Validates command against regex pattern.
     */
    private boolean validateRegex(String command, TutorialStep step) {
        String pattern = step.getExpectedCommand();
        if (pattern == null) {
            return false;
        }
        try {
            return command.matches(pattern);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validates that user ended up in the correct path location.
     * Checks both PathType and directory path.
     */
    private boolean validatePath(FormState sessionState, TutorialStep step) {
        String currentDirectory = sessionState.getCurrentDirectory();
        if (currentDirectory == null) {
            return false;
        }
        
        // Validate PathType if specified
        String expectedPathTypeName = step.getExpectedPathType();
        if (expectedPathTypeName != null) {
            PathType currentPathType = pathNavigationService.resolvePathType(currentDirectory);
            if (currentPathType == null || !currentPathType.name().equals(expectedPathTypeName)) {
                return false;
            }
        }
        
        // Validate directory path if specified
        String expectedDirectory = step.getExpectedDirectory();
        if (expectedDirectory != null) {
            return currentDirectory.equals(expectedDirectory);
        }
        
        return true;
    }
    
    /**
     * Custom validation logic for complex scenarios.
     * Can be extended for specific validation cases.
     */
    private boolean customValidate(String command, TutorialStep step, FormState sessionState) {
        // Placeholder for custom validation logic
        // Can be implemented for specific step IDs or patterns
        // Example: Check if a DU was created with specific UUAA
        return true;
    }
}
