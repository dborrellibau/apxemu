package com.bank.education.apxcli.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder pattern for creating forms with validation
 */
public class FormBuilder {
    private final List<FormField> fields = new ArrayList<>();
    
    public FormBuilder addField(String name, String prompt, FormField.FieldType type) {
        fields.add(new FormField(name, prompt, type));
        return this;
    }
    
    public FormBuilder addField(String name, String prompt, FormField.FieldType type, List<String> options) {
        fields.add(new FormField(name, prompt, type, options));
        return this;
    }
    
    public FormBuilder addUUAA() {
        return addField("uuaa", "Enter Application (UUAA) - 4 uppercase letters:", FormField.FieldType.UUAA);
    }
    
    public FormBuilder addCode(String type) {
        String prompt = getCodePrompt(type);
        return addField("code", prompt, FormField.FieldType.CODE);
    }
    
    public FormBuilder addVersion() {
        return addField("version", "Enter Version - 2 digits (01-99):", FormField.FieldType.VERSION);
    }
    
    public FormBuilder addCountry() {
        List<String> countries = Arrays.asList("AR", "CO", "ES", "GL", "MX", "PE", "US");
        return addField("country", "Select Country - Choose from: " + String.join(", ", countries) + ":", 
                       FormField.FieldType.COUNTRY_SELECT, countries);
    }
    
    public FormBuilder addClassName() {
        return addField("className", "Enter Class Name:", FormField.FieldType.CLASS_NAME);
    }
    
    public FormBuilder addDescription() {
        return addField("description", "Enter Description:", FormField.FieldType.DESCRIPTION);
    }
    
    public FormBuilder addDeploymentUnit() {
        return addField("deploymentUnit", "Enter Deployment Unit Name:", FormField.FieldType.DEPLOYMENT_UNIT);
    }
    
    public List<FormField> build() {
        return new ArrayList<>(fields);
    }
    
    private String getCodePrompt(String type) {
        switch (type.toLowerCase()) {
            case "dto":
                return "Enter DTO Code - 3 digits (001-999):";
            case "lib":
                return "Enter Library Code - 3 digits (001-999):";
            case "trx":
                return "Enter Transaction Code - 3 digits (001-999):";
            case "du-lib":
                return "Enter Library Container Code - 3 digits (001-999):";
            default:
                return "Enter Code - 3 digits (001-999):";
        }
    }
    
    // Static factory methods for common forms
    public static List<FormField> createDtoForm() {
        return new FormBuilder()
                .addUUAA()
                .addCode("dto")
                .addClassName()
                .addDescription()
                .build();
    }
    
    public static List<FormField> createLibForm() {
        return new FormBuilder()
                .addUUAA()
                .addCode("lib")
                .addDescription()
                .build();
    }
    
    public static List<FormField> createTrxForm() {
        return new FormBuilder()
                .addUUAA()
                .addCode("trx")
                .addVersion()
                .addCountry()
                .addDescription()
                .build();
    }
    
    public static List<FormField> createDuOnlineForm() {
        return new FormBuilder()
                .addUUAA()
                .addDeploymentUnit()
                .addDescription()
                .build();
    }
    
    public static List<FormField> createDuLibForm() {
        return new FormBuilder()
                .addUUAA()
                .addCode("du-lib")
                .addDescription()
                .build();
    }
}