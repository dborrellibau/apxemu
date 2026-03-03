package com.bank.education.apxcli.model.tutorial;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class TutorialProgress {
    
    private boolean tutorialMode = false;
    
    private Long currentLevelId;
    private Integer currentStepNumber = 1;
    
    private Set<Long> completedLevelIds = new HashSet<>();
    
    private Integer totalStepsCompleted = 0;
    private Integer totalHintsUsed = 0;
    
    private LocalDateTime tutorialStartedAt;
    private LocalDateTime lastStepCompletedAt;
    
    // Constructor
    public TutorialProgress() {
    }
    
    // Getters and Setters
    public boolean isTutorialMode() {
        return tutorialMode;
    }
    
    public void setTutorialMode(boolean tutorialMode) {
        this.tutorialMode = tutorialMode;
    }
    
    public Long getCurrentLevelId() {
        return currentLevelId;
    }
    
    public void setCurrentLevelId(Long currentLevelId) {
        this.currentLevelId = currentLevelId;
    }
    
    public Integer getCurrentStepNumber() {
        return currentStepNumber;
    }
    
    public void setCurrentStepNumber(Integer currentStepNumber) {
        this.currentStepNumber = currentStepNumber;
    }
    
    public Set<Long> getCompletedLevelIds() {
        return completedLevelIds;
    }
    
    public void setCompletedLevelIds(Set<Long> completedLevelIds) {
        this.completedLevelIds = completedLevelIds;
    }
    
    public Integer getTotalStepsCompleted() {
        return totalStepsCompleted;
    }
    
    public void setTotalStepsCompleted(Integer totalStepsCompleted) {
        this.totalStepsCompleted = totalStepsCompleted;
    }
    
    public Integer getTotalHintsUsed() {
        return totalHintsUsed;
    }
    
    public void setTotalHintsUsed(Integer totalHintsUsed) {
        this.totalHintsUsed = totalHintsUsed;
    }
    
    public LocalDateTime getTutorialStartedAt() {
        return tutorialStartedAt;
    }
    
    public void setTutorialStartedAt(LocalDateTime tutorialStartedAt) {
        this.tutorialStartedAt = tutorialStartedAt;
    }
    
    public LocalDateTime getLastStepCompletedAt() {
        return lastStepCompletedAt;
    }
    
    public void setLastStepCompletedAt(LocalDateTime lastStepCompletedAt) {
        this.lastStepCompletedAt = lastStepCompletedAt;
    }
    
    // Helper methods
    
    /**
     * Checks if a level has been completed.
     */
    public boolean isLevelCompleted(Long levelId) {
        return completedLevelIds.contains(levelId);
    }
    
    /**
     * Marks a level as completed.
     */
    public void completeLevel(Long levelId) {
        completedLevelIds.add(levelId);
    }
    
    /**
     * Advances to the next step and updates statistics.
     */
    public void advanceStep() {
        currentStepNumber++;
        totalStepsCompleted++;
        lastStepCompletedAt = LocalDateTime.now();
    }
    
    /**
     * Increments hint usage counter.
     */
    public void useHint() {
        totalHintsUsed++;
    }
}
