package com.bank.education.apxcli.handler;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.forms.AddComponentService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for "apx init" menu selection (1-5 or type names)
 * 
 * Priority 30 - Handles selection from 5-option menu:
 * 1. DU Online
 * 2. DU Lib
 * 3. DTO
 * 4. LIB
 * 5. TRX
 * 
 * Activated after user executes "apx init" at ROOT level.
 * Accepts numeric (1-5) or type names (du-online, du-lib, dto, lib, trx).
 * 
 * Delegates to AddComponentService.startFormSession() to begin form wizard.
 */
@Component
public class InitMenuHandler extends CommandHandler {
    
    public static final int PRIORITY = 30;
    
    private static final Map<String, String> INIT_MENU_MAP;
    
    static {
        Map<String, String> map = new HashMap<>();
        map.put("1", "du-online");
        map.put("2", "du-lib");
        map.put("3", "dto");
        map.put("4", "lib");
        map.put("5", "trx");
        INIT_MENU_MAP = map;
    }
    
    private final AddComponentService addComponentService;
    private final Map<String, FormState> activeSessions;
    
    public InitMenuHandler(AddComponentService addComponentService,
                          Map<String, FormState> activeSessions) {
        this.addComponentService = addComponentService;
        this.activeSessions = activeSessions;
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean canHandle(CommandRequest request, FormState sessionState) {
        return sessionState.isAwaitingInitSelection();
    }
    
    @Override
    public CommandResponse handle(CommandRequest request, FormState sessionState) {
        String input = request.getCommand().trim().toLowerCase();
        
        // Validate input: must be 1-5 or valid type name
        if (!input.matches("^(\\d+|du-online|du-lib|dto|lib|trx)$")) {
            sessionState.setAwaitingInitSelection(false);
            return CommandResponse.error(
                "Invalid selection. Please choose 1-5 or type name (du-online, du-lib, dto, lib, trx)"
            );
        }
        
        // Convert numeric selection to type name
        String formType = INIT_MENU_MAP.getOrDefault(input, input);
        
        // Validate numeric range (1-5)
        if (input.matches("^\\d+$")) {
            int selection = Integer.parseInt(input);
            if (selection < 1 || selection > 5) {
                sessionState.setAwaitingInitSelection(false);
                return CommandResponse.error("Invalid selection. Please choose 1-5.");
            }
        }
        
        // Clear session state before starting new form
        activeSessions.remove(request.getSessionId());
        sessionState.setAwaitingInitSelection(false);
        
        // Start form session for selected component type
        return addComponentService.startFormSession(
            request.getSessionId(),
            formType,
            sessionState.getCurrentDirectory()
        );
    }
}
