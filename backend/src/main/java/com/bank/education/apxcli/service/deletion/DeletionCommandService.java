package com.bank.education.apxcli.service.deletion;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for handling "apx del" interactive deletion command
 * Supports context-aware deletion from navigation levels 0-3
 * 
 * Level 0 (root): Delete DU (du-online/du-lib)
 * Level 1 (du-name): Delete folders (dto/lib/trx) or special (dep/job/util)
 * Level 2 (du-name/folder): Delete components inside folder
 * Level 3 (du-name/folder/component): Delete component directly
 */
@Service
public class DeletionCommandService {
    
    private final ArchitectureOrchestrationService architectureService;
    private final DeploymentUnitRepository deploymentUnitRepository;
    
    public DeletionCommandService(
            ArchitectureOrchestrationService architectureService,
            DeploymentUnitRepository deploymentUnitRepository) {
        this.architectureService = architectureService;
        this.deploymentUnitRepository = deploymentUnitRepository;
    }
    
    /**
     * Main entry point for "apx del" command
     * Detects navigation level and shows appropriate deletion menu
     * 
     * Level 0 (root): Show DU deletion menu (du-online/du-lib only)
     * Level 1 (du-name): Show type selection menu (dep/dto/job/lib/trx/util)
     * Level 2 (du-name/folder): Show component selection from folder
     * Level 3 (du-name/folder/component): Confirm deletion of specific component
     */
    public CommandResponse handleDeleteCommand(FormState sessionState) {
        String currentDir = sessionState.getCurrentDirectory();
        
        // Level 0: root
        if ("root".equals(currentDir)) {
            return showRootDeletionMenu(sessionState);
        }
        
        // Parse navigation level
        String[] pathParts = currentDir.split("/");
        int level = pathParts.length;
        
        if (level == 1) {
            // Level 1: cd du-name
            return showDUContextMenu(sessionState, pathParts[0]);
        } else if (level == 2) {
            // Level 2: cd du-name/folder
            return showFolderContextMenu(sessionState, pathParts[0], pathParts[1]);
        } else if (level == 3) {
            // Level 3: cd du-name/folder/component
            return showComponentDeletionConfirmation(sessionState, pathParts[0], pathParts[1], pathParts[2]);
        } else {
            return CommandResponse.error("Invalid navigation level for deletion");
        }
    }
    
    /**
     * Level 0 (root): Show DU deletion menu
     * Options: 1. du-online, 2. du-lib
     */
    private CommandResponse showRootDeletionMenu(FormState sessionState) {
        StringBuilder menu = new StringBuilder();
        menu.append("╔══════════════════════════════════════════════════════════╗\n");
        menu.append("║          ELIMINACIÓN DE DEPLOYMENT UNIT                  ║\n");
        menu.append("╚══════════════════════════════════════════════════════════╝\n");
        menu.append("\n");
        menu.append("Seleccione el tipo de deployment unit a eliminar:\n");
        menu.append("\n");
        menu.append("  1. du-online    - Online Deployment Unit\n");
        menu.append("  2. du-lib       - Library Deployment Unit\n");
        menu.append("\n");
        menu.append("Ingrese el número de opción o el tipo: ");
        
        // Set deletion context
        sessionState.addData("deletionContext", "root");
        sessionState.addData("deletionStep", "type-selection");
        
        return CommandResponse.info(menu.toString());
    }
    
    /**
     * Level 1 (du-name): Show deletion type menu
     * Options: 1. dep, 2. dto, 3. job, 4. lib, 5. trx, 6. util
     */
    private CommandResponse showDUContextMenu(FormState sessionState, String duName) {
        // Verify DU exists
        DeploymentUnit du = deploymentUnitRepository.findByName(duName).orElse(null);
        if (du == null) {
            return CommandResponse.error("Deployment unit '" + duName + "' not found");
        }
        
        StringBuilder menu = new StringBuilder();
        menu.append("╔══════════════════════════════════════════════════════════╗\n");
        menu.append("║          ELIMINACIÓN EN: ").append(String.format("%-32s", duName)).append("║\n");
        menu.append("╚══════════════════════════════════════════════════════════╝\n");
        menu.append("\n");
        menu.append("Seleccione el tipo de elemento a eliminar:\n");
        menu.append("\n");
        menu.append("  1. dep     - Dependencia\n");
        menu.append("  2. dto     - Data Transfer Object\n");
        menu.append("  3. job     - Job\n");
        menu.append("  4. lib     - Library\n");
        menu.append("  5. trx     - Transaction\n");
        menu.append("  6. util    - Utility\n");
        menu.append("\n");
        menu.append("Ingrese el número de opción o el tipo: ");
        
        // Set deletion context
        sessionState.addData("deletionContext", "du-level");
        sessionState.addData("deletionDU", duName);
        sessionState.addData("deletionStep", "type-selection");
        
        return CommandResponse.info(menu.toString());
    }
    
    /**
     * Level 2 (du-name/folder): Show component list from folder
     */
    private CommandResponse showFolderContextMenu(FormState sessionState, String duName, String folderName) {
        // TODO ETAPA 4: List components in folder
        return CommandResponse.error("Component listing from folder not yet implemented");
    }
    
    /**
     * Level 3 (du-name/folder/component): Direct deletion confirmation
     */
    private CommandResponse showComponentDeletionConfirmation(FormState sessionState, String duName, String folderName, String componentName) {
        // TODO ETAPA 4: Show component info and request confirmation
        return CommandResponse.error("Direct component deletion not yet implemented");
    }
    
    /**
     * Placeholder for future ETAPA 6 - actual deletion execution
     */
    public CommandResponse executeConfirmedDelete(String action) {
        // Parse action string like "delete-component-123"
        return CommandResponse.error("Deletion execution not yet implemented. Action: " + action);
    }
}
