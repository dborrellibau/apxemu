package com.bank.education.apxcli.form;

import java.util.List;

/**
 * Represents a field in a form with validation and prompt information
 */
public class FormField {
    private final String name;
    private final String prompt;
    private final FieldType type;
    private final List<String> options; // For SELECT fields
    private final boolean required;
    
    public FormField(String name, String prompt, FieldType type) {
        this(name, prompt, type, null, true);
    }
    
    public FormField(String name, String prompt, FieldType type, List<String> options) {
        this(name, prompt, type, options, true);
    }
    
    public FormField(String name, String prompt, FieldType type, List<String> options, boolean required) {
        this.name = name;
        this.prompt = prompt;
        this.type = type;
        this.options = options;
        this.required = required;
    }
    
    public String getName() { return name; }
    public String getPrompt() { return prompt; }
    public FieldType getType() { return type; }
    public List<String> getOptions() { return options; }
    public boolean isRequired() { return required; }
    
    public enum FieldType {
        UUAA,           // 4 uppercase letters
        CODE,           // 3 digits (001-999)
        VERSION,        // 2 digits (01-99)
        COUNTRY_SELECT, // Select from predefined countries
        CLASS_NAME,     // Java class name
        DESCRIPTION,    // Free text description
        DEPLOYMENT_UNIT // Deployment unit name (no spaces)
    }
}