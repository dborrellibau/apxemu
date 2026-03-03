package com.bank.education.apxcli.config;

import com.bank.education.apxcli.model.tutorial.StepValidationType;
import com.bank.education.apxcli.model.tutorial.TutorialLevel;
import com.bank.education.apxcli.model.tutorial.TutorialStep;
import com.bank.education.apxcli.repository.tutorial.TutorialLevelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that loads tutorial data at application startup.
 * Creates tutorial levels and steps in the database.
 */
@Configuration
public class TutorialDataLoader {
    
    @Bean
    public CommandLineRunner loadTutorialData(TutorialLevelRepository levelRepository) {
        return args -> {
            // Only load if no levels exist (avoid duplicates on restart)
            if (levelRepository.count() > 0) {
                return;
            }
            
            // Level 1: Primeros Pasos
            TutorialLevel level1 = createLevel1();
            levelRepository.save(level1);
        };
    }
    
    /**
     * Creates Level 1: First Steps - Basic Navigation
     */
    private TutorialLevel createLevel1() {
        TutorialLevel level = new TutorialLevel();
        level.setName("Nivel 1: Primeros Pasos");
        level.setDescription("Aprende a navegar por el sistema APX usando comandos básicos");
        level.setOrderNumber(1);
        level.setBadgeIcon("🎯");
        
        // Step 1: Execute ls
        TutorialStep step1 = new TutorialStep();
        step1.setStepNumber(1);
        step1.setInstruction("Usa el comando 'ls' para listar todos los deployment units disponibles");
        step1.setHint("Escribe simplemente 'ls' y presiona Enter");
        step1.setValidationType(StepValidationType.EXACT_MATCH);
        step1.setExpectedCommand("ls");
        step1.setSuccessMessage("¡Perfecto! Ahora puedes ver todos los DU del sistema");
        step1.setErrorMessage("Recuerda usar solo 'ls' sin parámetros adicionales");
        level.addStep(step1);
        
        // Step 2: Navigate to DU-ONLINE-CUST
        TutorialStep step2 = new TutorialStep();
        step2.setStepNumber(2);
        step2.setInstruction("Navega al deployment unit 'DU-ONLINE-CUST' usando el comando 'cd'");
        step2.setHint("Usa 'cd DU-ONLINE-CUST' para entrar al DU");
        step2.setValidationType(StepValidationType.PATH_VALIDATION);
        step2.setExpectedPathType("DU_ONLINE");
        step2.setExpectedDirectory("DU-ONLINE-CUST");
        step2.setSuccessMessage("¡Excelente! Ahora estás dentro del DU-ONLINE-CUST");
        step2.setErrorMessage("Usa el formato: cd DU-ONLINE-CUST");
        level.addStep(step2);
        
        // Step 3: Execute pwd
        TutorialStep step3 = new TutorialStep();
        step3.setStepNumber(3);
        step3.setInstruction("Usa 'pwd' para verificar tu ubicación actual");
        step3.setHint("'pwd' muestra el directorio actual. Es como el 'where am I?' del sistema");
        step3.setValidationType(StepValidationType.EXACT_MATCH);
        step3.setExpectedCommand("pwd");
        step3.setSuccessMessage("Correcto. Siempre puedes usar 'pwd' para saber dónde estás");
        step3.setErrorMessage("Escribe exactamente 'pwd'");
        level.addStep(step3);
        
        // Step 4: Execute ls again
        TutorialStep step4 = new TutorialStep();
        step4.setStepNumber(4);
        step4.setInstruction("Lista el contenido del DU usando 'ls'");
        step4.setHint("Dentro de un DU-ONLINE verás 3 carpetas: dto/, library/ y transactions/");
        step4.setValidationType(StepValidationType.EXACT_MATCH);
        step4.setExpectedCommand("ls");
        step4.setSuccessMessage("Bien hecho. Estas son las 3 carpetas estándar de un DU-ONLINE");
        step4.setErrorMessage("Usa 'ls' para ver las carpetas");
        level.addStep(step4);
        
        // Step 5: Navigate to dto folder
        TutorialStep step5 = new TutorialStep();
        step5.setStepNumber(5);
        step5.setInstruction("Navega a la carpeta 'dto' usando 'cd dto'");
        step5.setHint("Los DTOs (Data Transfer Objects) contienen las estructuras de datos");
        step5.setValidationType(StepValidationType.PATH_VALIDATION);
        step5.setExpectedPathType("FOLDER");
        step5.setExpectedDirectory("DU-ONLINE-CUST/dto");
        step5.setSuccessMessage("¡Perfecto! Completaste el Nivel 1. Ahora dominas la navegación básica");
        step5.setErrorMessage("Usa: cd dto");
        level.addStep(step5);
        
        return level;
    }
}
