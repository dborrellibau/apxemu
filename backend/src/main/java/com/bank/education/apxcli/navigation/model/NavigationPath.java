package com.bank.education.apxcli.navigation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable value object representing a validated navigation path
 * 
 * Examples:
 * - root → []
 * - customer-service → ["customer-service"]
 * - customer-service/dto → ["customer-service", "dto"]
 * - customer-service/dto/customer-dto → ["customer-service", "dto", "customer-dto"]
 */
public final class NavigationPath {
    
    private final List<String> segments;
    private final int level;
    private final PathType type;
    private final PathType parentType;
    
    /**
     * Constructor
     */
    public NavigationPath(List<String> segments, PathType type, PathType parentType) {
        this.segments = Collections.unmodifiableList(new ArrayList<>(segments));
        this.level = segments.size();
        this.type = Objects.requireNonNull(type, "PathType cannot be null");
        this.parentType = parentType; // Can be null for root
    }
    
    // ========== GETTERS ==========
    
    public List<String> getSegments() {
        return segments;
    }
    
    public int getLevel() {
        return level;
    }
    
    public PathType getType() {
        return type;
    }
    
    public PathType getParentType() {
        return parentType;
    }
    
    // ========== PATH COMPONENTS ==========
    
    /**
     * Get deployment unit name (null if root)
     */
    public String getDuName() {
        return level >= 1 ? segments.get(0) : null;
    }
    
    /**
     * Get folder name (null if not in folder level)
     */
    public String getFolderName() {
        if (level >= 2 && type == PathType.FOLDER) {
            return segments.get(1);
        }
        if (level >= 2 && type == PathType.COMPONENT_IN_FOLDER) {
            return segments.get(1);
        }
        return null;
    }
    
    /**
     * Get component name (null if not a component)
     */
    public String getComponentName() {
        if (type == PathType.COMPONENT_STANDALONE) {
            return segments.get(0);
        }
        if (type == PathType.COMPONENT_IN_DULIB && level >= 2) {
            return segments.get(1);
        }
        if (type == PathType.COMPONENT_IN_FOLDER && level >= 3) {
            return segments.get(2);
        }
        return null;
    }
    
    // ========== PATH REPRESENTATIONS ==========
    
    /**
     * Get absolute path (internal format)
     * Examples: "root", "customer-service", "customer-service/dto"
     */
    public String getAbsolutePath() {
        if (level == 0) {
            return "root";
        }
        return String.join("/", segments);
    }
    
    /**
     * Get display path (user-friendly format)
     * Examples: "/vether", "/vether/customer-service", "/vether/customer-service/dto"
     */
    public String getDisplayPath() {
        if (level == 0) {
            return "/vether";
        }
        return "/vether/" + String.join("/", segments);
    }
    
    // ========== TYPE CHECKS ==========
    
    public boolean isRoot() {
        return type == PathType.ROOT;
    }
    
    public boolean isDu() {
        return type.isDeploymentUnit();
    }
    
    public boolean isFolder() {
        return type == PathType.FOLDER;
    }
    
    public boolean isComponent() {
        return type.isComponent();
    }
    
    public boolean canHaveChildren() {
        return type.canHaveChildren();
    }
    
    // ========== OBJECT METHODS ==========
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NavigationPath that = (NavigationPath) o;
        return level == that.level &&
               segments.equals(that.segments) &&
               type == that.type;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(segments, level, type);
    }
    
    @Override
    public String toString() {
        return "NavigationPath{" +
               "path='" + getAbsolutePath() + '\'' +
               ", level=" + level +
               ", type=" + type +
               '}';
    }
}
