package com.bank.education.apxcli.handler;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.system.SystemCommandService;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for the "reset" command.
 * 
 * Priority 0 - Highest priority to allow session reset in any state,
 * including during interactive flows or form input.
 * 
 * Clears the FormState and returns a session reset message.
 */
@Component
public class ResetCommandHandler extends CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(ResetCommandHandler.class);
    
    private final SystemCommandService systemCommandService;

    private final Map<String, FormState> activeSessions;
    public static final int PRIORITY = 1;

    public ResetCommandHandler(SystemCommandService systemCommandService,
        Map<String, FormState> activeSessions) {
        this.systemCommandService = systemCommandService;
        this.activeSessions = activeSessions;
    }
    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public boolean canHandle(CommandRequest request, FormState sessionState) {
        String cmd = request.getCommand();
        return cmd != null && cmd.trim().equalsIgnoreCase("reset");
    }

    @Override
    public CommandResponse handle(CommandRequest request, FormState sessionState) {
        String command = request.getCommand().toLowerCase().trim();
        log.info("ResetCommandHandler handling command: {}", command);
        if (command.equals("reset")) {
            return systemCommandService.handleResetSessionCommand(sessionState);
        } 
        
        return CommandResponse.error("Unknown command: " + command);
    }

    @Override
    public String getName() {
        return "ResetCommandHandler";
    }
}