package com.bank.education.apxcli.strategy;

import com.bank.education.apxcli.form.FormBuilder;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.dto.CommandResponse;

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
    
    @Override
    public boolean supportsInOutManagement() {
        return true;
    }
    
    @Override
    public CommandResponse addInput(DeploymentUnit unit, DeploymentUnit dto) {
        // Validate unit is TRX
        if (unit.getType() != DeploymentUnit.DeploymentUnitType.TRX) {
            return CommandResponse.error("Only transaction components can have inputs");
        }
        
        // Validate dto is DTO
        if (dto.getType() != DeploymentUnit.DeploymentUnitType.DTO) {
            return CommandResponse.error("Only DTOs can be added as inputs");
        }
        
        // Check if already exists
        if (unit.getInputs().contains(dto)) {
            return CommandResponse.error("DTO '" + dto.getName() + "' is already in inputs");
        }
        
        unit.addInput(dto);
        return CommandResponse.success("Message: The dto has been added correctly");
    }
    
    @Override
    public CommandResponse addOutput(DeploymentUnit unit, DeploymentUnit dto) {
        // Validate unit is TRX
        if (unit.getType() != DeploymentUnit.DeploymentUnitType.TRX) {
            return CommandResponse.error("Only transaction components can have outputs");
        }
        
        // Validate dto is DTO
        if (dto.getType() != DeploymentUnit.DeploymentUnitType.DTO) {
            return CommandResponse.error("Only DTOs can be added as outputs");
        }
        
        // Check if already exists
        if (unit.getOutputs().contains(dto)) {
            return CommandResponse.error("DTO '" + dto.getName() + "' is already in outputs");
        }
        
        unit.addOutput(dto);
        return CommandResponse.success("Message: The dto has been added correctly");
    }
    
    @Override
    public CommandResponse removeInput(DeploymentUnit unit, DeploymentUnit dto) {
        // Validate unit is TRX
        if (unit.getType() != DeploymentUnit.DeploymentUnitType.TRX) {
            return CommandResponse.error("Only transaction components can have inputs");
        }
        
        // Check if exists
        if (!unit.getInputs().contains(dto)) {
            return CommandResponse.error("DTO '" + dto.getName() + "' is not in inputs");
        }
        
        unit.removeInput(dto);
        return CommandResponse.success("Input '" + dto.getName() + "' removed successfully");
    }
    
    @Override
    public CommandResponse removeOutput(DeploymentUnit unit, DeploymentUnit dto) {
        // Validate unit is TRX
        if (unit.getType() != DeploymentUnit.DeploymentUnitType.TRX) {
            return CommandResponse.error("Only transaction components can have outputs");
        }
        
        // Check if exists
        if (!unit.getOutputs().contains(dto)) {
            return CommandResponse.error("DTO '" + dto.getName() + "' is not in outputs");
        }
        
        unit.removeOutput(dto);
        return CommandResponse.success("Output '" + dto.getName() + "' removed successfully");
    }
}