package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.ContainableDto;
import com.bank.education.apxcli.dto.DeploymentUnitDto;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service responsible for querying and listing Containable objects
 * Extracted from ArchitectureService to follow Single Responsibility Principle
 */
@Service
public class DeploymentUnitQueryService {
    
    private final DeploymentUnitRepository repository;
    
    public DeploymentUnitQueryService(DeploymentUnitRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Lists deployment units, optionally filtered by type
     */
    @Transactional(readOnly = true)
    public CommandResponse listDeploymentUnits(String type) {
        List<DeploymentUnit> units;
        
        if (type != null) {
            DeploymentUnit.DeploymentUnitType unitType = DeploymentUnit.DeploymentUnitType.fromString(type);
            
            if (unitType == null) {
                return CommandResponse.error("Invalid type: " + type + ". Use: du-online, du-lib, dto, lib, or trx");
            }
            
            units = repository.findByType(unitType);
        } else {
            units = repository.findAll();
        }
        
        if (units.isEmpty()) {
            String message = type != null ? "No " + type + " units found" : "No deployment units found";
            return CommandResponse.info(message);
        }
        
        // ETAPA 9: Add [DELETED] marker to deleted units
        List<String> unitNames = units.stream()
            .map(unit -> {
                String name = unit.getName();
                String suffix = unit.isDeleted() ? " \u001B[31m[DELETED]\u001B[0m" : "";
                return name + suffix;
            })
            .collect(Collectors.toList());
        
        return new CommandResponse(
            true,
            "Found " + units.size() + " deployment unit(s)",
            unitNames,
            CommandResponse.ResponseType.INFO,
            units.stream().map(DeploymentUnitDto::from).collect(Collectors.toList())
        );
    }
    
    /**
     * Gets all deployment units as DTOs for diagram visualization
    @Transactional(readOnly = true)
     */
    public List<ContainableDto> getAllDeploymentUnits() {
        return repository.findAllWithFolders().stream()
            .filter(du -> du.getParentDeploymentUnit() == null && du.getParentFolder() == null) // Only root level units
            .map(ContainableDto::from)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets deployment units by type
     */
    public List<DeploymentUnit> getDeploymentUnitsByType(DeploymentUnit.DeploymentUnitType type) {
        return repository.findByType(type);
    }
    
    /**
     * Gets count of deployment units by type
     */
    public CommandResponse getDeploymentUnitCount(String type) {
        if (type != null) {
            DeploymentUnit.DeploymentUnitType unitType = DeploymentUnit.DeploymentUnitType.fromString(type);
            if (unitType == null) {
                return CommandResponse.error("Invalid type: " + type);
            }
            
            long count = repository.findByType(unitType).size();
            return CommandResponse.info("Found " + count + " " + type + " unit(s)");
        } else {
            long count = repository.count();
            return CommandResponse.info("Total deployment units: " + count);
        }
    }
    
    /**
     * Get UUAA from a deployment unit by name
     */
    public String getDeploymentUnitUuaa(String duName) {
        return repository.findByName(duName)
            .map(DeploymentUnit::getUuaa)
            .orElse(null);
    }

    public Optional<List<DeploymentUnit>> getComponentsInFolder(String duName, String folderName) {
        Optional<DeploymentUnit> duOpt = repository.findByName(duName);
        if (!duOpt.isPresent()) {
            return Optional.empty();
        }
        List<DeploymentUnit> components = duOpt.get().getComponentsInFolder(folderName);
        if (components.isEmpty()) {
            return Optional.of(new ArrayList<>());
        }
        return Optional.of(components);
    }
}