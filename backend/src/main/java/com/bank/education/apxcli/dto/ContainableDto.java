package com.bank.education.apxcli.dto;

import com.bank.education.apxcli.model.Containable;
import com.bank.education.apxcli.model.ContainerType;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.model.ComponentFolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Universal DTO for Containable objects (DeploymentUnit or ComponentFolder)
 * This allows the frontend to handle both types uniformly
 */
public class ContainableDto {
    
    private Long id;
    private String name;
    private String description;
    private ContainerType containerType;
    private String entityType; // "DeploymentUnit" or "ComponentFolder"
    
    // For DeploymentUnit specific fields
    private DeploymentUnit.DeploymentUnitType deploymentUnitType;
    private LocalDateTime createdAt;
    private String uuaa;
    private String code;
    private String className;
    
    // For ComponentFolder specific fields
    private ComponentFolder.FolderType folderType;
    
    // Hierarchical structure
    private List<ContainableDto> children;
    
    // Constructors
    public ContainableDto() {}
    
    // Static factory method
    public static ContainableDto from(Containable containable) {
        ContainableDto dto = new ContainableDto();
        
        dto.setId(containable.getId());
        dto.setName(containable.getName());
        dto.setContainerType(containable.getContainerType());
        
        if (containable instanceof DeploymentUnit) {
            DeploymentUnit du = (DeploymentUnit) containable;
            dto.setEntityType("DeploymentUnit");
            dto.setDescription(du.getDescription());
            dto.setDeploymentUnitType(du.getType());
            dto.setCreatedAt(du.getCreatedAt());
            dto.setUuaa(du.getUuaa());
            dto.setCode(du.getCode());
            dto.setClassName(du.getClassName());
            
        } else if (containable instanceof ComponentFolder) {
            ComponentFolder folder = (ComponentFolder) containable;
            dto.setEntityType("ComponentFolder");
            dto.setDescription(folder.getDescription());
            dto.setFolderType(folder.getType());
        }
        
        // Create children DTOs (both DeploymentUnits and ComponentFolders)
        List<ContainableDto> children = containable.getChildDeploymentUnits().stream()
                .map(ContainableDto::from)
                .collect(Collectors.toList());
        
        children.addAll(containable.getChildComponentFolders().stream()
                .map(ContainableDto::from)
                .collect(Collectors.toList()));
        
        dto.setChildren(children);
        
        return dto;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ContainerType getContainerType() { return containerType; }
    public void setContainerType(ContainerType containerType) { this.containerType = containerType; }
    
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    public DeploymentUnit.DeploymentUnitType getDeploymentUnitType() { return deploymentUnitType; }
    public void setDeploymentUnitType(DeploymentUnit.DeploymentUnitType deploymentUnitType) { this.deploymentUnitType = deploymentUnitType; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getUuaa() { return uuaa; }
    public void setUuaa(String uuaa) { this.uuaa = uuaa; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public ComponentFolder.FolderType getFolderType() { return folderType; }
    public void setFolderType(ComponentFolder.FolderType folderType) { this.folderType = folderType; }
    
    public List<ContainableDto> getChildren() { return children; }
    public void setChildren(List<ContainableDto> children) { this.children = children; }
}