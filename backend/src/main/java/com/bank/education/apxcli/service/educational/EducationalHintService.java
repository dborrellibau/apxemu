package com.bank.education.apxcli.service.educational;

import com.bank.education.apxcli.navigation.model.PathType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para gestionar hints educativos contextuales
 * Los hints se muestran automáticamente según el comando ejecutado y el PathType actual
 */
@Service
public class EducationalHintService {
    
    private final Map<String, String> hints = new HashMap<>();
    
    /**
     * Carga todos los hints educativos al iniciar el servicio
     * Formato de key: "comando:PathType" o "comando:ANY" para hints genéricos
     */
    @PostConstruct
    public void loadHints() {
        // ========== HINTS EN ROOT ==========
        
        hints.put("ls:ROOT", 
            "Deployment Units Overview\n\n" +
            "You're viewing all deployment units in the system:\n" +
            "• DU_ONLINE: Online banking services (transactions, DTOs)\n" +
            "• DU_LIB: Shared libraries used across multiple DUs\n\n" +
            "Use 'cd <du-name>' to navigate into a deployment unit."
        );
        
        hints.put("pwd:ROOT",
            "Navigation Context\n\n" +
            "You're at ROOT level - the top of the hierarchy.\n" +
            "From here you can:\n" +
            "• Use 'apx init' to create new deployment units\n" +
            "• Use 'apx list' to see all components globally\n" +
            "• Use 'cd' to navigate into deployment units"
        );
        
        hints.put("apx:ROOT",
            "APX Commands at ROOT\n\n" +
            "Available commands from ROOT:\n" +
            "• apx init - Interactive wizard to create DU_ONLINE or DU_LIB\n" +
            "• apx list - List all deployment units or filter by type\n" +
            "• apx help - Show all available commands\n\n" +
            "Commands like 'apx add' and 'apx del' require navigating into a DU first."
        );
        
        // ========== HINTS EN DU_ONLINE ==========
        
        hints.put("ls:DU_ONLINE",
            "DU_ONLINE Structure\n\n" +
            "This deployment unit contains three folders:\n" +
            "• dto/ - Data Transfer Objects\n" +
            "• library/ - Reusable library components\n" +
            "• transactions/ - Business transaction handlers\n\n" +
            "Use 'cd <folder>' to navigate into any folder."
        );
        
        hints.put("apx:DU_ONLINE",
            "APX Commands in DU_ONLINE\n\n" +
            "From a DU_ONLINE you can:\n" +
            "• apx add - Create components (DTO, Library, Transaction)\n" +
            "• apx del - Delete components with interactive menu\n" +
            "• apx show - See detailed structure and component count\n\n" +
            "The system will guide you with interactive menus for each operation."
        );
        
        hints.put("pwd:DU_ONLINE",
            "DU_ONLINE Context\n\n" +
            "You're inside a deployment unit container.\n" +
            "This PathType enables component creation commands.\n\n" +
            "CommandPermissionService validates which commands are available\n" +
            "based on your current location in the hierarchy."
        );
        
        // ========== HINTS EN FOLDER ==========
        
        hints.put("ls:FOLDER",
            "Folder Contents\n\n" +
            "You're viewing components inside a functional folder.\n" +
            "Each component has:\n" +
            "• UUAA code (4 characters) - inherited from parent DU\n" +
            "• Component code (3 digits for DTOs, name for others)\n" +
            "• Type indicator (dto, lib, trx)\n\n" +
            "Use 'cd <component>' to navigate into a component."
        );
        
        hints.put("pwd:FOLDER",
            "Folder Context\n\n" +
            "You're at PathType.FOLDER level.\n" +
            "This is level 2 in the 4-level hierarchy:\n" +
            "ROOT → DU → FOLDER → COMPONENT\n\n" +
            "From here you can navigate into components with 'cd'."
        );
        
        hints.put("cd:FOLDER",
            "Navigating to Components\n\n" +
            "Use 'cd <component-name>' to enter a component.\n" +
            "Once inside a component, you can:\n" +
            "• Use 'apx show' to see details and dependencies\n" +
            "• Use 'apx add dep' to create dependency relationships\n\n" +
            "Components are where the dependency graph begins."
        );
        
        // ========== HINTS EN COMPONENT (cualquier nivel de componente) ==========
        
        hints.put("apx:COMPONENT_IN_FOLDER",
            "Component Commands\n\n" +
            "From inside a component you can:\n" +
            "• apx show - See component details and all dependencies\n" +
            "• apx add dep - Create dependency to another component\n" +
            "• apx add in - Add input DTO to transaction (TRX only)\n" +
            "• apx add out - Add output DTO to transaction (TRX only)\n\n" +
            "Dependencies form the architecture's relationship graph."
        );
        
        hints.put("pwd:COMPONENT_IN_FOLDER",
            "Component Context\n\n" +
            "You're at PathType.COMPONENT_IN_FOLDER (level 3).\n" +
            "This is the deepest navigation level in the hierarchy.\n\n" +
            "Components are atomic units that can have dependencies\n" +
            "on other components, forming the architecture graph."
        );
        
        // ========== HINTS GENÉRICOS (ANY) ==========
        
        hints.put("apx:ANY",
            "APX Command System\n\n" +
            "APX is the main command namespace for architecture operations.\n" +
            "All creation, deletion, and management commands require 'apx' prefix.\n\n" +
            "Use 'apx help' to see all available commands with examples."
        );
        
        hints.put("cd:ANY",
            "Hierarchical Navigation\n\n" +
            "The system uses a 4-level PathType hierarchy:\n" +
            "• Level 0: ROOT\n" +
            "• Level 1: DU_ONLINE, DU_LIB, COMPONENT_STANDALONE\n" +
            "• Level 2: FOLDER (dto, library, transactions)\n" +
            "• Level 3: COMPONENT_IN_FOLDER\n\n" +
            "PathNavigationService enforces valid transitions between levels."
        );
        
        hints.put("help:ANY",
            "Context-Aware Help\n\n" +
            "The help command shows available commands based on:\n" +
            "• Your current PathType (where you're navigating)\n" +
            "• Active FormState (if a wizard is running)\n\n" +
            "CommandPermissionService validates permissions per context."
        );
        
        hints.put("clear:ANY",
            "Terminal Management\n\n" +
            "Use 'clear' to clean the terminal screen.\n" +
            "Command history (arrow keys ↑↓) is preserved after clearing.\n\n" +
            "The terminal maintains session state through WebSocket connection."
        );
        
        hints.put("ls:ANY",
            "Context-Aware Listing\n\n" +
            "'ls' shows different content based on your location:\n" +
            "• ROOT: All deployment units\n" +
            "• DU_ONLINE: Folders (dto, library, transactions)\n" +
            "• FOLDER: Components inside that folder\n\n" +
            "Deleted items are shown in red with [DELETED] marker."
        );
    }
    
    /**
     * Obtiene hint contextual para un comando y PathType dados
     * 
     * @param command Comando completo ingresado por el usuario (ej: "ls", "cd DU_ONLINE_CUST")
     * @param currentPath PathType actual del usuario
     * @return Hint string o null si no hay hint disponible
     */
    public String getHintFor(String command, PathType currentPath) {
        if (command == null || command.trim().isEmpty()) {
            return null;
        }
        
        // Extraer prefijo del comando (primera palabra)
        String prefix = command.trim().split("\\s+")[0].toLowerCase();
        
        // Buscar hint específico para este PathType
        String key = prefix + ":" + currentPath.name();
        String hint = hints.get(key);
        
        // Fallback: buscar hint genérico (ANY)
        if (hint == null) {
            hint = hints.get(prefix + ":ANY");
        }
        
        return hint;
    }
    
    /**
     * Verifica si existe un hint para el comando dado
     */
    public boolean hasHintFor(String command, PathType currentPath) {
        return getHintFor(command, currentPath) != null;
    }
    
    /**
     * Obtiene el número total de hints cargados
     */
    public int getTotalHints() {
        return hints.size();
    }
}
