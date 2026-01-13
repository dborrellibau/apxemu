package com.bank.education.apxcli.handler;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.navigation.PathNavigationService;
import com.bank.education.apxcli.navigation.model.PathType;
import com.bank.education.apxcli.service.educational.EducationalHintService;
import com.bank.education.apxcli.service.navigation.NavigationCommandService;
import com.bank.education.apxcli.service.system.SystemCommandService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Handler for standard terminal commands (cd, ls, pwd, clear, exit)
 * 
 * Priority 100 (lowest) - these are the most generic commands,
 * should only execute if no interactive flow is active.
 * 
 * Delegates to:
 * - NavigationCommandService for cd, ls, pwd
 * - SystemCommandService for clear, exit
 * - EducationalHintService for post-command hints
 */
@Component
public class StandardCommandHandler extends CommandHandler {
    
    public static final int PRIORITY = 100;
    
    private static final List<String> STANDARD_COMMANDS = Arrays.asList(
        "cd", "pwd", "ls", "clear", "exit"
    );
    
    private final NavigationCommandService navigationService;
    private final SystemCommandService systemCommandService;
    private final PathNavigationService pathNavigationService;
    private final EducationalHintService hintService;
    
    public StandardCommandHandler(NavigationCommandService navigationService,
                                 SystemCommandService systemCommandService,
                                 PathNavigationService pathNavigationService,
                                 EducationalHintService hintService) {
        this.navigationService = navigationService;
        this.systemCommandService = systemCommandService;
        this.pathNavigationService = pathNavigationService;
        this.hintService = hintService;
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean canHandle(CommandRequest request, FormState sessionState) {
        String command = request.getCommand().toLowerCase().trim();
        return STANDARD_COMMANDS.contains(command);
    }
    
    @Override
    public CommandResponse handle(CommandRequest request, FormState sessionState) {
        String command = request.getCommand().toLowerCase().trim();
        String[] args = request.getArgs();
        
        CommandResponse response;
        
        switch (command) {
            case "cd":
                response = navigationService.handleCdCommand(sessionState, args);
                break;
            case "pwd":
                response = navigationService.handlePwdCommand(sessionState);
                break;
            case "ls":
                response = navigationService.handleLsCommand(sessionState, args);
                break;
            case "clear":
                response = systemCommandService.handleClearCommand();
                break;
            case "exit":
                response = systemCommandService.handleExitCommand();
                break;
            default:
                response = CommandResponse.error("Unknown command: " + command);
        }
        
        // Add educational hint if command was successful
        if (response.isSuccess()) {
            PathType currentPathType = pathNavigationService.resolvePathType(
                sessionState.getCurrentDirectory()
            );
            String hint = hintService.getHintFor(request.getCommand(), currentPathType);
            if (hint != null) {
                response.setEducationalHint(hint);
            }
        }
        
        return response;
    }
}
