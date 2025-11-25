package com.bank.education.apxcli.strategy;

import java.util.List;

/**
 * Strategy for LIB deployment units  
 * Base libraries - interface/contract definitions
 */
public class LibStrategy extends SimpleDeploymentUnitStrategy {
    
    @Override
    public String getDescription() {
        return "Library component - Creates base library and implementation automatically";
    }
    
    @Override
    public List<String> getFormPrompts() {
        return java.util.Arrays.asList(
            "Enter Application (UUAA) - 4 uppercase letters:",
            "Enter Library Code - 3 digits (001-999):",
            "Enter Description:"
        );
    }
}