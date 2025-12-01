package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.ContainableDto;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestration service that coordinates all architecture-related operations
 * Acts as a facade to maintain API compatibility while delegating to specialized services
 * Follows the Facade pattern to provide a unified interface to subsystems
 */
@Service
public class ArchitectureOrchestrationService {
    
    private final ContainableCreationService creationService;
    private final ContainableValidationService validationService;
    private final ContainableInfoService infoService;
    private final DependencyManagementService dependencyService;
    private final DeploymentUnitQueryService queryService;
    
    public ArchitectureOrchestrationService(
        ContainableCreationService creationService,
        ContainableValidationService validationService,
        ContainableInfoService infoService,
        DependencyManagementService dependencyService,
        DeploymentUnitQueryService queryService
    ) {
        this.creationService = creationService;
        this.validationService = validationService;
        this.infoService = infoService;
        this.dependencyService = dependencyService;
        this.queryService = queryService;
    }
    
    // ===== CREATION OPERATIONS =====
    
    public CommandResponse createDto(String uuaa, String code, String className, String description) {
        return creationService.createDto(uuaa, code, className, description);
    }
    
    public CommandResponse createLib(String uuaa, String code, String description) {
        return creationService.createLib(uuaa, code, description);
    }
    
    public CommandResponse createTrx(String uuaa, String code, String version, String country, String description) {
        return creationService.createTrx(uuaa, code, version, country, description);
    }
    
    public CommandResponse createDuOnline(String uuaa, String deploymentUnit, String description) {
        return creationService.createDuOnline(uuaa, deploymentUnit, description);
    }
    
    public CommandResponse createDuLib(String uuaa, String code, String description) {
        return creationService.createDuLib(uuaa, code, description);
    }
    
    public CommandResponse createDeploymentUnit(String type, String name) {
        return creationService.createDeploymentUnit(type, name);
    }
    
    // InFolder creation methods
    public CommandResponse createDtoInFolder(String duName, String uuaa, String code, String className, String description) {
        return creationService.createDtoInFolder(duName, uuaa, code, className, description);
    }
    
    public CommandResponse createLibInFolder(String duName, String uuaa, String code, String description) {
        return creationService.createLibInFolder(duName, uuaa, code, description);
    }
    
    public CommandResponse createTrxInFolder(String duName, String uuaa, String code, String version, String country, String description) {
        return creationService.createTrxInFolder(duName, uuaa, code, version, country, description);
    }
    
    public CommandResponse clearAllDeploymentUnits() {
        return creationService.clearAllDeploymentUnits();
    }
    
    // ===== VALIDATION OPERATIONS =====
    
    public boolean containableExists(String identifier, String type) {
        return validationService.containableExists(identifier, type);
    }
    
    public boolean deploymentUnitExists(String name) {
        return validationService.deploymentUnitExists(name);
    }
    
    public boolean isCodeExists(String type, String code) {
        return validationService.isCodeExists(type, code);
    }
    
    // ===== INFORMATION OPERATIONS =====
    
    public CommandResponse getDeploymentUnitDetails(String name) {
        return infoService.getDeploymentUnitDetails(name);
    }
    
    public CommandResponse debugDeploymentUnit(String duName) {
        return infoService.debugDeploymentUnit(duName);
    }
    
    public CommandResponse getContainableInfo(String name, boolean debugMode) {
        return infoService.getContainableInfo(name, debugMode);
    }
    
    // ===== DEPENDENCY OPERATIONS =====
    
    public CommandResponse createDependency(String sourceName, String targetName) {
        return dependencyService.createDependency(sourceName, targetName);
    }
    
    public CommandResponse removeDependency(String sourceName, String targetName) {
        return dependencyService.removeDependency(sourceName, targetName);
    }
    
    public CommandResponse validateDependency(String sourceName, String targetName) {
        return dependencyService.validateDependency(sourceName, targetName);
    }
    
    // ===== QUERY OPERATIONS =====
    
    public CommandResponse listDeploymentUnits(String type) {
        return queryService.listDeploymentUnits(type);
    }
    
    public List<ContainableDto> getAllDeploymentUnits() {
        return queryService.getAllDeploymentUnits();
    }
    
    public CommandResponse getDeploymentUnitCount(String type) {
        return queryService.getDeploymentUnitCount(type);
    }
    
    // ===== COMPLEX OPERATIONS (Coordinated) =====
    
    /**
     * Creates a deployment unit with validation and optional dependency setup
     */
    public CommandResponse createWithValidation(String type, String name, String uuaa, String code, String description) {
        // Validate first
        ContainableValidationService.ValidationResult validation = validationService.validateCreationParameters(name, uuaa, code, type);
        if (!validation.isValid()) {
            return CommandResponse.error(validation.getFirstError());
        }
        
        // Create the unit
        return creationService.createDeploymentUnit(type, name);
    }
    
    /**
     * Gets comprehensive unit information including dependencies
     */
    public CommandResponse getCompleteUnitInfo(String name) {
        if (!validationService.deploymentUnitExists(name)) {
            return CommandResponse.error("Unit '" + name + "' not found");
        }
        
        // Get basic info and debug info combined
        CommandResponse basicInfo = infoService.getDeploymentUnitDetails(name);
        CommandResponse debugInfo = infoService.getContainableInfo(name, true);
        
        if (basicInfo.isSuccess() && debugInfo.isSuccess()) {
            List<String> combinedOutput = new java.util.ArrayList<>(basicInfo.getOutput());
            combinedOutput.add("\n--- Debug Information ---");
            combinedOutput.addAll(debugInfo.getOutput());
            
            return new CommandResponse(
                true,
                "Complete information for '" + name + "'",
                combinedOutput,
                CommandResponse.ResponseType.INFO,
                basicInfo.getData()
            );
        }
        
        return basicInfo.isSuccess() ? basicInfo : debugInfo;
    }
    
    /**
     * Validates and creates a dependency between units
     */
    public CommandResponse createValidatedDependency(String sourceName, String targetName) {
        // Validate both units exist
        if (!validationService.deploymentUnitExists(sourceName)) {
            return CommandResponse.error("Source unit '" + sourceName + "' does not exist");
        }
        
        if (!validationService.deploymentUnitExists(targetName)) {
            return CommandResponse.error("Target unit '" + targetName + "' does not exist");
        }
        
        // Validate dependency rules
        CommandResponse validation = dependencyService.validateDependency(sourceName, targetName);
        if (!validation.isSuccess()) {
            return validation;
        }
        
        // Create the dependency
        return dependencyService.createDependency(sourceName, targetName);
    }
    
    /**
     * Lists components within a specific folder of a deployment unit
     */
    public CommandResponse listComponentsInFolder(String duName, String folder) {
        return infoService.listComponentsInFolder(duName, folder);
    }
    
    /**
     * Check if a component exists within a specific folder
     */
    public boolean componentExistsInFolder(String duName, String folder, String componentName) {
        return infoService.componentExistsInFolder(duName, folder, componentName);
    }
    
    /**
     * Get UUAA from a deployment unit by name
     */
    public String getDeploymentUnitUuaa(String duName) {
        return queryService.getDeploymentUnitUuaa(duName);
    }
}