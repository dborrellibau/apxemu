package com.bank.education.apxcli.service.dependencies;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import com.bank.education.apxcli.service.DiagramService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

/**
 * Service responsible for managing dependencies between Containable objects
 * Extracted from ArchitectureService to follow Single Responsibility Principle
 */
@Service
@Transactional
public class DependencyManagementService {
    
    private final DeploymentUnitRepository repository;
    private final DiagramService diagramService;
    
    public DependencyManagementService(DeploymentUnitRepository repository, DiagramService diagramService) {
        this.repository = repository;
        this.diagramService = diagramService;
    }
    
    /**
     * Creates a dependency between two deployment units
     * Note: Circular dependency validation has been removed as per requirements
     */
    public CommandResponse createDependency(String sourceName, String targetName) {
        Optional<DeploymentUnit> sourceOpt = repository.findByName(sourceName);
        Optional<DeploymentUnit> targetOpt = repository.findByName(targetName);
        
        if (!sourceOpt.isPresent()) {
            return CommandResponse.error("Source deployment unit '" + sourceName + "' not found");
        }
        
        if (!targetOpt.isPresent()) {
            return CommandResponse.error("Target deployment unit '" + targetName + "' not found");
        }
        
        DeploymentUnit source = sourceOpt.get();
        DeploymentUnit target = targetOpt.get();
        
        if (source.getDependencies().contains(target)) {
            return CommandResponse.error("Dependency already exists between '" + sourceName + "' and '" + targetName + "'");
        }
        
        source.addDependency(target);
        repository.save(source);
        
        // Notify diagram service
        diagramService.notifyDiagramUpdate();
        
        return CommandResponse.success("Created dependency: " + sourceName + " -> " + targetName);
    }
    
    /**
     * Removes a dependency between two deployment units
     */
    public CommandResponse removeDependency(String sourceName, String targetName) {
        Optional<DeploymentUnit> sourceOpt = repository.findByName(sourceName);
        Optional<DeploymentUnit> targetOpt = repository.findByName(targetName);
        
        if (!sourceOpt.isPresent()) {
            return CommandResponse.error("Source deployment unit '" + sourceName + "' not found");
        }
        
        if (!targetOpt.isPresent()) {
            return CommandResponse.error("Target deployment unit '" + targetName + "' not found");
        }
        
        DeploymentUnit source = sourceOpt.get();
        DeploymentUnit target = targetOpt.get();
        
        if (!source.getDependencies().contains(target)) {
            return CommandResponse.error("No dependency exists between '" + sourceName + "' and '" + targetName + "'");
        }
        
        source.removeDependency(target);
        repository.save(source);
        
        // Notify diagram service
        diagramService.notifyDiagramUpdate();
        
        return CommandResponse.success("Removed dependency: " + sourceName + " -> " + targetName);
    }
    
    /**
     * Validates that creating a dependency would not create a circular dependency
     */
    public boolean wouldCreateCircularDependency(DeploymentUnit source, DeploymentUnit target) {
        return hasPath(target, source, new HashSet<>());
    }
    
    /**
     * Helper method to check if there's a path from 'from' to 'to' unit
     * Uses DFS to detect cycles
     */
    private boolean hasPath(DeploymentUnit from, DeploymentUnit to, Set<DeploymentUnit> visited) {
        if (visited.contains(from)) {
            return false; // Already visited this node
        }
        
        visited.add(from);
        
        for (DeploymentUnit dependency : from.getDependencies()) {
            if (dependency.equals(to)) {
                return true; // Direct path found
            }
            
            if (hasPath(dependency, to, visited)) {
                return true; // Indirect path found
            }
        }
        
        return false;
    }
    
    /**
     * Gets all dependencies of a deployment unit (direct and transitive)
     */
    public Set<DeploymentUnit> getAllDependencies(String unitName) {
        Optional<DeploymentUnit> unitOpt = repository.findByName(unitName);
        if (!unitOpt.isPresent()) {
            return new HashSet<>();
        }
        
        Set<DeploymentUnit> allDependencies = new HashSet<>();
        collectAllDependencies(unitOpt.get(), allDependencies, new HashSet<>());
        return allDependencies;
    }
    
    /**
     * Recursively collects all dependencies (direct and transitive)
     */
    private void collectAllDependencies(DeploymentUnit unit, Set<DeploymentUnit> collected, Set<DeploymentUnit> visited) {
        if (visited.contains(unit)) {
            return; // Avoid infinite recursion
        }
        
        visited.add(unit);
        
        for (DeploymentUnit dependency : unit.getDependencies()) {
            collected.add(dependency);
            collectAllDependencies(dependency, collected, visited);
        }
    }
    
}
