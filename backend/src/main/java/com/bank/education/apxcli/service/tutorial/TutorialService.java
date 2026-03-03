package com.bank.education.apxcli.service.tutorial;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.model.tutorial.TutorialLevel;
import com.bank.education.apxcli.model.tutorial.TutorialProgress;
import com.bank.education.apxcli.model.tutorial.TutorialStep;
import com.bank.education.apxcli.repository.tutorial.TutorialLevelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Main service for tutorial functionality.
 * Manages tutorial flow, step validation, and progress tracking.
 */
@Service
public class TutorialService {
    
    private final TutorialLevelRepository levelRepository;
    private final TutorialValidatorService validatorService;
    
    public TutorialService(TutorialLevelRepository levelRepository,
                          TutorialValidatorService validatorService) {
        this.levelRepository = levelRepository;
        this.validatorService = validatorService;
    }
    
    /**
     * Starts tutorial mode for the user.
     * Initializes progress and sets up the first level.
     */
    @Transactional(readOnly = true)
    public CommandResponse startTutorial(FormState sessionState) {
        // Check if already in tutorial
        TutorialProgress existingProgress = sessionState.getTutorialProgress();
        if (existingProgress != null && existingProgress.isTutorialMode()) {
            return CommandResponse.info(
                "Ya estás en modo tutorial.\n" +
                "Nivel actual: " + existingProgress.getCurrentLevelId() + 
                ", Paso: " + existingProgress.getCurrentStepNumber() + "\n" +
                "Usa 'tutorial exit' para salir o continúa con el siguiente paso."
            );
        }
        
        // Load first level
        TutorialLevel firstLevel = levelRepository.findByOrderNumber(1)
            .orElseThrow(() -> new IllegalStateException("No tutorial levels configured"));
        
        // Initialize progress
        TutorialProgress progress = new TutorialProgress();
        progress.setTutorialMode(true);
        progress.setCurrentLevelId(firstLevel.getId());
        progress.setCurrentStepNumber(1);
        progress.setTutorialStartedAt(LocalDateTime.now());
        
        sessionState.setTutorialProgress(progress);
        
        // Reset to root for clean tutorial start
        sessionState.setCurrentDirectory("root");
        
        CommandResponse response = CommandResponse.success(
            "🎓 ¡Bienvenido al Tutorial de APX CLI!\n\n" +
            "El tutorial comenzará con el siguiente comando.\n" +
            "Las instrucciones aparecerán en el panel de hints educativas. 💡"
        );
        
        // Set educational hint with first step
        response.setEducationalHint(generateTutorialHint(sessionState));
        
        return response;
    }
    
    /**
     * Generates educational hint with tutorial information.
     * Shows current level, step, instruction, and progress bar.
     */
    @Transactional(readOnly = true)
    public String generateTutorialHint(FormState sessionState) {
        TutorialProgress progress = sessionState.getTutorialProgress();
        if (progress == null || !progress.isTutorialMode()) {
            return null;
        }
        
        TutorialLevel level = levelRepository.findById(progress.getCurrentLevelId())
            .orElse(null);
        if (level == null) {
            return null;
        }
        
        TutorialStep step = level.getSteps().stream()
            .filter(s -> s.getStepNumber().equals(progress.getCurrentStepNumber()))
            .findFirst()
            .orElse(null);
        if (step == null) {
            return null;
        }
        
        // Build formatted hint
        StringBuilder hint = new StringBuilder();
        hint.append("🎓 **").append(level.getName()).append("**\n");
        hint.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        hint.append("📝 **Paso ").append(progress.getCurrentStepNumber())
            .append("/").append(level.getSteps().size()).append(":**\n");
        hint.append(step.getInstruction()).append("\n\n");
        hint.append("💡 **Tip:** ").append(step.getHint()).append("\n\n");
        
        // Progress bar
        int progressPercent = (progress.getCurrentStepNumber() * 100) / level.getSteps().size();
        int filledBars = progressPercent / 10;
        hint.append("Progreso: [");
        for (int i = 0; i < 10; i++) {
            hint.append(i < filledBars ? "█" : "░");
        }
        hint.append("] ").append(progressPercent).append("%\n\n");
        
        hint.append("_Escribe 'tutorial hint' para ayuda adicional_");
        
        return hint.toString();
    }
    
    /**
     * Validates command execution and advances to next step if correct.
     */
    @Transactional(readOnly = true)
    public TutorialStepResult validateAndAdvance(String command, FormState sessionState) {
        TutorialProgress progress = sessionState.getTutorialProgress();
        if (progress == null || !progress.isTutorialMode()) {
            return TutorialStepResult.notInTutorialMode();
        }
        
        TutorialLevel currentLevel = levelRepository.findById(progress.getCurrentLevelId())
            .orElseThrow(() -> new IllegalStateException("Current level not found"));
        
        TutorialStep currentStep = currentLevel.getSteps().stream()
            .filter(s -> s.getStepNumber().equals(progress.getCurrentStepNumber()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Current step not found"));
        
        // Validate command
        boolean isValid = validatorService.validate(command, currentStep, sessionState);
        
        if (!isValid) {
            return TutorialStepResult.failure(currentStep);
        }
        
        // Check if this is the last step of the level
        if (progress.getCurrentStepNumber() >= currentLevel.getSteps().size()) {
            return handleLevelCompletion(sessionState, currentLevel);
        } else {
            // Advance to next step
            progress.advanceStep();
            return TutorialStepResult.success(currentStep);
        }
    }
    
    /**
     * Handles level completion and transition to next level or tutorial completion.
     */
    @Transactional(readOnly = true)
    private TutorialStepResult handleLevelCompletion(FormState sessionState, TutorialLevel completedLevel) {
        TutorialProgress progress = sessionState.getTutorialProgress();
        progress.completeLevel(completedLevel.getId());
        progress.advanceStep(); // Count the last step
        
        // Look for next level
        TutorialLevel nextLevel = levelRepository.findByOrderNumber(completedLevel.getOrderNumber() + 1)
            .orElse(null);
        
        if (nextLevel == null) {
            // Tutorial completed
            progress.setTutorialMode(false);
            return TutorialStepResult.tutorialCompleted(
                progress.getTotalStepsCompleted(),
                progress.getTotalHintsUsed()
            );
        }
        
        // Advance to next level
        progress.setCurrentLevelId(nextLevel.getId());
        progress.setCurrentStepNumber(1);
        
        return TutorialStepResult.levelCompleted(completedLevel.getOrderNumber());
    }
    
    /**
     * Shows additional hint for current step.
     */
    @Transactional(readOnly = true)
    public CommandResponse showHint(FormState sessionState) {
        TutorialProgress progress = sessionState.getTutorialProgress();
        if (progress == null || !progress.isTutorialMode()) {
            return CommandResponse.error("No estás en modo tutorial. Usa 'tutorial start'");
        }
        
        TutorialLevel level = levelRepository.findById(progress.getCurrentLevelId())
            .orElseThrow(() -> new IllegalStateException("Tutorial level not found"));
        TutorialStep step = level.getSteps().stream()
            .filter(s -> s.getStepNumber().equals(progress.getCurrentStepNumber()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Tutorial step not found"));
        
        progress.useHint();
        
        String hintText = "💡 **Hint Adicional:**\n\n" + step.getHint();
        
        // Add expected command if it's exact match
        if (step.getExpectedCommand() != null && 
            step.getValidationType().name().equals("EXACT_MATCH")) {
            hintText += "\n\n_Comando esperado:_ `" + step.getExpectedCommand() + "`";
        }
        
        return CommandResponse.info(hintText);
    }
    
    /**
     * Shows current tutorial progress status.
     */
    @Transactional(readOnly = true)
    public CommandResponse showStatus(FormState sessionState) {
        TutorialProgress progress = sessionState.getTutorialProgress();
        
        if (progress == null || !progress.isTutorialMode()) {
            return CommandResponse.info(
                "📚 No estás en modo tutorial.\n" +
                "Usa 'tutorial start' para comenzar."
            );
        }
        
        TutorialLevel currentLevel = levelRepository.findById(progress.getCurrentLevelId())
            .orElseThrow(() -> new IllegalStateException("Tutorial level not found"));
        
        StringBuilder status = new StringBuilder();
        status.append("📊 **Estado del Tutorial**\n");
        status.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        status.append("🎓 Nivel actual: ").append(currentLevel.getName()).append("\n");
        status.append("📝 Paso: ").append(progress.getCurrentStepNumber())
              .append("/").append(currentLevel.getSteps().size()).append("\n\n");
        
        int progressPercent = (progress.getCurrentStepNumber() * 100) / currentLevel.getSteps().size();
        status.append("Progreso del nivel: ").append(progressPercent).append("%\n");
        
        status.append("\n📈 Estadísticas:\n");
        status.append("- Pasos completados: ").append(progress.getTotalStepsCompleted()).append("\n");
        status.append("- Hints usadas: ").append(progress.getTotalHintsUsed()).append("\n");
        status.append("- Niveles completados: ").append(progress.getCompletedLevelIds().size()).append("\n");
        
        return CommandResponse.success(status.toString());
    }
    
    /**
     * Exits tutorial mode (progress is saved in session).
     */
    public CommandResponse exitTutorial(FormState sessionState) {
        TutorialProgress progress = sessionState.getTutorialProgress();
        if (progress == null || !progress.isTutorialMode()) {
            return CommandResponse.error("No estás en modo tutorial");
        }
        
        progress.setTutorialMode(false);
        
        return CommandResponse.success(
            "📚 Saliste del modo tutorial.\n" +
            "Progreso guardado: " + progress.getTotalStepsCompleted() + " pasos completados.\n" +
            "Usa 'tutorial start' para continuar más tarde."
        );
    }
}
