package com.bank.education.apxcli.controller;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.service.CommandParserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class CommandController {
    
    private final CommandParserService commandParserService;
    
    public CommandController(CommandParserService commandParserService) {
        this.commandParserService = commandParserService;
    }
    
    @MessageMapping("/command")
    @SendTo("/topic/responses")
    public CommandResponse handleCommand(CommandRequest request) {
        System.out.println("DEBUG: Handling command - " + request.getCommand() + ", Session: " + request.getSessionId() + ", Thread: " + Thread.currentThread().getName());
        return commandParserService.parseCommand(request);
    }
}