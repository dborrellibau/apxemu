package com.bank.education.apxcli.dto;

import java.util.HashMap;
import java.util.Map;

public class FormState {
    private String formType;
    private int currentStep;
    private Map<String, String> formData;
    private String currentDirectory; // For navigation: "root" or "<du-name>/<folder>"
    private boolean awaitingComponentSelection; // Flag for apx add component selection
    
    // Dependency flow flags
    private boolean awaitingDependencySourceSelection; // Flag for selecting source component (levels 1-2)
    private boolean awaitingDependencyTypeSelection;   // Flag for selecting dependency type
    private boolean awaitingDependencyArtifactId;      // Flag for entering artifact ID
    
    public FormState() {
        this.formData = new HashMap<>();
        this.currentDirectory = "root";
        this.awaitingComponentSelection = false;
        this.awaitingDependencySourceSelection = false;
        this.awaitingDependencyTypeSelection = false;
        this.awaitingDependencyArtifactId = false;
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
    
    // Dependency flow getters and setters
    public boolean isAwaitingDependencySourceSelection() {
        return awaitingDependencySourceSelection;
    }
    
    public void setAwaitingDependencySourceSelection(boolean awaitingDependencySourceSelection) {
        this.awaitingDependencySourceSelection = awaitingDependencySourceSelection;
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
        this.awaitingDependencySourceSelection = false;
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
}