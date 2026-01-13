package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.handler.CommandHandlerRegistry;
import com.bank.education.apxcli.navigation.PathNavigationService;
import com.bank.education.apxcli.navigation.model.PathType;
import com.bank.education.apxcli.service.educational.EducationalHintService;
import com.bank.education.apxcli.service.forms.AddComponentService;
import com.bank.education.apxcli.service.forms.FormInputService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simplified command parser orchestrator
 * 
 * Responsibilities:
 * 1. Session management (activeSessions map)
 * 2. Delegation to CommandHandlerRegistry for command routing
 * 3. Educational hint injection on successful commands
 * 4. Prompt management
 * 
 * All command processing logic has been moved to specialized CommandHandler implementations.
 * See com.bank.education.apxcli.handler package for handler implementations.
 */
@Service
public class CommandParserService {
    
    private final CommandHandlerRegistry handlerRegistry;
    private final PathNavigationService pathNavigationService;
    private final EducationalHintService hintService;
    private final AddComponentService addComponentService;
    private final FormInputService formInputService;
    
    private final Map<String, FormState> activeSessions = new ConcurrentHashMap<>();
    
    public CommandParserService(CommandHandlerRegistry handlerRegistry,
                               PathNavigationService pathNavigationService,
                               EducationalHintService hintService,
                               AddComponentService addComponentService,
                               FormInputService formInputService) {
        this.handlerRegistry = handlerRegistry;
        this.pathNavigationService = pathNavigationService;
        this.hintService = hintService;
        this.addComponentService = addComponentService;
        this.formInputService = formInputService;
        
        // Share activeSessions with form services (legacy compatibility)
        this.addComponentService.setActiveSessions(activeSessions);
        this.formInputService.setActiveSessions(activeSessions);
        
        // Clear any residual sessions on startup
        this.activeSessions.clear();
    }
    
    /**
     * Main entry point for command processing
     * Delegates to CommandHandlerRegistry which routes to appropriate handler
     * 
     * @param request Command request containing command text, args, and session ID
     * @return Command response with output, status, prompt, and optional hints
     */
    public CommandResponse parseCommand(CommandRequest request) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : "default";
        
        // Get or create session state
        FormState sessionState = getOrCreateSessionState(sessionId);
        
        // Delegate to handler registry (finds appropriate handler automatically)
        CommandResponse response = handlerRegistry.dispatch(request, sessionState);
        
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
        
        // Always set current prompt
        response.setPrompt(sessionState.getCurrentPrompt());
        
        return response;
    }
    
    /**
     * Gets or creates session state for the given session ID
     * 
     * @param sessionId Session identifier
     * @return FormState for this session
     */
    private FormState getOrCreateSessionState(String sessionId) {
        FormState state = activeSessions.get(sessionId);
        if (state == null) {
            state = new FormState();
            activeSessions.put(sessionId, state);
        }
        return state;
    }
    
    /**
     * Expose active sessions for handler access (legacy compatibility)
     * Required by ActiveFormHandler and InitMenuHandler
     * 
     * @return Map of active sessions
     */
    public Map<String, FormState> getActiveSessions() {
        return activeSessions;
    }
}
