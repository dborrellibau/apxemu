package com.bank.education.apxcli.service.info;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.navigation.PathNavigationService;
import com.bank.education.apxcli.navigation.model.NavigationPath;
import com.bank.education.apxcli.navigation.model.PathType;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import com.bank.education.apxcli.service.ContainableInfoService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service responsible for information and debugging commands
 */
@Service
public class InfoCommandService {
    
    private final ArchitectureOrchestrationService architectureService;
    private final ContainableInfoService containableInfoService;
    private final PathNavigationService pathNavigationService;
    
    public InfoCommandService(
        ArchitectureOrchestrationService architectureService,
        ContainableInfoService containableInfoService,
        PathNavigationService pathNavigationService
    ) {
        this.architectureService = architectureService;
        this.containableInfoService = containableInfoService;
        this.pathNavigationService = pathNavigationService;
    }
    
    public CommandResponse handleListCommand(String[] args) {
        String type = args.length > 0 ? args[0] : null;
        return architectureService.listDeploymentUnits(type);
    }
    
    /**
     * Context-aware show command (ETAPA 10 - refactored with PathNavigationService)
     * - ROOT: NOT AVAILABLE - returns error
     * - DU_ONLINE/DU_LIB/COMPONENT_STANDALONE: shows DU or component info
     * - COMPONENT_IN_FOLDER/COMPONENT_IN_DULIB: shows component info
     * - FOLDER: not supported
     */
    public CommandResponse handleShowCommand(String[] args, FormState sessionState) {
        // Get current directory and PathType
        String currentDir = sessionState.getCurrentDirectory();
        if (currentDir == null) currentDir = "root";
        
        PathType pathType = pathNavigationService.resolvePathType(currentDir);
        
        // ROOT: NOT AVAILABLE
        if (pathType == PathType.ROOT) {
            return CommandResponse.error("Command 'apx show' not available from root level");
        }
        
        // FOLDER: not supported
        if (pathType == PathType.FOLDER) {
            return CommandResponse.error("Command 'apx show' not available at folder level");
        }
        
        // DU or standalone component (level 1)
        if (pathType == PathType.DU_ONLINE || pathType == PathType.DU_LIB || 
            pathType == PathType.COMPONENT_STANDALONE) {
            NavigationPath path = pathNavigationService.createPath(currentDir);
            String name = path.getDuName() != null ? path.getDuName() : currentDir;
            return handleShowAtLevel1(name);
        }
        
        // Component inside folder or DU_LIB (level 2-3)
        if (pathType == PathType.COMPONENT_IN_FOLDER || pathType == PathType.COMPONENT_IN_DULIB) {
            NavigationPath path = pathNavigationService.createPath(currentDir);
            String componentName = path.getComponentName();
            return handleShowAtLevel3(componentName);
        }
        
        return CommandResponse.error("Command 'apx show' not available at current location");
    }
    

    
    /**
     * Handle show at level 1 - DU or standalone component
     * ETAPA 2: Shows full DU tree with folders and components
     */
    private CommandResponse handleShowAtLevel1(String name) {
        String details = containableInfoService.getDeploymentUnitDetailsForShow(name);
        return CommandResponse.info(details);
    }
    
    /**
     * Handle show at level 3 - component inside folder
     * ETAPA 3: Shows component details with dependencies
     */
    private CommandResponse handleShowAtLevel3(String componentName) {
        String details = containableInfoService.getComponentDetailsForShow(componentName);
        return CommandResponse.info(details);
    }
    
    public CommandResponse handleDebugDuCommand(String[] args) {
        if (args.length == 0) {
            return CommandResponse.error("Usage: debug-du <du-name>");
        }
        
        String duName = args[0];
        return architectureService.debugDeploymentUnit(duName);
    }
    
    public CommandResponse handleDebugSessionsCommand(Map<String, FormState> activeSessions) {
        StringBuilder debug = new StringBuilder("Session Debug Info:\n");
        debug.append("Total active sessions: ").append(activeSessions.size()).append("\n");
        
        if (activeSessions.isEmpty()) {
            debug.append("No active sessions - system ready for new commands.");
        } else {
            for (Map.Entry<String, FormState> entry : activeSessions.entrySet()) {
                FormState state = entry.getValue();
                debug.append("Session ").append(entry.getKey())
                     .append(": type=").append(state.getFormType())
                     .append(", step=").append(state.getCurrentStep())
                     .append(", complete=").append(state.isComplete()).append("\n");
            }
        }
        
        return CommandResponse.success(debug.toString());
    }
}
