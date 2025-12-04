package com.bank.education.apxcli.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "deployment_units")
public class DeploymentUnit implements Containable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeploymentUnitType type;
    
    @Column(name = "uuaa", length = 4)
    private String uuaa;
    
    @Column(name = "code", length = 3)
    private String code;
    
    @Column(name = "class_name")
    private String className;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
    
    // Parent folder (for objects contained within folders like DTO, LIB, TRX)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder_id")
    private ComponentFolder parentFolder;
    
    // NEW: Parent DeploymentUnit (for hierarchical containers)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_deployment_unit_id")
    private DeploymentUnit parentDeploymentUnit;
    
    // NEW: Child DeploymentUnits (for containers like DU-LIB containing libraries)
    @OneToMany(mappedBy = "parentDeploymentUnit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DeploymentUnit> childDeploymentUnits = new HashSet<>();
    
    // Component folders (for containers like DU-ONLINE, DU-LIB)
    @OneToMany(mappedBy = "parentDeploymentUnit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ComponentFolder> componentFolders = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "deployment_unit_dependencies",
        joinColumns = @JoinColumn(name = "source_unit_id"),
        inverseJoinColumns = @JoinColumn(name = "target_unit_id")
    )
    private Set<DeploymentUnit> dependencies = new HashSet<>();
    
    // Constructors
    public DeploymentUnit() {
        this.createdAt = LocalDateTime.now();
    }
    
    public DeploymentUnit(String name, DeploymentUnitType type) {
        this();
        this.name = name;
        this.type = type;
    }
    
    public DeploymentUnit(String name, DeploymentUnitType type, String uuaa, String code, String className, String description) {
        this();
        this.name = name;
        this.type = type;
        this.uuaa = uuaa;
        this.code = code;
        this.className = className;
        this.description = description;
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
    
    public DeploymentUnitType getType() {
        return type;
    }
    
    public void setType(DeploymentUnitType type) {
        this.type = type;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUuaa() {
        return uuaa;
    }
    
    public void setUuaa(String uuaa) {
        this.uuaa = uuaa;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public Set<ComponentFolder> getComponentFolders() {
        return componentFolders;
    }
    
    public void setComponentFolders(Set<ComponentFolder> componentFolders) {
        this.componentFolders = componentFolders;
    }
    
    public ComponentFolder getParentFolder() {
        return parentFolder;
    }
    
    public void setParentFolder(ComponentFolder parentFolder) {
        this.parentFolder = parentFolder;
    }
    
    public Set<DeploymentUnit> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(Set<DeploymentUnit> dependencies) {
        this.dependencies = dependencies;
    }
    
    // Helper methods
    public void addDependency(DeploymentUnit dependency) {
        this.dependencies.add(dependency);
    }
    
    public void removeDependency(DeploymentUnit dependency) {
        this.dependencies.remove(dependency);
    }
    
    // NEW: Getter/Setter for parent DeploymentUnit
    public DeploymentUnit getParentDeploymentUnit() {
        return parentDeploymentUnit;
    }
    
    public void setParentDeploymentUnit(DeploymentUnit parentDeploymentUnit) {
        this.parentDeploymentUnit = parentDeploymentUnit;
    }
    
    // NEW: Getter/Setter for child DeploymentUnits
    public Set<DeploymentUnit> getChildDeploymentUnitsSet() {
        return childDeploymentUnits;
    }
    
    public void setChildDeploymentUnits(Set<DeploymentUnit> childDeploymentUnits) {
        this.childDeploymentUnits = childDeploymentUnits;
    }
    
    public boolean isDeleted() {
        return deleted;
    }
    
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
    // ============== CONTAINABLE INTERFACE IMPLEMENTATION ==============
    
    @Override
    public ContainerType getContainerType() {
        // Determine container type based on DeploymentUnitType
        switch (this.type) {
            case DU_ONLINE:
            case DU_LIB:
                return ContainerType.DEPLOYMENT_UNIT;
            default:
                return ContainerType.SIMPLE_OBJECT;
        }
    }
    
    @Override
    public List<DeploymentUnit> getChildDeploymentUnits() {
        return new ArrayList<>(childDeploymentUnits);
    }
    
    @Override
    public List<ComponentFolder> getChildComponentFolders() {
        return new ArrayList<>(componentFolders);
    }
    
    @Override
    public void addChildDeploymentUnit(DeploymentUnit unit) {
        this.childDeploymentUnits.add(unit);
        unit.setParentDeploymentUnit(this);
    }
    
    @Override
    public void addChildComponentFolder(ComponentFolder folder) {
        this.componentFolders.add(folder);
        folder.setParentDeploymentUnit(this);
    }
    
    @Override
    public void removeChildDeploymentUnit(DeploymentUnit unit) {
        this.childDeploymentUnits.remove(unit);
        unit.setParentDeploymentUnit(null);
    }
    
    @Override
    public void removeChildComponentFolder(ComponentFolder folder) {
        this.componentFolders.remove(folder);
        folder.setParentDeploymentUnit(null);
    }
    
    @Override
    public boolean canContain(ContainerType type) {
        return this.getContainerType().canContain(type);
    }
    
    @Override
    public Containable getParentContainer() {
        if (parentDeploymentUnit != null) {
            return parentDeploymentUnit;
        }
        return parentFolder;
    }
    
    @Override
    public void setParentContainer(Containable parent) {
        if (parent instanceof DeploymentUnit) {
            setParentDeploymentUnit((DeploymentUnit) parent);
            setParentFolder(null);
        } else if (parent instanceof ComponentFolder) {
            setParentFolder((ComponentFolder) parent);
            setParentDeploymentUnit(null);
        }
    }
    
    public enum DeploymentUnitType {
        DU_ONLINE("du-online"),
        DU_LIB("du-lib"),
        DTO("dto"), 
        LIB("lib"),
        LIB_IMPL("lib-impl"),
        TRX("trx");
        
        private final String value;
        
        DeploymentUnitType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static DeploymentUnitType fromString(String text) {
            for (DeploymentUnitType type : DeploymentUnitType.values()) {
                if (type.value.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            return DU_ONLINE; // default
        }
    }
}