package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.handler.CommandHandlerRegistry;
import com.bank.education.apxcli.handler.TutorialCommandHandler;
import com.bank.education.apxcli.navigation.PathNavigationService;
import com.bank.education.apxcli.navigation.model.PathType;
import com.bank.education.apxcli.service.educational.EducationalHintService;
import com.bank.education.apxcli.service.tutorial.TutorialStepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simplified command parser orchestrator
 * 
 * Responsibilities:
 * 1. Session management (activeSessions map)
 * 2. Delegation to CommandHandlerRegistry for command routing
 * 3. Tutorial mode integration and step validation
 * 4. Educational hint injection on successful commands
 * 5. Prompt management
 * 
 * All command processing logic has been moved to specialized CommandHandler
 * implementations.
 * See com.bank.education.apxcli.handler package for handler implementations.
 */
@Service
public class CommandParserService {

    private static final Logger log = LoggerFactory.getLogger(CommandParserService.class);

    private final CommandHandlerRegistry handlerRegistry;
    private final PathNavigationService pathNavigationService;
    private final EducationalHintService hintService;
    private final TutorialCommandHandler tutorialHandler;

    private final Map<String, FormState> activeSessions = new ConcurrentHashMap<>();

    public CommandParserService(CommandHandlerRegistry handlerRegistry,
            PathNavigationService pathNavigationService,
            EducationalHintService hintService,
            TutorialCommandHandler tutorialHandler) {
        this.handlerRegistry = handlerRegistry;
        this.pathNavigationService = pathNavigationService;
        this.hintService = hintService;
        this.tutorialHandler = tutorialHandler;

        // Clear any residual sessions on startup
        this.activeSessions.clear();
    }

    /**
     * Main entry point for command processing
     * Delegates to CommandHandlerRegistry which routes to appropriate handler
     * 
     * Tutorial integration:
     * - After command execution, validates if it fulfills current tutorial step
     * - Provides tutorial hints instead of normal educational hints when tutorial is active
     * - Shows step completion messages and level progression feedback
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

        // Check if handler wants to replace the session state
        if (response.getNewSessionState() != null) {
            activeSessions.put(sessionId, response.getNewSessionState());
            // Update sessionState reference for hint/prompt processing below
            sessionState = response.getNewSessionState();
        }

        // Tutorial mode integration
        boolean isTutorialActive = tutorialHandler.isInTutorialMode(sessionState);
        boolean isTutorialCommand = request.getCommand().trim().toLowerCase().startsWith("tutorial");
        
        // If tutorial is active and command executed successfully (but not a tutorial command itself)
        if (isTutorialActive && !isTutorialCommand && response.isSuccess()) {
            TutorialStepResult stepResult = tutorialHandler.validateStepAfterExecution(
                buildFullCommand(request), 
                sessionState
            );
            
            // Process tutorial validation result
            response = processTutorialStepResult(response, stepResult, sessionState);
        }
        
        // Add educational hint based on mode
        // Note: Skip if tutorial command (it manages its own hints)
        if (isTutorialActive && !isTutorialCommand) {
            // Tutorial mode: always show tutorial hint (even if command failed)
            String tutorialHint = tutorialHandler.generateTutorialHint(sessionState);
            if (tutorialHint != null) {
                response.setEducationalHint(tutorialHint);
            }
        } else if (!isTutorialActive && response.isSuccess()) {
            // Normal mode: show contextual educational hint only on success
            PathType currentPathType = pathNavigationService.resolvePathType(
                    sessionState.getCurrentDirectory());
            String fullCommand = buildFullCommand(request);
            String hint = hintService.getHintFor(fullCommand, currentPathType);
            if (hint != null) {
                response.setEducationalHint(hint);
            }
        }

        // Always set current prompt
        response.setPrompt(sessionState.getCurrentPrompt());

        // Log final response state
        log.info("Final response - command: {}, isTutorialCommand: {}, hint present: {}, hint length: {}", 
            request.getCommand(), 
            isTutorialCommand,
            response.getEducationalHint() != null,
            response.getEducationalHint() != null ? response.getEducationalHint().length() : 0);

        return response;
    }
    
    /**
     * Builds full command string from request (command + args)
     */
    private String buildFullCommand(CommandRequest request) {
        String fullCommand = request.getCommand();
        String[] args = request.getArgs();
        if (args != null && args.length > 0) {
            fullCommand += " " + args[0];
        }
        return fullCommand;
    }
    
    /**
     * Processes tutorial step validation result and enriches response with feedback
     */
    private CommandResponse processTutorialStepResult(CommandResponse originalResponse, 
                                                       TutorialStepResult stepResult, 
                                                       FormState sessionState) {
        if (!stepResult.isSuccess()) {
            // Step validation failed - command executed but didn't fulfill tutorial objective
            String failureMessage = "\n\n❌ " + stepResult.getErrorMessage();
            
            // Preserve original success response but add tutorial feedback
            originalResponse.setMessage(originalResponse.getMessage() + failureMessage);
            return originalResponse;
        }
        
        // Step succeeded
        if (stepResult.isTutorialCompleted()) {
            // Tutorial completely finished
            String completionMessage = originalResponse.getMessage() + 
                "\n\n🎉 **¡Felicitaciones! Tutorial Completado** 🎉\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Has completado todos los niveles del tutorial.\n" +
                "Pasos completados: " + stepResult.getTotalSteps() + "\n" +
                "Hints usadas: " + stepResult.getHintsUsed() + "\n\n" +
                "Has dominado los fundamentos de APX CLI. " +
                "Ahora puedes explorar libremente el sistema.";
            originalResponse.setMessage(completionMessage);
            
        } else if (stepResult.isLevelCompleted()) {
            // Level finished, advancing to next level
            String levelMessage = originalResponse.getMessage() + 
                "\n\n⭐ **¡Nivel " + stepResult.getCompletedLevelNumber() + " Completado!** ⭐\n" +
                "Excelente trabajo. Avanzando al siguiente nivel...\n\n" +
                "Continúa siguiendo las instrucciones en el panel educativo.";
            originalResponse.setMessage(levelMessage);
            
        } else {
            // Normal step completion
            String successMessage = originalResponse.getMessage() + 
                "\n\n✅ " + stepResult.getSuccessMessage();
            originalResponse.setMessage(successMessage);
        }
        
        return originalResponse;
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
