package com.bank.education.apxcli.strategy;

import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.form.FormBuilder;

import java.util.List;
import java.util.ArrayList;

/**
 * Strategy for DU-LIB deployment units
 * Contains base and impl library objects directly (no folders)
 */
public class DuLibStrategy extends SimpleDeploymentUnitStrategy {
    
    /**
     * Creates the base and impl library objects directly in the DU-LIB
     */
    public List<DeploymentUnit> createLibraryObjects(DeploymentUnit duLib, String uuaa, String code, String description) {
        String baseName = uuaa + "R" + code; // Format: UUAAR001
        String implName = baseName + "IMPL"; // Format: UUAAR001IMPL
        
        List<DeploymentUnit> libraries = new ArrayList<>();
        
        // Create base library object
        DeploymentUnit baseLib = new DeploymentUnit(
            baseName,
            DeploymentUnit.DeploymentUnitType.LIB,
            uuaa,
            code,
            null,
            description
        );
        
        // Create implementation library object
        DeploymentUnit implLib = new DeploymentUnit(
            implName,
            DeploymentUnit.DeploymentUnitType.LIB_IMPL,
            uuaa,
            code,
            null,
            description + " (Implementation)"
        );
        
        libraries.add(baseLib);
        libraries.add(implLib);
        
        return libraries;
    }
    
    @Override
    public String getDescription() {
        return "Deployment Unit Library - Container for base and implementation library objects";
    }
    
    @Override
    public List<String> getFormPrompts() {
        return FormBuilder.createDuLibForm().stream()
                .map(field -> field.getPrompt())
                .collect(java.util.stream.Collectors.toList());
    }
}