package com.bank.education.apxcli.dto;

import java.util.HashMap;
import java.util.Map;

public class FormState {
    private String formType;
    private int currentStep;
    private Map<String, String> formData;
    private String currentDirectory; // For navigation: "root" or "<du-name>/<folder>"
    private boolean awaitingComponentSelection; // Flag for apx add component selection
    private boolean awaitingInitSelection;      // Flag for apx init menu selection
    
    // Dependency flow flags
    private boolean awaitingDependencyTypeSelection;   // Flag for selecting dependency type
    private boolean awaitingDependencyArtifactId;      // Flag for entering artifact ID
    
    // Deletion flow flag - indicates user is in deletion menu/selection
    private boolean awaitingDeletionSelection;
    
    // In/Out flow flags (for apx add in/out)
    private boolean inOutSelectionMode;        // Flag for selecting in/out type (dto, group, list, parameters)
    private boolean awaitingInOutDtoName;      // Flag for entering DTO name
    
    // Confirmation flow - stores action string like "delete-component-123" or null
    private String awaitingConfirmationFor;
    
    public FormState() {
        this.formData = new HashMap<>();
        this.currentDirectory = "root";
        this.awaitingComponentSelection = false;
        this.awaitingInitSelection = false;
        this.awaitingDependencyTypeSelection = false;
        this.awaitingDependencyArtifactId = false;
        this.awaitingDeletionSelection = false;
        this.inOutSelectionMode = false;
        this.awaitingInOutDtoName = false;
        this.awaitingConfirmationFor = null;
    }
    
    public FormState(String formType) {
        this();
        this.formType = formType;
        this.currentStep = 0;
    }
    
    public String getFormType() {
        return formType;
    }
    
    public void setFormType(String formType) {
        this.formType = formType;
    }
    
    public int getCurrentStep() {
        return currentStep;
    }
    
    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }
    
    public Map<String, String> getFormData() {
        return formData;
    }
    
    public void setFormData(Map<String, String> formData) {
        this.formData = formData;
    }
    
    public void addData(String key, String value) {
        this.formData.put(key, value);
    }
    
    public String getData(String key) {
        return this.formData.get(key);
    }
    
    public void nextStep() {
        this.currentStep++;
    }
    
    public boolean isComplete() {
        if (formType == null) {
            return false;
        }
        switch (formType) {
            case "dto":
                return formData.containsKey("uuaa") && formData.containsKey("code") && 
                       formData.containsKey("className") && formData.containsKey("description");
            case "lib":
                return formData.containsKey("uuaa") && formData.containsKey("code") && 
                       formData.containsKey("description");
            case "trx":
                return formData.containsKey("uuaa") && formData.containsKey("code") && 
                       formData.containsKey("version") && formData.containsKey("country") &&
                       formData.containsKey("description");
            case "du-online":
                return formData.containsKey("uuaa") && formData.containsKey("deploymentUnit") && 
                       formData.containsKey("description");
            case "du-lib":
                return formData.containsKey("uuaa") && formData.containsKey("code") && 
                       formData.containsKey("description");
            default:
                return false;
        }
    }
    
    public String getCurrentDirectory() {
        return currentDirectory;
    }
    
    public void setCurrentDirectory(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }
    
    public String getCurrentPrompt() {
        if ("root".equals(currentDirectory)) {
            return "vether> ";
        } else {
            return "vether/" + currentDirectory + "> ";
        }
    }
    
    public boolean isAwaitingComponentSelection() {
        return awaitingComponentSelection;
    }
    
    public void setAwaitingComponentSelection(boolean awaitingComponentSelection) {
        this.awaitingComponentSelection = awaitingComponentSelection;
    }
    
    public boolean isAwaitingInitSelection() {
        return awaitingInitSelection;
    }
    
    public void setAwaitingInitSelection(boolean awaitingInitSelection) {
        this.awaitingInitSelection = awaitingInitSelection;
    }
    
    public boolean isAwaitingDependencyTypeSelection() {
        return awaitingDependencyTypeSelection;
    }
    
    public void setAwaitingDependencyTypeSelection(boolean awaitingDependencyTypeSelection) {
        this.awaitingDependencyTypeSelection = awaitingDependencyTypeSelection;
    }
    
    public boolean isAwaitingDependencyArtifactId() {
        return awaitingDependencyArtifactId;
    }
    
    public void setAwaitingDependencyArtifactId(boolean awaitingDependencyArtifactId) {
        this.awaitingDependencyArtifactId = awaitingDependencyArtifactId;
    }
    
    /**
     * Clears all dependency flow flags and temporary data
     */
    public void clearDependencyFlowData() {
        this.awaitingDependencyTypeSelection = false;
        this.awaitingDependencyArtifactId = false;
        clearPendingDepData();
    }
    
    /**
     * Clears temporary dependency data from formData map
     */
    public void clearPendingDepData() {
        this.formData.remove("depSourceComponent");
        this.formData.remove("depSourceType");
        this.formData.remove("depTargetType");
    }
    
    /**
     * Gets the pending confirmation action string
     * @return action string like "delete-component-123" or null if no confirmation pending
     */
    public String getAwaitingConfirmationFor() {
        return awaitingConfirmationFor;
    }
    
    /**
     * Sets the pending confirmation action string
     * @param action string describing what action needs confirmation (e.g., "delete-component-123")
     */
    public void setAwaitingConfirmationFor(String action) {
        this.awaitingConfirmationFor = action;
    }
    
    /**
     * Check if user is in deletion flow
     */
    public boolean isAwaitingDeletionSelection() {
        return awaitingDeletionSelection;
    }
    
    /**
     * Set deletion flow flag
     */
    public void setAwaitingDeletionSelection(boolean awaitingDeletionSelection) {
        this.awaitingDeletionSelection = awaitingDeletionSelection;
    }
    
    /**
     * Clear deletion flow data
     */
    public void clearDeletionFlowData() {
        this.awaitingDeletionSelection = false;
        this.formData.remove("deletionContext");
        this.formData.remove("deletionDU");
        this.formData.remove("deletionFolder");
        this.formData.remove("deletionStep");
        this.formData.remove("deletionComponentCount");
    }
    
    // In/Out flow getters and setters
    public boolean isInOutSelectionMode() {
        return inOutSelectionMode;
    }
    
    public void setInOutSelectionMode(boolean inOutSelectionMode) {
        this.inOutSelectionMode = inOutSelectionMode;
    }
    
    public boolean isAwaitingInOutDtoName() {
        return awaitingInOutDtoName;
    }
    
    public void setAwaitingInOutDtoName(boolean awaitingInOutDtoName) {
        this.awaitingInOutDtoName = awaitingInOutDtoName;
    }
    
    /**
     * Clears all in/out flow flags and temporary data
     */
    public void clearInOutFlowData() {
        this.inOutSelectionMode = false;
        this.awaitingInOutDtoName = false;
        this.formData.remove("inOutMode");
        this.formData.remove("inOutComponent");
    }

    public void reset() {
    this.formType = null;
    this.currentStep = 0;
    this.formData.clear();
    this.currentDirectory = "root";
    this.awaitingComponentSelection = false;
    this.awaitingInitSelection = false;
    this.awaitingDependencyTypeSelection = false;
    this.awaitingDependencyArtifactId = false;
    this.awaitingDeletionSelection = false;
    this.inOutSelectionMode = false;
    this.awaitingInOutDtoName = false;
    this.awaitingConfirmationFor = null;
}
}