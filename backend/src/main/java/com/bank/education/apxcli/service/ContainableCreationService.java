package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.DeploymentUnitDto;
import com.bank.education.apxcli.dto.ContainableDto;
import com.bank.education.apxcli.model.ComponentFolder;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import com.bank.education.apxcli.strategy.DeploymentUnitStrategy;
import com.bank.education.apxcli.strategy.DeploymentUnitStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service responsible for creating Containable objects
 * Extracted from ArchitectureService to follow Single Responsibility Principle
 */
@Service
@Transactional
public class ContainableCreationService {
    
    private final DeploymentUnitRepository repository;
    private final DiagramService diagramService;
    private final ContainableValidationService validationService;
    
    public ContainableCreationService(DeploymentUnitRepository repository, 
                                    DiagramService diagramService,
                                    ContainableValidationService validationService) {
        this.repository = repository;
        this.diagramService = diagramService;
        this.validationService = validationService;
    }
    
    /**
     * Universal method to create any Containable object, either standalone or within a container
     * PATTERN: Unifies all creation methods (standalone + InFolder)
     */
    public CommandResponse createContainable(String type, String parentContainer, 
                                           String uuaa, String code, String className, 
                                           String version, String country, String description, String customName) {
        
        DeploymentUnit.DeploymentUnitType unitType = DeploymentUnit.DeploymentUnitType.fromString(type);
        if (unitType == null) {
            return CommandResponse.error("Invalid type: " + type);
        }
        
        // Generate name if not provided
        String finalName = customName != null ? customName : generateName(unitType, uuaa, code, version, country);
        
        // If parentContainer is specified, create within that container
        if (parentContainer != null && !parentContainer.isEmpty()) {
            return createInContainer(parentContainer, unitType, finalName, uuaa, code, className, version, country, description);
        }
        
        // Otherwise create standalone
        return createUnitInternal(unitType, finalName, uuaa, code, className, version, country, description);
    }
    
    /**
     * Universal creation method for any type of DeploymentUnit
     * This is the core method that all other creation methods delegate to
     */
    private CommandResponse createUnitInternal(DeploymentUnit.DeploymentUnitType type, String name, 
                                    String uuaa, String code, String className, 
                                    String version, String country, String description) {
        
        System.out.println("DEBUG: Creating unit - Name: " + name + ", Type: " + type + ", Thread: " + Thread.currentThread().getName());
        
        if (repository.existsByName(name)) {
            return CommandResponse.error("Unit '" + name + "' already exists");
        }
        
        // Get strategy for this type
        DeploymentUnitStrategy strategy = DeploymentUnitStrategyFactory.getStrategy(type);
        
        // Validate using strategy
        CommandResponse validation = strategy.validateCreation(uuaa, name, description, className);
        if (!validation.isSuccess()) {
            return validation;
        }
        
        // Create unit
        DeploymentUnit unit = new DeploymentUnit(name, type, uuaa, code, className, description);
        
        // Apply strategy-specific logic
        applyStrategyLogic(unit, strategy, uuaa, code, description);
        
        // Save and notify
        DeploymentUnit saved = repository.save(unit);
        diagramService.notifyDiagramUpdate();
        
        return CommandResponse.success(
            "Created " + type.name() + " '" + name + "' - " + strategy.getDescription(),
            ContainableDto.from(saved)
        );
    }
    
    /**
     * Creates a Containable object within a specified container
     */
    private CommandResponse createInContainer(String containerName, DeploymentUnit.DeploymentUnitType type, 
                                            String name, String uuaa, String code, String className, 
                                            String version, String country, String description) {
        
        Optional<DeploymentUnit> containerOpt = repository.findByName(containerName);
        if (!containerOpt.isPresent()) {
            return CommandResponse.error("Container '" + containerName + "' not found");
        }
        
        DeploymentUnit container = containerOpt.get();
        
        // Find appropriate folder in container
        Optional<ComponentFolder> folderOpt = findAppropriateFolder(container, type);
        if (!folderOpt.isPresent()) {
            return CommandResponse.error("No appropriate folder found in " + containerName + " for " + type);
        }
        
        // Check if code already exists in this folder
        ComponentFolder folder = folderOpt.get();
        boolean codeExists = folder.getContainedUnits().stream()
            .anyMatch(unit -> code != null && code.equals(unit.getCode()));
        
        if (codeExists) {
            return CommandResponse.error(type.name() + " with code '" + code + "' already exists in " + containerName);
        }
        
        // Validate using strategy
        DeploymentUnitStrategy strategy = DeploymentUnitStrategyFactory.getStrategy(type);
        CommandResponse validation = strategy.validateCreation(uuaa, code, description, className);
        if (!validation.isSuccess()) {
            return validation;
        }
        
        // Create the object
        DeploymentUnit object = new DeploymentUnit(name, type, uuaa, code, className, description);
        
        // Add to folder using Containable interface
        folder.addContainedUnit(object);
        repository.save(object);
        diagramService.notifyDiagramUpdate();
        
        return CommandResponse.success(
            "Created " + type.name() + " '" + name + "' in " + containerName + "/" + folder.getType().name().toLowerCase(),
            DeploymentUnitDto.from(object)
        );
    }
    
    /**
     * Finds the appropriate folder for a given type within a container
     */
    private Optional<ComponentFolder> findAppropriateFolder(DeploymentUnit container, DeploymentUnit.DeploymentUnitType type) {
        ComponentFolder.FolderType targetFolderType;
        
        switch (type) {
            case DTO:
                targetFolderType = ComponentFolder.FolderType.DTO;
                break;
            case LIB:
            case LIB_IMPL:
                targetFolderType = ComponentFolder.FolderType.LIBRARY;
                break;
            case TRX:
                targetFolderType = ComponentFolder.FolderType.TRANSACTIONS;
                break;
            default:
                return Optional.empty();
        }
        
        return container.getComponentFolders().stream()
            .filter(folder -> folder.getType() == targetFolderType)
            .findFirst();
    }
    
    /**
     * Generates standard name for a deployment unit type
     */
    private String generateName(DeploymentUnit.DeploymentUnitType type, String uuaa, String code, String version, String country) {
        switch (type) {
            case DTO:
                return uuaa + "C" + code;
            case LIB:
                return uuaa + "R" + code;
            case LIB_IMPL:
                return uuaa + "R" + code + "IMPL";
            case TRX:
                String ver = version != null ? version : "01";
                String ctry = country != null ? country.toUpperCase() : "GL";
                return uuaa + "T" + code + "-" + ver + "-" + ctry;
            case DU_LIB:
                return uuaa + "R" + code + "-parent";
            case DU_ONLINE:
            default:
                return null; // These types need custom names
        }
    }
    
    /**
     * Applies strategy-specific creation logic
     */
    private void applyStrategyLogic(DeploymentUnit unit, DeploymentUnitStrategy strategy, 
                                  String uuaa, String code, String description) {
        
        // Add default folders if strategy supports them
        if (strategy.canContainFolders()) {
            unit.getComponentFolders().addAll(strategy.createDefaultFolders(unit));
        }
        
        // Handle special cases
        switch (unit.getType()) {
            case DU_LIB:
                // Create child libraries directly
                String baseName = uuaa + "R" + code;
                String implLibName = baseName + "IMPL";
                
                DeploymentUnit baseLib = new DeploymentUnit(
                    baseName, DeploymentUnit.DeploymentUnitType.LIB,
                    uuaa, code, null, description
                );
                
                DeploymentUnit implLib = new DeploymentUnit(
                    implLibName, DeploymentUnit.DeploymentUnitType.LIB_IMPL,
                    uuaa, code, null, description + " (Implementation)"
                );
                
                // Use Containable interface
                unit.addChildDeploymentUnit(baseLib);
                unit.addChildDeploymentUnit(implLib);
                break;
                
            case LIB:
            case LIB_IMPL:
            case DTO:
            case TRX:
            case DU_ONLINE:
            default:
                // No special logic required for these types
                break;
        }
    }
    
    // Public methods for creating specific types - delegates to universal method
    
    public CommandResponse createDto(String uuaa, String code, String className, String description) {
        return createContainable("dto", null, uuaa, code, className, null, null, description, null);
    }
    
    public CommandResponse createLib(String uuaa, String code, String description) {
        // For standalone libraries, create both base and impl
        return createLibStandaloneWithImpl(uuaa, code, description);
    }
    
    public CommandResponse createTrx(String uuaa, String code, String version, String country, String description) {
        return createContainable("trx", null, uuaa, code, null, version, country, description, null);
    }
    
    public CommandResponse createDuOnline(String uuaa, String deploymentUnit, String description) {
        return createContainable("du-online", null, uuaa, null, null, null, null, description, deploymentUnit);
    }
    
    public CommandResponse createDuLib(String uuaa, String code, String description) {
        return createContainable("du-lib", null, uuaa, code, null, null, null, description, null);
    }
    
    // Methods for creating objects within specific DU folders
    
    public CommandResponse createDtoInFolder(String duName, String uuaa, String code, String className, String description) {
        return createContainable("dto", duName, uuaa, code, className, null, null, description, null);
    }
    
    public CommandResponse createLibInFolder(String duName, String uuaa, String code, String description) {
        // For libraries in DU-ONLINE folders, create both base and impl
        return createLibInFolderWithImpl(duName, uuaa, code, description);
    }
    
    /**
     * Creates a library component with both base and impl versions in the library folder
     */
    private CommandResponse createLibInFolderWithImpl(String duName, String uuaa, String code, String description) {
        Optional<DeploymentUnit> containerOpt = repository.findByName(duName);
        if (!containerOpt.isPresent()) {
            return CommandResponse.error("Deployment unit '" + duName + "' not found");
        }
        
        DeploymentUnit container = containerOpt.get();
        
        // Find library folder in container
        Optional<ComponentFolder> folderOpt = container.getComponentFolders().stream()
            .filter(f -> f.getType() == ComponentFolder.FolderType.LIBRARY)
            .findFirst();
            
        if (!folderOpt.isPresent()) {
            return CommandResponse.error("No library folder found in " + duName);
        }
        
        ComponentFolder folder = folderOpt.get();
        
        // Build library names: UUAAR### (base) and UUAAR###IMPL (impl)
        String baseName = uuaa + "R" + code;
        String implName = baseName + "IMPL";
        
        // Check if base or impl already exist
        boolean baseExists = folder.getContainedUnits().stream()
            .anyMatch(unit -> baseName.equals(unit.getName()));
        boolean implExists = folder.getContainedUnits().stream()
            .anyMatch(unit -> implName.equals(unit.getName()));
            
        if (baseExists || implExists) {
            return CommandResponse.error("Library with code '" + code + "' already exists in " + duName + "/library");
        }
        
        // Validate using strategy
        DeploymentUnitStrategy strategy = DeploymentUnitStrategyFactory.getStrategy(DeploymentUnit.DeploymentUnitType.LIB);
        CommandResponse validation = strategy.validateCreation(uuaa, code, description, null);
        if (!validation.isSuccess()) {
            return validation;
        }
        
        // Create base library
        DeploymentUnit baseLib = new DeploymentUnit(baseName, DeploymentUnit.DeploymentUnitType.LIB, uuaa, code, null, description);
        folder.addContainedUnit(baseLib);
        repository.save(baseLib);
        
        // Create impl library
        DeploymentUnit implLib = new DeploymentUnit(implName, DeploymentUnit.DeploymentUnitType.LIB_IMPL, uuaa, code, null, description);
        folder.addContainedUnit(implLib);
        repository.save(implLib);
        
        diagramService.notifyDiagramUpdate();
        
        return CommandResponse.success(
            "Created LIB '" + baseName + "' and '" + implName + "' in " + duName + "/library"
        );
    }

        /**
     * Creates a standalone library component with both base and impl versions in root
     */
    private CommandResponse createLibStandaloneWithImpl(String uuaa, String code, String description) {
        // Build library names: UUAAR### (base) and UUAAR###IMPL (impl)
        String baseName = uuaa + "R" + code;
        String implName = baseName + "IMPL";
        
        // Check if base or impl already exist by name
        if (repository.existsByName(baseName)) {
            return CommandResponse.error("Library '" + baseName + "' already exists");
        }
        if (repository.existsByName(implName)) {
            return CommandResponse.error("Library '" + implName + "' already exists");
        }
        
        // Validate using strategy
        DeploymentUnitStrategy strategy = DeploymentUnitStrategyFactory.getStrategy(DeploymentUnit.DeploymentUnitType.LIB);
        CommandResponse validation = strategy.validateCreation(uuaa, code, description, null);
        if (!validation.isSuccess()) {
            return validation;
        }
        
        // Create base library (standalone in root)
        DeploymentUnit baseLib = new DeploymentUnit(baseName, DeploymentUnit.DeploymentUnitType.LIB, uuaa, code, null, description);
        repository.save(baseLib);
        
        // Create impl library (standalone in root)
        DeploymentUnit implLib = new DeploymentUnit(implName, DeploymentUnit.DeploymentUnitType.LIB_IMPL, uuaa, code, null, description);
        repository.save(implLib);
        
        diagramService.notifyDiagramUpdate();
        
        return CommandResponse.success(
            "Created LIB '" + baseName + "' and '" + implName + "' in root"
        );
    }

    public CommandResponse createTrxInFolder(String duName, String uuaa, String code, String version, String country, String description) {
        return createContainable("trx", duName, uuaa, code, null, version, country, description, null);
    }
    
    /**
     * Generic method to create deployment units by type and name
     */
    public CommandResponse createDeploymentUnit(String type, String name) {
        DeploymentUnit.DeploymentUnitType unitType = DeploymentUnit.DeploymentUnitType.fromString(type);
        
        if (unitType == null) {
            return CommandResponse.error("Unsupported deployment unit type: " + type);
        }
        
        return createUnitInternal(unitType, name, null, null, null, null, null, null);
    }
    
    /**
     * Clear all deployment units
     */
    public CommandResponse clearAllDeploymentUnits() {
        repository.deleteAll();
        diagramService.notifyDiagramUpdate();
        return CommandResponse.success("All deployment units cleared");
    }
}