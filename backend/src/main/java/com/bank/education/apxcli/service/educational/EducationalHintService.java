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
            "💡 **Deployment Units (DU)**\n\n" +
            "Las DU agrupan componentes relacionados por dominio:\n" +
            "• DU_ONLINE: Procesa transacciones en línea (TRX, DTOs)\n" +
            "• DU_LIB: Contiene librerías compartidas entre DUs\n\n" +
            "Cada DU puede contener carpetas (FO) para organizar componentes."
        );
        
        hints.put("cd:ROOT",
            "💡 **Navegación Jerárquica (PathType)**\n\n" +
            "El sistema usa 7 tipos de paths organizados en 4 niveles:\n" +
            "• Nivel 0: ROOT\n" +
            "• Nivel 1: DU_ONLINE, DU_LIB, COMPONENT_STANDALONE\n" +
            "• Nivel 2: FOLDER (carpetas dentro de DUs)\n" +
            "• Nivel 3: COMPONENT_IN_FOLDER\n\n" +
            "Cada PathType habilita diferentes comandos disponibles."
        );
        
        hints.put("create:ROOT",
            "💡 **Strategy Pattern**\n\n" +
            "Cada tipo de componente implementa DeploymentUnitStrategy:\n" +
            "• Define formularios interactivos específicos\n" +
            "• Valida reglas de negocio propias\n" +
            "• Determina estructura de carpetas permitida\n\n" +
            "Ejemplo: DuOnlineStrategy contiene FO, LibStrategy es standalone."
        );
        
        // ========== HINTS EN DU_ONLINE ==========
        
        hints.put("ls:DU_ONLINE",
            "💡 **Contexto: DU Online**\n\n" +
            "Dentro de una DU_ONLINE puedes crear carpetas (FO).\n" +
            "Las carpetas organizan componentes por dominio funcional:\n" +
            "• Transacciones de clientes\n" +
            "• Operaciones de cuentas\n" +
            "• DTOs de comunicación\n\n" +
            "Usa 'mkdir <nombre>' para crear carpetas."
        );
        
        hints.put("cd:DU_ONLINE",
            "💡 **Navegación a Carpetas**\n\n" +
            "Al entrar en carpetas (FO) cambias a PathType.FOLDER.\n" +
            "Allí puedes crear componentes específicos:\n" +
            "• create trx: Transacciones\n" +
            "• create dto: Objetos de transferencia\n\n" +
            "Los componentes heredan el contexto de la carpeta padre."
        );
        
        // ========== HINTS EN FOLDER ==========
        
        hints.put("create:FOLDER",
            "💡 **Componentes en Carpetas**\n\n" +
            "Dentro de carpetas (PathType.FOLDER) creas componentes.\n" +
            "El wizard FormProcessingService guía paso a paso:\n" +
            "1. Selección de tipo (TRX, DTO, etc)\n" +
            "2. Ingreso de códigos bancarios (UUAA de 4 chars)\n" +
            "3. Confirmación final\n\n" +
            "FormState mantiene el progreso entre pasos."
        );
        
        hints.put("ls:FOLDER",
            "💡 **Contenido de Carpetas**\n\n" +
            "Las carpetas (FO) organizan componentes por dominio.\n" +
            "Cada componente tiene:\n" +
            "• Código UUAA (4 caracteres)\n" +
            "• Tipo (TRX, DTO, LIB)\n" +
            "• Relaciones con otros componentes\n\n" +
            "Los componentes pueden tener dependencias entre sí."
        );
        
        // ========== HINTS GENÉRICOS (ANY) ==========
        
        hints.put("rm:ANY",
            "💡 **Soft Delete Pattern**\n\n" +
            "Los componentes NUNCA se borran físicamente de H2.\n" +
            "Se marca el flag deleted=true para:\n" +
            "• Mantener auditoría completa de cambios\n" +
            "• Permitir escenarios educativos de recuperación\n\n" +
            "DeletionCommandService navega automáticamente si estás 'dentro' del componente eliminado."
        );
        
        hints.put("deps:ANY",
            "💡 **Gestión de Dependencias Interactiva**\n\n" +
            "El wizard usa FormState para mantener contexto:\n" +
            "• awaitingComponentSelection: esperando selección\n" +
            "• temporaryDependencies: lista acumulativa\n" +
            "• Comandos 'add', 'remove', 'list', 'done', 'cancel'\n\n" +
            "Patrón reutilizado en otros wizards del sistema."
        );
        
        hints.put("help:ANY",
            "💡 **Sistema de Ayuda Contextual**\n\n" +
            "El comando 'help' muestra comandos disponibles según:\n" +
            "• PathType actual (dónde estás navegando)\n" +
            "• Estado de FormState (si hay wizard activo)\n\n" +
            "CommandPermissionService valida permisos por contexto."
        );
        
        hints.put("show:ANY",
            "💡 **Visualización de Componentes**\n\n" +
            "El comando 'show' muestra detalles del componente actual:\n" +
            "• Metadatos (código, descripción, tipo)\n" +
            "• Dependencias entrantes y salientes\n" +
            "• Ubicación en la jerarquía\n\n" +
            "Usa ContainableInfoService con @Transactional para evitar LazyInitializationException."
        );
        
        hints.put("pwd:ANY",
            "💡 **Contexto de Navegación**\n\n" +
            "El comando 'pwd' muestra tu ubicación actual.\n" +
            "El sistema mantiene el estado en FormState:\n" +
            "• currentDirectory: ruta actual\n" +
            "• PathType: tipo de ubicación\n\n" +
            "Esto determina qué comandos están disponibles."
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
