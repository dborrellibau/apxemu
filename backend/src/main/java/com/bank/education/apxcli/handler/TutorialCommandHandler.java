package com.bank.education.apxcli.handler;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.model.tutorial.TutorialProgress;
import com.bank.education.apxcli.service.tutorial.TutorialService;
import com.bank.education.apxcli.service.tutorial.TutorialStepResult;
import org.springframework.stereotype.Component;

/**
 * Handler for tutorial commands and step validation.
 * 
 * Priority 20 - High priority to intercept tutorial commands but allow
 * reset (priority 1) to work during tutorial mode.
 * 
 * Supports subcommands:
 * - tutorial start: Begins interactive tutorial
 * - tutorial hint: Shows additional help for current step
 * - tutorial status: Displays progress statistics
 * - tutorial exit: Exits tutorial mode (preserves progress)
 * 
 * Also provides post-execution validation via validateStepAfterExecution()
 * which is called by CommandParserService after any command execution
 * when tutorial mode is active.
 */
@Component
public class TutorialCommandHandler extends CommandHandler {
    
    private final TutorialService tutorialService;
    
    public static final int PRIORITY = 20;
    
    public TutorialCommandHandler(TutorialService tutorialService) {
        this.tutorialService = tutorialService;
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean canHandle(CommandRequest request, FormState sessionState) {
        String cmd = request.getCommand();
        return cmd != null && cmd.trim().toLowerCase().startsWith("tutorial");
    }
    
    @Override
    public CommandResponse handle(CommandRequest request, FormState sessionState) {
        String command = request.getCommand().trim().toLowerCase();
        
        // Parse subcommand
        String[] parts = command.split("\\s+");
        if (parts.length < 2) {
            return showHelpMessage();
        }
        
        String subCommand = parts[1];
        
        switch (subCommand) {
            case "start":
                return handleStart(sessionState);
            
            case "hint":
                return handleHint(sessionState);
            
            case "status":
                return handleStatus(sessionState);
            
            case "exit":
                return handleExit(sessionState);
            
            default:
                return CommandResponse.error(
                    "Subcomando desconocido: '" + subCommand + "'\n" +
                    "Usa 'tutorial start|hint|status|exit'"
                );
        }
    }
    
    @Override
    public String getName() {
        return "TutorialCommandHandler";
    }
    
    /**
     * Validates if the executed command fulfills the current tutorial step.
     * Called by CommandParserService after any command execution when tutorial is active.
     * 
     * @param executedCommand The command that was just executed
     * @param sessionState Current session state with tutorial progress
     * @return TutorialStepResult indicating success/failure and next action
     */
    public TutorialStepResult validateStepAfterExecution(String executedCommand, FormState sessionState) {
        return tutorialService.validateAndAdvance(executedCommand, sessionState);
    }
    
    /**
     * Generates tutorial hint for display in educational panel.
     * Called by CommandParserService or EducationalHintService to show current step.
     * 
     * @param sessionState Current session state
     * @return Formatted tutorial hint or null if not in tutorial mode
     */
    public String generateTutorialHint(FormState sessionState) {
        return tutorialService.generateTutorialHint(sessionState);
    }
    
    /**
     * Checks if user is currently in tutorial mode
     */
    public boolean isInTutorialMode(FormState sessionState) {
        TutorialProgress progress = sessionState.getTutorialProgress();
        return progress != null && progress.isTutorialMode();
    }
    
    // ============ Private Handlers ============
    
    private CommandResponse handleStart(FormState sessionState) {
        return tutorialService.startTutorial(sessionState);
    }
    
    private CommandResponse handleHint(FormState sessionState) {
        return tutorialService.showHint(sessionState);
    }
    
    private CommandResponse handleStatus(FormState sessionState) {
        return tutorialService.showStatus(sessionState);
    }
    
    private CommandResponse handleExit(FormState sessionState) {
        return tutorialService.exitTutorial(sessionState);
    }
    
    private CommandResponse showHelpMessage() {
        return CommandResponse.info(
            "🎓 **Tutorial de APX CLI**\n\n" +
            "Comandos disponibles:\n" +
            "  tutorial start  - Comenzar el tutorial interactivo\n" +
            "  tutorial hint   - Mostrar ayuda adicional para el paso actual\n" +
            "  tutorial status - Ver tu progreso y estadísticas\n" +
            "  tutorial exit   - Salir del modo tutorial (se guarda tu progreso)\n\n" +
            "El tutorial te guiará paso a paso para aprender los comandos de APX CLI."
        );
    }
}
