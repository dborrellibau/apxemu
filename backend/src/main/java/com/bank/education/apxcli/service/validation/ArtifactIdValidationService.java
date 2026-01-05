package com.bank.education.apxcli.service.validation;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Service for validating artifact IDs based on component type patterns
 * 
 * Validation patterns:
 * - DTO: ^[A-Z]{4}C\d{3}$ (e.g., UUAAC001)
 * - LIB: ^[A-Z]{4}R\d{3}$ (e.g., UUAAR001)
 * - LIB_IMPL: ^[A-Z]{4}R\d{3}IMPL$ (e.g., UUAAR001IMPL)
 * - TRX: ^[A-Z]{4}T\d{3}-\d{2}-[A-Z]{2}$ (e.g., UUAAT001-01-AR)
 */
@Service
public class ArtifactIdValidationService {
    
    // Component type constants
    public static final String TYPE_DTO = "DTO";
    public static final String TYPE_LIB = "LIB";
    public static final String TYPE_LIB_IMPL = "LIB_IMPL";
    public static final String TYPE_TRX = "TRX";
    
    // Regex patterns for each component type
    private static final Pattern DTO_PATTERN = Pattern.compile("^[A-Z]{4}C\\d{3}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern LIB_PATTERN = Pattern.compile("^[A-Z]{4}R\\d{3}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern LIB_IMPL_PATTERN = Pattern.compile("^[A-Z]{4}R\\d{3}IMPL$", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRX_PATTERN = Pattern.compile("^[A-Z]{4}T\\d{3}-\\d{2}-[A-Z]{2}$", Pattern.CASE_INSENSITIVE);
    
    /**
     * Validates an artifact ID against the expected pattern for a component type
     * 
     * @param artifactId The artifact ID to validate
     * @param expectedType The expected component type (DTO, LIB, LIB_IMPL, TRX)
     * @return ValidationResult with success status and error message if applicable
     */
    public ValidationResult validateArtifactId(String artifactId, String expectedType) {
        if (artifactId == null || artifactId.trim().isEmpty()) {
            return ValidationResult.failure("Artifact ID cannot be empty");
        }
        
        artifactId = artifactId.trim();
        
        // Check if it matches the expected type pattern
        Pattern expectedPattern = getPatternForType(expectedType);
        if (!expectedPattern.matcher(artifactId).matches()) {
            return ValidationResult.failure(
                "Invalid artifact ID format for " + expectedType + ". Expected pattern: " + 
                getPatternDescription(expectedType)
            );
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Detects the component type from an artifact ID
     * 
     * @param artifactId The artifact ID to analyze
     * @return The detected type string (DTO, LIB, LIB_IMPL, TRX), or null if no pattern matches
     */
    public String detectTypeFromArtifactId(String artifactId) {
        if (artifactId == null || artifactId.trim().isEmpty()) {
            return null;
        }
        
        artifactId = artifactId.trim();
        
        if (LIB_IMPL_PATTERN.matcher(artifactId).matches()) {
            return TYPE_LIB_IMPL;
        }
        if (DTO_PATTERN.matcher(artifactId).matches()) {
            return TYPE_DTO;
        }
        if (LIB_PATTERN.matcher(artifactId).matches()) {
            return TYPE_LIB;
        }
        if (TRX_PATTERN.matcher(artifactId).matches()) {
            return TYPE_TRX;
        }
        
        return null;
    }
    
    /**
     * Checks if an artifact ID is a LIB_IMPL (which cannot be used as a dependency target)
     * 
     * @param artifactId The artifact ID to check
     * @return true if it's a LIB_IMPL, false otherwise
     */
    public boolean isLibImpl(String artifactId) {
        if (artifactId == null || artifactId.trim().isEmpty()) {
            return false;
        }
        return LIB_IMPL_PATTERN.matcher(artifactId.trim()).matches();
    }
    
    /**
     * Gets the regex pattern for a specific component type
     */
    private Pattern getPatternForType(String type) {
        switch (type) {
            case TYPE_DTO:
                return DTO_PATTERN;
            case TYPE_LIB:
                return LIB_PATTERN;
            case TYPE_LIB_IMPL:
                return LIB_IMPL_PATTERN;
            case TYPE_TRX:
                return TRX_PATTERN;
            default:
                throw new IllegalArgumentException("Unknown component type: " + type);
        }
    }
    
    /**
     * Gets a human-readable description of the pattern for a component type
     */
    private String getPatternDescription(String type) {
        switch (type) {
            case TYPE_DTO:
                return "4 uppercase letters + 'C' + 3 digits (e.g., UUAAC001)";
            case TYPE_LIB:
                return "4 uppercase letters + 'R' + 3 digits (e.g., UUAAR001)";
            case TYPE_LIB_IMPL:
                return "4 uppercase letters + 'R' + 3 digits + 'IMPL' (e.g., UUAAR001IMPL)";
            case TYPE_TRX:
                return "4 uppercase letters + 'T' + 3 digits + '-' + 2 digits + '-' + 2 uppercase letters (e.g., UUAAT001-01-AR)";
            default:
                return "Unknown pattern";
        }
    }
    
    /**
     * Result of a validation operation
     */
    public static class ValidationResult {
        private final boolean success;
        private final String errorMessage;
        
        private ValidationResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
