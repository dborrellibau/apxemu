package com.bank.education.apxcli.service.tutorial;

import com.bank.education.apxcli.model.tutorial.TutorialLevel;
import com.bank.education.apxcli.model.tutorial.TutorialStep;

/**
 * DTO representing the result of a tutorial step validation.
 * Contains success/failure status and relevant messages.
 */
public class TutorialStepResult {
    
    private boolean success;
    private boolean inTutorialMode;
    private boolean tutorialCompleted;
    private boolean levelCompleted;
    
    private String successMessage;
    private String errorMessage;
    
    private Integer completedLevelNumber;
    private Integer totalSteps;
    private Integer hintsUsed;
    
    // Private constructor - use factory methods
    private TutorialStepResult() {
    }
    
    /**
     * Creates a success result after completing a step.
     */
    public static TutorialStepResult success(TutorialStep step) {
        TutorialStepResult result = new TutorialStepResult();
        result.success = true;
        result.inTutorialMode = true;
        result.successMessage = step.getSuccessMessage();
        return result;
    }
    
    /**
     * Creates a failure result when step validation fails.
     */
    public static TutorialStepResult failure(TutorialStep step) {
        TutorialStepResult result = new TutorialStepResult();
        result.success = false;
        result.inTutorialMode = true;
        result.errorMessage = step.getErrorMessage();
        return result;
    }
    
    /**
     * Creates a result indicating user is not in tutorial mode.
     */
    public static TutorialStepResult notInTutorialMode() {
        TutorialStepResult result = new TutorialStepResult();
        result.success = false;
        result.inTutorialMode = false;
        return result;
    }
    
    /**
     * Creates a result indicating tutorial is fully completed.
     */
    public static TutorialStepResult tutorialCompleted(Integer totalSteps, Integer hintsUsed) {
        TutorialStepResult result = new TutorialStepResult();
        result.success = true;
        result.inTutorialMode = false;
        result.tutorialCompleted = true;
        result.totalSteps = totalSteps;
        result.hintsUsed = hintsUsed;
        return result;
    }
    
    /**
     * Creates a result indicating a level was completed.
     */
    public static TutorialStepResult levelCompleted(Integer completedLevelNumber) {
        TutorialStepResult result = new TutorialStepResult();
        result.success = true;
        result.inTutorialMode = true;
        result.levelCompleted = true;
        result.completedLevelNumber = completedLevelNumber;
        return result;
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public boolean isInTutorialMode() {
        return inTutorialMode;
    }
    
    public boolean isTutorialCompleted() {
        return tutorialCompleted;
    }
    
    public boolean isLevelCompleted() {
        return levelCompleted;
    }
    
    public String getSuccessMessage() {
        return successMessage;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Integer getCompletedLevelNumber() {
        return completedLevelNumber;
    }
    
    public Integer getTotalSteps() {
        return totalSteps;
    }
    
    public Integer getHintsUsed() {
        return hintsUsed;
    }
}
