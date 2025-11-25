package com.bank.education.apxcli.strategy;

import java.util.List;

/**
 * Strategy for DTO deployment units
 * Simple objects without folders
 */
public class DtoStrategy extends SimpleDeploymentUnitStrategy {
    
    @Override
    public String getDescription() {
        return "Data Transfer Object - Simple object for data transfer";
    }
    
    @Override
    public List<String> getFormPrompts() {
        return java.util.Arrays.asList(
            "Enter Application (UUAA) - 4 uppercase letters:",
            "Enter DTO Code - 3 digits (001-999):",
            "Enter DTO class name:",
            "Enter Description:"
        );
    }
}