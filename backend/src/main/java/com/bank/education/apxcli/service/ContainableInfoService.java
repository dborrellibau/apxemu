package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.ContainableDto;
import com.bank.education.apxcli.dto.DeploymentUnitDto;
import com.bank.education.apxcli.model.ComponentFolder;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

/**
 * Service responsible for providing information and debug details about Containable objects
 * Extracted from ArchitectureService to follow Single Responsibility Principle
 */
@Service
public class ContainableInfoService {
    
    private final DeploymentUnitRepository repository;
    
    public ContainableInfoService(DeploymentUnitRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Universal information method that consolidates details and debug
     * PATTERN: Single method handles both standard and debug modes through parameterization
     */
    public CommandResponse getContainableInfo(String name, boolean debugMode) {
        Optional<DeploymentUnit> unitOpt = repository.findByIdWithDependencies(
            repository.findByName(name).map(DeploymentUnit::getId).orElse(null)
        );
        
        if (!unitOpt.isPresent()) {
            return CommandResponse.error("Unit '" + name + "' not found");
        }
        
        DeploymentUnit unit = unitOpt.get();
        
        if (debugMode) {
            return generateDebugInfo(unit);
        } else {
            return generateStandardInfo(unit);
        }
    }
    
    /**
     * Get detailed information about a deployment unit
     */
    public CommandResponse getDeploymentUnitDetails(String name) {
        Optional<DeploymentUnit> unitOpt = repository.findByIdWithDependencies(
            repository.findByName(name).map(DeploymentUnit::getId).orElse(null)
        );
        
        if (!unitOpt.isPresent()) {
            return CommandResponse.error("Deployment unit '" + name + "' not found");
        }
        
        DeploymentUnit unit = unitOpt.get();
        DeploymentUnitDto dto = DeploymentUnitDto.from(unit);
        
        List<String> details = Arrays.asList(
            "Name: " + unit.getName(),
            "Type: " + unit.getType(),
            "Created: " + unit.getCreatedAt(),
            "Folders: " + unit.getComponentFolders().size(),
            "Dependencies: " + unit.getDependencies().size()
        );
        
        return new CommandResponse(
            true,
            "Details for '" + name + "'",
            details,
            CommandResponse.ResponseType.INFO,
            dto
        );
    }
    
    /**
     * Debug information for a deployment unit (backward compatibility)
     */
    public CommandResponse debugDeploymentUnit(String duName) {
        Optional<DeploymentUnit> duOpt = repository.findByName(duName);
        if (!duOpt.isPresent()) {
            return CommandResponse.error("Deployment unit '" + duName + "' not found");
        }
        
        return generateDebugInfo(duOpt.get());
    }
    
    /**
     * Generates debug information for a Containable
     */
    private CommandResponse generateDebugInfo(DeploymentUnit unit) {
        StringBuilder debug = new StringBuilder("Debug info for DU: " + unit.getName() + "\n");
        debug.append("Type: ").append(unit.getType()).append("\n");
        debug.append("Component Folders (").append(unit.getComponentFolders().size()).append("):\n");
        
        for (ComponentFolder folder : unit.getComponentFolders()) {
            debug.append("- ").append(folder.getType()).append(": ");
            if (folder.getContainedUnits().isEmpty()) {
                debug.append("Empty");
            } else {
                debug.append("Contains ").append(folder.getContainedUnits().size()).append(" unit(s):");
                for (DeploymentUnit contained : folder.getContainedUnits()) {
                    debug.append("\n  * ").append(contained.getName());
                    if (contained.getCode() != null) {
                        debug.append(" (Code: ").append(contained.getCode()).append(")");
                    }
                }
            }
            debug.append("\n");
        }
        
        // Add children info for Containable hierarchy
        if (!unit.getChildDeploymentUnits().isEmpty()) {
            debug.append("Child Units (").append(unit.getChildDeploymentUnits().size()).append("):\n");
            for (DeploymentUnit child : unit.getChildDeploymentUnits()) {
                debug.append("- ").append(child.getName()).append(" (").append(child.getType()).append(")\n");
            }
        }
        
        return CommandResponse.info(debug.toString());
    }
    
    /**
     * Generates standard information for a Containable
     */
    private CommandResponse generateStandardInfo(DeploymentUnit unit) {
        ContainableDto dto = ContainableDto.from(unit);
        
        List<String> details = Arrays.asList(
            "Name: " + unit.getName(),
            "Type: " + unit.getType(),
            "Created: " + unit.getCreatedAt(),
            "Folders: " + unit.getComponentFolders().size(),
            "Children: " + unit.getChildDeploymentUnits().size(),
            "Dependencies: " + unit.getDependencies().size()
        );
        
        return new CommandResponse(
            true,
            "Details for '" + unit.getName() + "'",
            details,
            CommandResponse.ResponseType.INFO,
            dto
        );
    }
    
    /**
     * Check if a unit exists (delegated method for info operations)
     */
    public boolean unitExists(String name) {
        return repository.existsByName(name);
    }
    
    /**
     * Get unit by name for info operations
     */
    public Optional<DeploymentUnit> findUnitByName(String name) {
        return repository.findByName(name);
    }
    
    /**
     * List components within a specific folder of a deployment unit
     */
    public CommandResponse listComponentsInFolder(String duName, String folderName) {
        Optional<DeploymentUnit> duOpt = repository.findByName(duName);
        if (!duOpt.isPresent()) {
            return CommandResponse.error("Deployment unit '" + duName + "' not found");
        }
        
        DeploymentUnit du = duOpt.get();
        
        // Map user folder name to internal folder type
        String internalFolderType = mapUserFolderToInternal(folderName, du);
        
        Optional<ComponentFolder> folderOpt = du.getComponentFolders().stream()
            .filter(folder -> internalFolderType.equals(folder.getType().toString().toLowerCase()))
            .findFirst();
            
        if (!folderOpt.isPresent()) {
            return CommandResponse.info("Folder '" + folderName + "' is empty in " + duName);
        }
        
        ComponentFolder folder = folderOpt.get();
        if (folder.getContainedUnits().isEmpty()) {
            return CommandResponse.info("Folder '" + folderName + "' is empty in " + duName);
        }
        
        // ETAPA 9: Add [DELETED] marker to deleted components
        List<String> componentList = new ArrayList<>();
        for (DeploymentUnit component : folder.getContainedUnits()) {
            String name = component.getName();
            String type = " (" + component.getType() + ")";
            String deletedMarker = component.isDeleted() ? " \u001B[31m[DELETED]\u001B[0m" : "";
            componentList.add(name + type + deletedMarker);
        }
            
        return new CommandResponse(
            true,
            "Contents of " + duName + "/" + folderName + ":",
            componentList,
            CommandResponse.ResponseType.INFO,
            null
        );
    }
    
    /**
     * Map user folder name to internal folder type for different DU types
     */
    private String mapUserFolderToInternal(String userFolderName, DeploymentUnit du) {
        if (du.getType() == DeploymentUnit.DeploymentUnitType.DU_LIB) {
            // For DU-LIB: detect by IMPL suffix
            return userFolderName.endsWith("IMPL") ? "impl" : "base";
        }
        
        // For DU-ONLINE: map user input to enum names
        // User writes: "dto", "library", "transactions"
        // Enum names: "DTO", "LIBRARY", "TRANSACTIONS"
        String lowerFolderName = userFolderName.toLowerCase();
        if ("dto".equals(lowerFolderName)) {
            return "dto";
        } else if ("library".equals(lowerFolderName)) {
            return "library";
        } else if ("transactions".equals(lowerFolderName)) {
            return "transactions";
        }
        
        return userFolderName.toLowerCase();
    }
    
    /**
     * Check if a component exists within a specific folder of a deployment unit
     */
    @Transactional(readOnly = true)
    public boolean componentExistsInFolder(String duName, String folderName, String componentName) {
        System.out.println("DEBUG: componentExistsInFolder called with duName=" + duName + ", folderName=" + folderName + ", componentName=" + componentName);
        
        Optional<DeploymentUnit> duOpt = repository.findByName(duName);
        System.out.println("DEBUG: DU found: " + duOpt.isPresent());
        
        if (!duOpt.isPresent()) {
            return false;
        }
        
        DeploymentUnit du = duOpt.get();
        
        // Map user folder name to internal folder type
        String internalFolderType = mapUserFolderToInternal(folderName, du);
        System.out.println("DEBUG: Internal folder type: " + internalFolderType);
        
        Optional<ComponentFolder> folderOpt = du.getComponentFolders().stream()
            .filter(folder -> internalFolderType.equals(folder.getType().toString().toLowerCase()))
            .findFirst();
        
        System.out.println("DEBUG: Target folder found: " + folderOpt.isPresent());
            
        if (!folderOpt.isPresent()) {
            return false;
        }
        
        ComponentFolder folder = folderOpt.get();
        
        System.out.println("DEBUG: Number of contained units: " + folder.getContainedUnits().size());
        folder.getContainedUnits().forEach(unit -> System.out.println("DEBUG: - Unit name: " + unit.getName()));
        
        // Check if component with given name exists in this folder
        boolean exists = folder.getContainedUnits().stream()
            .anyMatch(component -> componentName.equals(component.getName()));
        
        System.out.println("DEBUG: Component exists: " + exists);
        return exists;
    }

    /**
     * Lists all components across all folders in a deployment unit
     * Used for dependency flow when selecting source component from level 1 or 2
     * 
     * @param duName The deployment unit name
     * @return List of component names across all folders (dto, lib, trx)
     */
    public List<String> listAllComponentsInDU(String duName) {
        List<String> allComponents = new ArrayList<>();
        
        Optional<DeploymentUnit> duOpt = repository.findByName(duName);
        if (!duOpt.isPresent()) {
            return allComponents; // Empty list if DU not found
        }
        
        DeploymentUnit du = duOpt.get();
        
        // Iterate through all folders (dto, lib, trx)
        for (ComponentFolder folder : du.getComponentFolders()) {
            for (DeploymentUnit component : folder.getContainedUnits()) {
                allComponents.add(component.getName());
            }
        }
        
        return allComponents;
    }

    /**
     * Get formatted details for a deployment unit for 'apx show' command
     * Returns tree structure with folders and components
     * 
     * @param duName The deployment unit name
     * @return Formatted string with DU details and component tree
     */
    @Transactional(readOnly = true)
    public String getDeploymentUnitDetailsForShow(String duName) {
        Optional<DeploymentUnit> duOpt = repository.findByName(duName);
        if (!duOpt.isPresent()) {
            return "Deployment unit '" + duName + "' not found";
        }
        
        DeploymentUnit du = duOpt.get();
        StringBuilder output = new StringBuilder();
        
        // Header with artifact type
        String artifactType = du.getType().toString().toLowerCase().replace("du_", "du-");
        output.append("Artifact(").append(artifactType).append(")\n");
        output.append("- Artifact: ").append(du.getName()).append("\n");
        output.append("- Description: ").append(du.getDescription() != null ? du.getDescription() : "No description").append("\n");
        output.append("- Application(UUAA): ").append(du.getUuaa() != null ? du.getUuaa() : "N/A").append("\n\n");
        
        // Components section with tree
        output.append("Artifacts:\n");
        output.append("-----------\n");
        
        // Ensure folders are shown in order: dto, library, transactions
        String[] folderOrder = {"dto", "library", "transactions"};
        List<ComponentFolder> folders = new ArrayList<>();
        
        for (String folderName : folderOrder) {
            Optional<ComponentFolder> folderOpt = du.getComponentFolders().stream()
                .filter(f -> folderName.equals(f.getType().toString().toLowerCase()))
                .findFirst();
            
            if (folderOpt.isPresent()) {
                folders.add(folderOpt.get());
            } else {
                // Create virtual empty folder for display
                folders.add(null);
            }
        }
        
        // Build tree
        for (int i = 0; i < folders.size(); i++) {
            boolean isLast = (i == folders.size() - 1);
            String folderName = folderOrder[i];
            ComponentFolder folder = folders.get(i);
            
            // Count non-deleted components
            int count = 0;
            List<DeploymentUnit> components = new ArrayList<>();
            if (folder != null) {
                for (DeploymentUnit component : folder.getContainedUnits()) {
                    if (!component.isDeleted()) {
                        count++;
                        components.add(component);
                    }
                }
            }
            
            // Folder line
            String connector = isLast ? "╰─ " : "├─ ";
            output.append(connector).append(duName).append("\\").append(folderName).append(": ").append(count).append("\n");
            
            // Components under folder
            if (!components.isEmpty()) {
                String indent = isLast ? "   " : "│  ";
                
                for (int j = 0; j < components.size(); j++) {
                    boolean isLastComponent = (j == components.size() - 1);
                    String componentConnector = isLastComponent ? "╰─ " : "├─ ";
                    output.append(indent).append(componentConnector).append(components.get(j).getName()).append("\n");
                }
            }
        }
        
        return output.toString();
    }

    /**
     * Get formatted details for a component for 'apx show' command
     * Returns component info with dependencies list
     * 
     * @param componentName The component name
     * @return Formatted string with component details and dependencies
     */
    @Transactional(readOnly = true)
    public String getComponentDetailsForShow(String componentName) {
        Optional<DeploymentUnit> componentOpt = repository.findByName(componentName);
        
        if (!componentOpt.isPresent()) {
            return "Component '" + componentName + "' not found";
        }
        
        DeploymentUnit component = componentOpt.get();
        StringBuilder output = new StringBuilder();
        
        // Map type to lowercase format
        String typeStr = component.getType().toString().toLowerCase();
        if (typeStr.startsWith("du_")) {
            typeStr = typeStr.substring(3); // Remove "du_" prefix
        }
        typeStr = typeStr.replace("_", "-"); // lib_impl -> lib-impl
        
        // Header with artifact type
        output.append("Artifact(").append(typeStr).append(")\n");
        output.append("- Artifact: ").append(component.getName()).append("\n");
        output.append("- Description: ").append(component.getDescription() != null ? component.getDescription() : "No description").append("\n");
        output.append("- Application(UUAA): ").append(component.getUuaa() != null ? component.getUuaa() : "N/A").append("\n\n");
        
        // Dependencies section - force initialization within transaction
        Set<DeploymentUnit> allDeps = component.getDependencies();
        List<DeploymentUnit> dependencies = new ArrayList<>();
        for (DeploymentUnit dep : allDeps) {
            // Trigger proxy initialization
            dep.getName();
            if (!dep.isDeleted()) {
                dependencies.add(dep);
            }
        }
        
        output.append("Dependencies: ").append(dependencies.size()).append("\n");
        if (!dependencies.isEmpty()) {
            for (DeploymentUnit dep : dependencies) {
                output.append("- ").append(dep.getName()).append("\n");
            }
        }
        
        return output.toString();
    }
}