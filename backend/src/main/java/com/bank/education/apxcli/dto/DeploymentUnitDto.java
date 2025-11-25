package com.bank.education.apxcli.dto;

import com.bank.education.apxcli.model.DeploymentUnit;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DeploymentUnitDto {
    private Long id;
    private String name;
    private String description;
    private DeploymentUnit.DeploymentUnitType type;
    private LocalDateTime createdAt;
    private List<ComponentFolderDto> componentFolders;
    private Set<String> dependencyNames;
    
    public DeploymentUnitDto() {}
    
    public DeploymentUnitDto(Long id, String name, String description, 
                           DeploymentUnit.DeploymentUnitType type, LocalDateTime createdAt,
                           List<ComponentFolderDto> componentFolders, Set<String> dependencyNames) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.createdAt = createdAt;
        this.componentFolders = componentFolders;
        this.dependencyNames = dependencyNames;
    }
    
    public static DeploymentUnitDto from(DeploymentUnit unit) {
        return new DeploymentUnitDto(
            unit.getId(),
            unit.getName(),
            unit.getDescription(),
            unit.getType(),
            unit.getCreatedAt(),
            unit.getComponentFolders().stream()
                .map(ComponentFolderDto::from)
                .collect(Collectors.toList()),
            unit.getDependencies().stream()
                .map(DeploymentUnit::getName)
                .collect(Collectors.toSet())
        );
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public DeploymentUnit.DeploymentUnitType getType() {
        return type;
    }
    
    public void setType(DeploymentUnit.DeploymentUnitType type) {
        this.type = type;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<ComponentFolderDto> getComponentFolders() {
        return componentFolders;
    }
    
    public void setComponentFolders(List<ComponentFolderDto> componentFolders) {
        this.componentFolders = componentFolders;
    }
    
    public Set<String> getDependencyNames() {
        return dependencyNames;
    }
    
    public void setDependencyNames(Set<String> dependencyNames) {
        this.dependencyNames = dependencyNames;
    }
}