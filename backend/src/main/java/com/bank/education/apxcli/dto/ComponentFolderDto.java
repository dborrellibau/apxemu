package com.bank.education.apxcli.dto;

import com.bank.education.apxcli.model.ComponentFolder;
import java.util.List;
import java.util.stream.Collectors;

public class ComponentFolderDto {
    private Long id;
    private ComponentFolder.FolderType type;
    private String description;
    private List<DeploymentUnitDto> containedUnits;
    
    public ComponentFolderDto() {}
    
    public ComponentFolderDto(Long id, ComponentFolder.FolderType type, String description) {
        this.id = id;
        this.type = type;
        this.description = description;
    }
    
    public static ComponentFolderDto from(ComponentFolder folder) {
        ComponentFolderDto dto = new ComponentFolderDto(
            folder.getId(),
            folder.getType(),
            folder.getDescription()
        );
        
        // Convert contained units to DTOs
        dto.setContainedUnits(
            folder.getContainedUnits().stream()
                .map(DeploymentUnitDto::from)
                .collect(Collectors.toList())
        );
        
        return dto;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ComponentFolder.FolderType getType() {
        return type;
    }
    
    public void setType(ComponentFolder.FolderType type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<DeploymentUnitDto> getContainedUnits() {
        return containedUnits;
    }
    
    public void setContainedUnits(List<DeploymentUnitDto> containedUnits) {
        this.containedUnits = containedUnits;
    }
}