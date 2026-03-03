package com.bank.education.apxcli.model.tutorial;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tutorial_level")
public class TutorialLevel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false, unique = true)
    private Integer orderNumber;
    
    @Column(length = 10)
    private String badgeIcon;
    
    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    private List<TutorialStep> steps = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "tutorial_level_prerequisites", joinColumns = @JoinColumn(name = "level_id"))
    @Column(name = "prerequisite_level_id")
    private List<Long> prerequisiteLevelIds = new ArrayList<>();
    
    // Constructors
    public TutorialLevel() {
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
    
    public Integer getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public String getBadgeIcon() {
        return badgeIcon;
    }
    
    public void setBadgeIcon(String badgeIcon) {
        this.badgeIcon = badgeIcon;
    }
    
    public List<TutorialStep> getSteps() {
        return steps;
    }
    
    public void setSteps(List<TutorialStep> steps) {
        this.steps = steps;
    }
    
    public List<Long> getPrerequisiteLevelIds() {
        return prerequisiteLevelIds;
    }
    
    public void setPrerequisiteLevelIds(List<Long> prerequisiteLevelIds) {
        this.prerequisiteLevelIds = prerequisiteLevelIds;
    }
    
    // Helper methods
    
    /**
     * Adds a step to this level and sets the bidirectional relationship.
     */
    public void addStep(TutorialStep step) {
        steps.add(step);
        step.setLevel(this);
    }
    
    /**
     * Removes a step from this level.
     */
    public void removeStep(TutorialStep step) {
        steps.remove(step);
        step.setLevel(null);
    }
}
