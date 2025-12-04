package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.ContainableDto;
import com.bank.education.apxcli.dto.DeploymentUnitDto;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public CommandResponse listDeploymentUnits(String type) {
        List<DeploymentUnit> units;
        
        if (type != null) {
            DeploymentUnit.DeploymentUnitType unitType = parseType(type);
            
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
                String prefix = unit.getType().name().toLowerCase() + ": ";
                String name = unit.getName();
                String suffix = unit.isDeleted() ? " \u001B[31m[DELETED]\u001B[0m" : "";
                return prefix + name + suffix;
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
     * Gets all deployment units (raw entities)
     */
    public List<DeploymentUnit> getAllDeploymentUnitsRaw() {
        return repository.findAll();
    }
    
    /**
     * Searches deployment units by name pattern
     */
    public List<DeploymentUnit> searchDeploymentUnitsByName(String namePattern) {
        return repository.findAll().stream()
            .filter(unit -> unit.getName().toLowerCase().contains(namePattern.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets deployment units by UUAA
     */
    public List<DeploymentUnit> getDeploymentUnitsByUuaa(String uuaa) {
        return repository.findAll().stream()
            .filter(unit -> uuaa.equals(unit.getUuaa()))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets deployment units that are containers (have component folders)
     */
    public List<DeploymentUnit> getContainerDeploymentUnits() {
        return repository.findAll().stream()
            .filter(unit -> !unit.getComponentFolders().isEmpty())
            .collect(Collectors.toList());
    }
    
    /**
     * Gets deployment units that are standalone (not in folders, not child units)
     */
    public List<DeploymentUnit> getStandaloneDeploymentUnits() {
        return repository.findAll().stream()
            .filter(unit -> unit.getParentDeploymentUnit() == null && unit.getParentFolder() == null)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets count of deployment units by type
     */
    public CommandResponse getDeploymentUnitCount(String type) {
        if (type != null) {
            DeploymentUnit.DeploymentUnitType unitType = parseType(type);
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
     * Helper method to parse type strings
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
     * Get UUAA from a deployment unit by name
     */
    public String getDeploymentUnitUuaa(String duName) {
        return repository.findByName(duName)
            .map(DeploymentUnit::getUuaa)
            .orElse(null);
    }
}