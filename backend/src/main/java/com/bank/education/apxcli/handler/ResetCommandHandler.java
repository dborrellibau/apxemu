package com.bank.education.apxcli.handler;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.system.SystemCommandService;

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
    
    private final SystemCommandService systemCommandService;

    public static final int PRIORITY = 1;

    public ResetCommandHandler(SystemCommandService systemCommandService) {
        this.systemCommandService = systemCommandService;
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