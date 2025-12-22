package com.bank.education.apxcli.strategy;

import java.util.List;

/**
 * Strategy for LIB_IMPL deployment units
 * Implementation libraries - counterpart to base libraries
 */
public class LibImplStrategy extends SimpleDeploymentUnitStrategy {
    
    @Override
    public String getDescription() {
        return "Library Implementation - Implementation counterpart of base library";
    }
    
    @Override
    public List<String> getFormPrompts() {
        return java.util.Arrays.asList(
            "Enter Application (UUAA) - 4 letters (A-Z):",
            "Enter Library Implementation Code - 3 digits (000-999):",
            "Enter Description:"
        );
    }
}