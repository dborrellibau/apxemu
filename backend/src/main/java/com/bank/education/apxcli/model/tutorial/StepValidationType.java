package com.bank.education.apxcli.model.tutorial;

 /**
 * Enum defining the types of validation for tutorial steps.
 * Each type determines how the user's command is validated against the step objective.
 */
public enum StepValidationType {
    
    /**
     * el comando debe matchear exacto (case-insensitive).
     * expectedCommand="ls" only accepts "ls"
     */
    EXACT_MATCH,
    
    /**
     * el comando debe matchear un regex.
     * Example: expectedCommand="cd DU-.*" accepts "cd DU-ONLINE-CUST", "cd DU-LIB-BASE"
     */
    REGEX_PATTERN,
    
    /**
     * Valida basado en la ubicación final del path (PathType + directorio).
     * Example: User must end up in PathType.DU_ONLINE at "DU-ONLINE-CUST"
     * Command used doesn't matter, only the resulting location.
     */
    PATH_VALIDATION,
    
    /**
     * Custom en TutorialValidatorService.
     * Used for complex validations (e.g., "create DU with specific UUAA")
     */
    CUSTOM_VALIDATOR
}
