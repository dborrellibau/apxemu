package com.bank.education.apxcli.dto;

import java.util.List;

public class CommandResponse {
    private boolean success;
    private String message;
    private List<String> output;
    private ResponseType type;
    private Object data;
    private String prompt; // Current prompt to show in terminal
    
    public CommandResponse() {}
    
    public CommandResponse(boolean success, String message, List<String> output, ResponseType type, Object data) {
        this.success = success;
        this.message = message;
        this.output = output;
        this.type = type;
        this.data = data;
        this.prompt = "vether> "; // Default prompt
    }
    
    public enum ResponseType {
        SUCCESS,
        ERROR,
        INFO,
        MENU,
        FORM,
        DIAGRAM_UPDATE
    }
    
    public static CommandResponse success(String message) {
        return new CommandResponse(true, message, null, ResponseType.SUCCESS, null);
    }
    
    public static CommandResponse success(String message, Object data) {
        return new CommandResponse(true, message, null, ResponseType.SUCCESS, data);
    }
    
    public static CommandResponse error(String message) {
        return new CommandResponse(false, message, null, ResponseType.ERROR, null);
    }
    
    public static CommandResponse info(String message) {
        return new CommandResponse(true, message, null, ResponseType.INFO, null);
    }
    
    public static CommandResponse menu(String message, List<String> options) {
        CommandResponse response = new CommandResponse(true, message, options, ResponseType.MENU, null);
        // Calculate max option number dynamically
        int maxOption = options != null ? options.size() : 0;
        // Store in data field as a map for extensibility
        if (maxOption > 0) {
            java.util.Map<String, Object> menuData = new java.util.HashMap<>();
            menuData.put("maxOption", maxOption);
            response.setData(menuData);
        }
        return response;
    }
    
    public static CommandResponse form(String message, Object formData) {
        return new CommandResponse(true, message, null, ResponseType.FORM, formData);
    }
    
    public static CommandResponse diagramUpdate(Object diagramData) {
        return new CommandResponse(true, "Diagram updated", null, ResponseType.DIAGRAM_UPDATE, diagramData);
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<String> getOutput() {
        return output;
    }
    
    public void setOutput(List<String> output) {
        this.output = output;
    }
    
    public ResponseType getType() {
        return type;
    }
    
    public void setType(ResponseType type) {
        this.type = type;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}