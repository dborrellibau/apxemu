package com.bank.education.apxcli.strategy;

import com.bank.education.apxcli.form.FormBuilder;

import java.util.List;

/**
 * Strategy for TRX deployment units
 * Transaction objects with version and country support
 */
public class TrxStrategy extends SimpleDeploymentUnitStrategy {
    
    @Override
    public String getDescription() {
        return "Transaction component - With version and country support";
    }
    
    @Override
    public List<String> getFormPrompts() {
        return FormBuilder.createTrxForm().stream()
                .map(field -> field.getPrompt())
                .collect(java.util.stream.Collectors.toList());
    }
}