package com.bank.education.apxcli.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "component_folders")
public class ComponentFolder implements Containable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FolderType type;
    
    @Column
    private String description;
    
    // Parent deployment unit (container like DU-ONLINE, DU-LIB)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_deployment_unit_id")
    private DeploymentUnit parentDeploymentUnit;
    
    // NEW: Parent ComponentFolder (for nested folders)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_component_folder_id")
    private ComponentFolder parentComponentFolder;
    
    // NEW: Child ComponentFolders (for nested folder structures)
    @OneToMany(mappedBy = "parentComponentFolder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ComponentFolder> childComponentFolders = new HashSet<>();
    
    // Deployment units contained within this folder
    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DeploymentUnit> containedUnits = new HashSet<>();
    
    // Constructors
    public ComponentFolder() {}
    
    public ComponentFolder(FolderType type, DeploymentUnit parentDeploymentUnit) {
        this.type = type;
        this.parentDeploymentUnit = parentDeploymentUnit;
    }
    
    public ComponentFolder(FolderType type, DeploymentUnit parentDeploymentUnit, String description) {
        this.type = type;
        this.parentDeploymentUnit = parentDeploymentUnit;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public FolderType getType() {
        return type;
    }
    
    public void setType(FolderType type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public DeploymentUnit getParentDeploymentUnit() {
        return parentDeploymentUnit;
    }
    
    public void setParentDeploymentUnit(DeploymentUnit parentDeploymentUnit) {
        this.parentDeploymentUnit = parentDeploymentUnit;
    }
    
    public Set<DeploymentUnit> getContainedUnits() {
        return containedUnits;
    }
    
    public void setContainedUnits(Set<DeploymentUnit> containedUnits) {
        this.containedUnits = containedUnits;
    }
    
    // Helper methods
    public void addContainedUnit(DeploymentUnit unit) {
        this.containedUnits.add(unit);
        unit.setParentFolder(this);
    }
    
    public void removeContainedUnit(DeploymentUnit unit) {
        this.containedUnits.remove(unit);
        unit.setParentFolder(null);
    }
    
    // NEW: Getter/Setter for parent ComponentFolder
    public ComponentFolder getParentComponentFolder() {
        return parentComponentFolder;
    }
    
    public void setParentComponentFolder(ComponentFolder parentComponentFolder) {
        this.parentComponentFolder = parentComponentFolder;
    }
    
    // NEW: Getter/Setter for child ComponentFolders
    public Set<ComponentFolder> getChildComponentFoldersSet() {
        return childComponentFolders;
    }
    
    public void setChildComponentFolders(Set<ComponentFolder> childComponentFolders) {
        this.childComponentFolders = childComponentFolders;
    }
    
    // ============== CONTAINABLE INTERFACE IMPLEMENTATION ==============
    
    @Override
    public String getName() {
        return this.type.name().toLowerCase() + (description != null ? " (" + description + ")" : "");
    }
    
    @Override
    public ContainerType getContainerType() {
        return ContainerType.COMPONENT_FOLDER;
    }
    
    @Override
    public List<DeploymentUnit> getChildDeploymentUnits() {
        return new ArrayList<>(containedUnits);
    }
    
    @Override
    public List<ComponentFolder> getChildComponentFolders() {
        return new ArrayList<>(childComponentFolders);
    }
    
    @Override
    public void addChildDeploymentUnit(DeploymentUnit unit) {
        this.containedUnits.add(unit);
        unit.setParentFolder(this);
    }
    
    @Override
    public void addChildComponentFolder(ComponentFolder folder) {
        this.childComponentFolders.add(folder);
        folder.setParentComponentFolder(this);
    }
    
    @Override
    public void removeChildDeploymentUnit(DeploymentUnit unit) {
        this.containedUnits.remove(unit);
        unit.setParentFolder(null);
    }
    
    @Override
    public void removeChildComponentFolder(ComponentFolder folder) {
        this.childComponentFolders.remove(folder);
        folder.setParentComponentFolder(null);
    }
    
    @Override
    public boolean canContain(ContainerType type) {
        return this.getContainerType().canContain(type);
    }
    
    @Override
    public Containable getParentContainer() {
        if (parentComponentFolder != null) {
            return parentComponentFolder;
        }
        return parentDeploymentUnit;
    }
    
    @Override
    public void setParentContainer(Containable parent) {
        if (parent instanceof ComponentFolder) {
            setParentComponentFolder((ComponentFolder) parent);
            setParentDeploymentUnit(null);
        } else if (parent instanceof DeploymentUnit) {
            setParentDeploymentUnit((DeploymentUnit) parent);
            setParentComponentFolder(null);
        }
    }
    
    public enum FolderType {
        LIBRARY("Library Components"),
        TRANSACTIONS("Business Transactions"), 
        DTO("Data Transfer Objects"),
        SRC("Source Code"),
        TEST("Test Code"),
        RESOURCES("Resources"),
        PARENT("Parent Container");
        
        private final String description;
        
        FolderType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}