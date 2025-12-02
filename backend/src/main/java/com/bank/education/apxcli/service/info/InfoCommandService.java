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
    
    public CommandResponse handleShowCommand(String[] args) {
        if (args.length < 1) {
            return CommandResponse.error("Show command requires deployment unit name");
        }
        
        String name = args[0];
        return architectureService.getDeploymentUnitDetails(name);
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
