package com.bank.education.apxcli.service;

import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import org.springframework.stereotype.Service;

/**
 * Service responsible for validation and existence checks of Containable objects
 * Extracted from ArchitectureService to follow Single Responsibility Principle
 */
@Service
public class ContainableValidationService {
    
    private final DeploymentUnitRepository repository;
    
    public ContainableValidationService(DeploymentUnitRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Universal validation method that consolidates all existence checks
     * PATTERN: Single method handles multiple validation scenarios through parameterization
     */
    public boolean containableExists(String identifier, String type) {
        if (type != null) {
            DeploymentUnit.DeploymentUnitType unitType = parseType(type);
            if (unitType != null) {
                return repository.existsByTypeAndCode(unitType, identifier);
            }
        }
        return repository.existsByName(identifier);
    }
    
    /**
     * Check if a deployment unit exists by name
     */
    public boolean deploymentUnitExists(String name) {
        return containableExists(name, null);
    }
    
    /**
     * Check if a code exists for a specific type
     */
    public boolean isCodeExists(String type, String code) {
        return containableExists(code, type);
    }
    
    /**
     * Validates that a deployment unit name is available
     */
    public boolean isNameAvailable(String name) {
        return !repository.existsByName(name);
    }
    
    /**
     * Validates creation parameters for a deployment unit
     */
    public ValidationResult validateCreationParameters(String name, String uuaa, String code, String type) {
        ValidationResult result = new ValidationResult();
        
        if (name == null || name.trim().isEmpty()) {
            result.addError("Name cannot be empty");
        } else if (repository.existsByName(name)) {
            result.addError("Unit '" + name + "' already exists");
        }
        
        if (uuaa == null || uuaa.trim().isEmpty()) {
            result.addError("UUAA cannot be empty");
        }
        
        if (code != null && !code.trim().isEmpty()) {
            DeploymentUnit.DeploymentUnitType unitType = parseType(type);
            if (unitType != null && repository.existsByTypeAndCode(unitType, code)) {
                result.addError("Code '" + code + "' already exists for type " + type);
            }
        }
        
        return result;
    }
    
    /**
     * Helper method to parse type strings - extracted from ArchitectureService
     */
    private DeploymentUnit.DeploymentUnitType parseType(String type) {
        if (type == null) return null;
        
        switch (type.toLowerCase()) {
            case "du-online": return DeploymentUnit.DeploymentUnitType.DU_ONLINE;
            case "du-lib": return DeploymentUnit.DeploymentUnitType.DU_LIB;
            case "dto": case "dtos": return DeploymentUnit.DeploymentUnitType.DTO;
            case "lib": case "libs": return DeploymentUnit.DeploymentUnitType.LIB;
            case "lib-impl": return DeploymentUnit.DeploymentUnitType.LIB_IMPL;
            case "trx": case "trxs": return DeploymentUnit.DeploymentUnitType.TRX;
            default: return null;
        }
    }
    
    /**
     * Result class for validation operations
     */
    public static class ValidationResult {
        private boolean valid = true;
        private java.util.List<String> errors = new java.util.ArrayList<>();
        
        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public java.util.List<String> getErrors() {
            return errors;
        }
        
        public String getFirstError() {
            return errors.isEmpty() ? null : errors.get(0);
        }
    }
}