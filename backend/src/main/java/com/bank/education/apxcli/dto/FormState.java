package com.bank.education.apxcli.dto;

import java.util.HashMap;
import java.util.Map;

public class FormState {
    private String formType;
    private int currentStep;
    private Map<String, String> formData;
    private String currentDirectory; // For navigation: "root" or "<du-name>/<folder>"
    
    public FormState() {
        this.formData = new HashMap<>();
        this.currentDirectory = "root";
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
            return "apx> ";
        } else {
            return "apx:" + currentDirectory + "> ";
        }
    }
}