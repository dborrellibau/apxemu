package com.bank.education.apxcli.service.system;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

/**
 * Service responsible for system-level commands (init, clear, reset, exit)
 */
@Service
public class SystemCommandService {
    
    private static final Logger log = LoggerFactory.getLogger(SystemCommandService.class);
    
    private final ArchitectureOrchestrationService architectureService;
    
    public SystemCommandService(ArchitectureOrchestrationService architectureService) {
        this.architectureService = architectureService;
    }
    
    public CommandResponse handleInitCommand(String[] args, com.bank.education.apxcli.dto.FormState sessionState) {
        // Set flag to indicate we're awaiting init menu selection (1-5)
        sessionState.setAwaitingInitSelection(true);
        
        return CommandResponse.menu(
            "Select banking component type to initialize:",
            Arrays.asList(
                "1. du-online  - Deployment Unit Online (dto/transactions/library folders)",
                "2. du-lib     - Deployment Unit Library (base + impl)",
                "3. dto        - Data Transfer Object",
                "4. lib        - Library component (creates base + impl)",
                "5. trx        - Transaction component",
                "6. util       - Utility component (under construction)",
                "7. job        - Job component (under construction)",
                "8. du-batch   - Deployment Unit Batch (under construction)"
            )
        );
    }
    
    public CommandResponse handleClearCommand() {
        return architectureService.clearAllDeploymentUnits();
    }
    
    public CommandResponse handleResetSessionCommand(FormState sessionState) {
    
        if (sessionState != null) {
            sessionState.reset(); // Limpia todos los flags y datos de la sesión
        }
        log.info("State {} has been reset.", sessionState);
        return CommandResponse.success("Session reset. You can now start new forms.");
}
    
    public CommandResponse handleResetAllSessionsCommand(Map<String, FormState> activeSessions) {
        int cleared = activeSessions.size();
        activeSessions.clear();
        return CommandResponse.success("Cleared " + cleared + " sessions. System fully reset.");
    }
    
    public CommandResponse handleExitCommand() {
        return CommandResponse.success("Goodbye! Session terminated.");
    }
}
