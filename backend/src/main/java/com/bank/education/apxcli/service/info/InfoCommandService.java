package com.bank.education.apxcli.service.info;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service responsible for information and debugging commands
 */
@Service
public class InfoCommandService {
    
    private final ArchitectureOrchestrationService architectureService;
    
    public InfoCommandService(ArchitectureOrchestrationService architectureService) {
        this.architectureService = architectureService;
    }
    
    public CommandResponse handleListCommand(String[] args) {
        String type = args.length > 0 ? args[0] : null;
        return architectureService.listDeploymentUnits(type);
    }
    
    /**
     * Context-aware show command (ETAPA 1)
     * - Level 0 (root): NOT AVAILABLE - returns error
     * - Level 1 (du-name or standalone component): shows DU or component info
     * - Level 3 (du-name/folder/component): shows component info
     */
    public CommandResponse handleShowCommand(String[] args, FormState sessionState) {
        // Get current directory to detect navigation level
        String currentDir = sessionState.getCurrentDirectory();
        if (currentDir == null) currentDir = "root";
        
        int level = detectNavigationLevel(currentDir);
        
        // Level 0 (root): NOT AVAILABLE
        if (level == 0) {
            return CommandResponse.error("Command 'apx show' not available from root level");
        }
        
        // Level 1: DU or standalone component
        if (level == 1) {
            // currentDir is just the name (e.g., "delfina")
            return handleShowAtLevel1(currentDir);
        }
        
        // Level 3: Component inside folder
        if (level == 3) {
            // currentDir is like "delfina/dto/UUAAC001", extract component name
            String[] parts = currentDir.split("/");
            String componentName = parts[2];
            return handleShowAtLevel3(componentName);
        }
        
        // Level 2 (folder): not supported
        return CommandResponse.error("Command 'apx show' not available at folder level");
    }
    
    /**
     * Detect navigation level from currentDirectory
     * Level 0: "root"
     * Level 1: "delfina" (no slashes)
     * Level 2: "delfina/library" (1 slash)
     * Level 3: "delfina/library/COMP001" (2 slashes)
     */
    private int detectNavigationLevel(String currentDir) {
        if ("root".equals(currentDir)) return 0;
        
        // Count slashes to determine level
        int slashes = 0;
        for (char c : currentDir.toCharArray()) {
            if (c == '/') slashes++;
        }
        
        return slashes + 1; // 0 slashes = level 1, 1 slash = level 2, 2 slashes = level 3
    }
    
    /**
     * Handle show at level 1 - DU or standalone component
     * ETAPA 1: Returns placeholder message
     */
    private CommandResponse handleShowAtLevel1(String name) {
        return CommandResponse.info("[ETAPA 1] Show at level 1 for: " + name + " (not implemented yet)");
    }
    
    /**
     * Handle show at level 3 - component inside folder
     * ETAPA 1: Returns placeholder message
     */
    private CommandResponse handleShowAtLevel3(String componentName) {
        return CommandResponse.info("[ETAPA 1] Show at level 3 for: " + componentName + " (not implemented yet)");
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
