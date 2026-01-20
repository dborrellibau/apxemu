package com.bank.education.apxcli.service.educational;

import com.bank.education.apxcli.navigation.model.PathType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class EducationalHintService {

    private Map<String, String> hints;

    @PostConstruct
    public void initialize() {
        hints = new HashMap<>();
        
        hints.put("cd:ROOT", 
            "Desde ROOT puedes navegar a deployment units (DU-ONLINE/DU-LIB) o componentes standalone. " +
            "Usa 'ls' para ver las opciones disponibles.");
        
        hints.put("cd:DU_ONLINE", 
            "Dentro de un DU-ONLINE encontrarás 3 carpetas: dto/, library/ y transactions/. " +
            "Usa 'cd dto' para explorar los Data Transfer Objects.");
        
        hints.put("cd:DU_LIB", 
            "Los DU-LIB contienen componentes base e implementaciones reutilizables. " +
            "Navega con 'cd <component-name>' para explorar un componente específico.");
        
        hints.put("cd:FOLDER", 
            "Estás en una carpeta de componentes. Usa 'ls' para ver los componentes disponibles, " +
            "luego 'cd <component-id>' para navegar a uno específico.");
        
        hints.put("cd:COMPONENT_IN_FOLDER", 
            "Estás dentro de un componente. Desde aquí puedes usar 'apx show' para ver detalles " +
            "o 'apx add dep' para gestionar dependencias.");
        
        hints.put("cd:COMPONENT_IN_DULIB", 
            "Este componente de librería puede ser dependencia de otros componentes. " +
            "Usa 'apx show' para ver qué componentes lo referencian.");
        
        hints.put("cd:COMPONENT_STANDALONE", 
            "Este es un componente standalone fuera de la estructura de carpetas. " +
            "Usa 'apx show' para inspeccionar sus dependencias.");
        
        hints.put("pwd:ROOT", 
            "Estás en el nivel raíz del sistema. Desde aquí puedes usar 'apx init' " +
            "para crear nuevos deployment units.");
        
        hints.put("pwd:DU_ONLINE", 
            "Esta ruta muestra un DU-ONLINE. Estos deployment units contienen " +
            "componentes organizados en carpetas (dto/, library/, transactions/).");
        
        hints.put("pwd:DU_LIB", 
            "Esta ruta muestra un DU-LIB. Las librerías compartidas no usan carpetas, " +
            "sus componentes están directamente bajo el DU.");
        
        hints.put("pwd:FOLDER", 
            "Estás en una carpeta de componentes dentro de un DU-ONLINE. " +
            "El nombre de la carpeta indica el tipo de componentes que contiene.");
        
        hints.put("pwd:COMPONENT_IN_FOLDER", 
            "Ruta completa hasta un componente específico. Útil para documentar " +
            "ubicaciones exactas en arquitecturas complejas.");
        
        hints.put("pwd:COMPONENT_IN_DULIB", 
            "Ruta de un componente de librería. Observa que no incluye carpeta intermedia " +
            "ya que DU-LIB tiene estructura plana.");
        
        hints.put("pwd:COMPONENT_STANDALONE", 
            "Ruta de un componente standalone. Estos componentes no pertenecen " +
            "a la estructura jerárquica de DU-ONLINE/DU-LIB.");
        
        hints.put("ls:ROOT", 
            "'ls' en ROOT muestra todos los deployment units del sistema. " +
            "Usa 'apx list <tipo>' para filtrar por tipo específico (du-online, du-lib).");
        
        hints.put("ls:DU_ONLINE", 
            "Los DU-ONLINE siempre tienen 3 carpetas estándar. El contador indica " +
            "cuántos componentes hay en cada carpeta. Usa 'cd <carpeta>' para explorar.");
        
        hints.put("ls:DU_LIB", 
            "Los DU-LIB muestran componentes directamente (sin carpetas). " +
            "Busca componentes LIB (base) y LIB-IMPL (implementación).");
        
        hints.put("ls:FOLDER", 
            "Listado de componentes en carpeta. El tipo entre paréntesis indica " +
            "si es dto, lib o trx. Los items con [DELETED] están marcados como eliminados.");
        
        hints.put("ls:COMPONENT_IN_FOLDER", 
            "No hay subcarpetas dentro de componentes. Usa 'apx show' para ver " +
            "detalles del componente actual o 'cd ..' para volver a la carpeta.");
        
        hints.put("ls:COMPONENT_IN_DULIB", 
            "Los componentes no tienen subelementos navegables. Usa 'apx show' " +
            "para inspeccionar dependencias y metadata.");
        
        hints.put("ls:COMPONENT_STANDALONE", 
            "Componente sin estructura interna. Para ver información detallada " +
            "usa 'apx show' en lugar de 'ls'.");
        
        hints.put("apx init:ROOT", 
            "'apx init' lanza un wizard interactivo para crear deployment units. " +
            "Primero elige el tipo (DU-ONLINE o DU-LIB), luego completa nombre, descripción y UUAA (4 letras mayúsculas). " +
            "Flujo típico: 1) Crear DU-ONLINE para aplicaciones → 2) Navegar con 'cd <du-name>' → " +
            "3) Agregar componentes con 'apx add' → 4) Gestionar dependencias con 'apx add dep'. " +
            "Alternativamente, crea DU-LIB para librerías compartidas reutilizables entre múltiples DU-ONLINE.");
        
        hints.put("apx add:DU_ONLINE", 
            "'apx add' crea componentes dentro del DU-ONLINE actual. Elige entre DTO (datos), " +
            "Library (lógica compartida) o Transaction (lógica de negocio). El UUAA se hereda automáticamente del DU. " +
            "Flujo recomendado: 1) Crear DTOs primero (estructuras de datos) → 2) Crear Libraries (lógica reutilizable) → " +
            "3) Crear Transactions (procesos de negocio) → 4) Navegar a componentes con 'cd <carpeta>/<component-id>' → " +
            "5) Agregar dependencias con 'apx add dep'. Usa 'apx list' para ver componentes disponibles como dependencias.");
        
        hints.put("apx add dep:COMPONENT_IN_FOLDER", 
            "Estás creando una dependencia desde un componente en carpeta. El wizard te guiará " +
            "para seleccionar el tipo de dependencia permitido y el artifact ID destino. Solo puedes referenciar LIB base (no LIB-IMPL). " +
            "Flujo de dependencias: 1) Ejecuta 'apx add dep' → 2) Selecciona tipo de componente destino (dto/lib/trx) → " +
            "3) Ingresa artifact ID del componente → 4) Confirma la creación. " +
            "Próximos pasos: Usa 'apx show' para verificar que la dependencia se agregó, revisa el diagrama visual " +
            "para ver las conexiones, o agrega más dependencias si es necesario. Usa 'apx list <tipo>' para buscar componentes disponibles.");
        
        hints.put("apx add dep:COMPONENT_IN_DULIB", 
            "Componentes de librería pueden depender de otros componentes del sistema. " +
            "Las dependencias aparecerán como flechas en el diagrama visual. " +
            "Flujo típico: 1) Agregar dependencias base primero (DTOs, otras librerías) → " +
            "2) Verificar con 'apx show' → 3) Probar navegación con 'cd ..' y 'apx list' para explorar arquitectura. " +
            "Las librerías en DU-LIB son reutilizables por múltiples DU-ONLINE, así que sus dependencias deben ser estables.");
        
        hints.put("apx add dep:COMPONENT_STANDALONE", 
            "Los componentes standalone también pueden tener dependencias. Usa el wizard " +
            "para seleccionar el tipo y artifact ID de la dependencia destino. " +
            "Flujo de trabajo: 1) Ejecuta 'apx add dep' → 2) Selecciona tipo de dependencia → 3) Ingresa artifact ID → " +
            "4) Verifica con 'apx show' que la dependencia se agregó correctamente. " +
            "Próximos pasos: Puedes agregar más dependencias, volver a ROOT con 'cd', o explorar otros componentes con 'apx list'.");
        
        hints.put("apx del:DU_ONLINE", 
            "'apx del' en DU-ONLINE muestra un menú interactivo con tipos de componentes. " +
            "Elige el tipo, luego selecciona el componente específico. El sistema usa soft delete (marcado, no borrado físico). " +
            "Flujo de eliminación: 1) Ejecuta 'apx del' → 2) Selecciona tipo de componente (dto/lib/trx) → " +
            "3) Elige el componente específico del menú → 4) Confirma con 'Y' o cancela con 'n'. " +
            "Importante: Los componentes eliminados permanecen en BD pero se ocultan. Si tenían dependencias, " +
            "debes eliminarlas manualmente. Usa 'apx show' en otros componentes para verificar referencias rotas.");
        
        hints.put("apx list:ROOT", 
            "'apx list' busca globalmente en todo el sistema. Úsalo sin parámetros para ver todos los DU, " +
            "o con tipo específico: 'apx list du-online', 'apx list dto', 'apx list lib', etc. " +
            "Flujo de exploración: 1) Ejecuta 'apx list' para ver todos los DU → 2) Filtra con 'apx list <tipo>' → " +
            "3) Navega a componentes interesantes con 'cd <du-name>' → 4) Inspecciona con 'apx show'. " +
            "Casos de uso: Buscar componentes candidatos para dependencias, auditar arquitectura completa, " +
            "o explorar antes de crear nuevos componentes para evitar duplicados.");
        
        hints.put("apx list:DU_ONLINE", 
            "'apx list' funciona desde cualquier ubicación. A diferencia de 'ls' (local), " +
            "'apx list' busca en todo el sistema. Filtra por tipo con: apx list <dto|lib|trx>. " +
            "Flujo de trabajo: Usa 'apx list lib' para ver todas las librerías disponibles antes de agregar dependencias, " +
            "o 'apx list dto' para encontrar estructuras de datos existentes. " +
            "Próximos pasos: Después de listar, navega a componentes con 'cd <path>', crea nuevos con 'apx add', " +
            "o inspecciona con 'apx show' desde sus ubicaciones.");
        
        hints.put("apx list:DU_LIB", 
            "Comando global independiente de tu ubicación. Útil para encontrar componentes " +
            "específicos sin navegar manualmente. Prueba: apx list lib");
        
        hints.put("apx list:FOLDER", 
            "'apx list' busca globalmente, 'ls' busca localmente. Desde carpetas, " +
            "'apx list dto' mostrará TODOS los DTOs del sistema, no solo los de esta carpeta.");
        
        hints.put("apx list:COMPONENT_IN_FOLDER", 
            "Búsqueda global desde un componente. Útil para encontrar componentes " +
            "candidatos para dependencias sin salir de tu ubicación actual. " +
            "Flujo típico: 1) Ejecuta 'apx list lib' para ver librerías disponibles → " +
            "2) Identifica artifact IDs útiles → 3) Ejecuta 'apx add dep' → 4) Ingresa el artifact ID encontrado. " +
            "Este flujo evita navegar manualmente por todo el sistema, manteniendo tu contexto actual.");
        
        hints.put("apx list:COMPONENT_IN_DULIB", 
            "Desde componentes de librería, 'apx list lib' te muestra todas las librerías " +
            "disponibles en el sistema para referencias o comparación.");
        
        hints.put("apx list:COMPONENT_STANDALONE", 
            "Comando universal de búsqueda. Combina con tipos: 'apx list du-online', " +
            "'apx list du-lib', 'apx list dto', 'apx list lib', 'apx list trx'.");

        hints.put("apx show:DU_ONLINE", 
            "'apx show' en DU-ONLINE muestra estructura de árbol con todas las carpetas " +
            "y componentes. Los números indican cantidad de componentes por carpeta. " +
            "Flujo de inspección: 1) Revisa la estructura completa del DU → 2) Identifica carpetas con componentes → " +
            "3) Navega a carpetas específicas con 'cd <carpeta>' → 4) Usa 'apx show' en componentes individuales para detalles. " +
            "Próximos pasos: Agrega componentes faltantes con 'apx add', elimina obsoletos con 'apx del', " +
            "o navega para gestionar dependencias con 'apx add dep'.");
        
        hints.put("apx show:DU_LIB", 
            "'apx show' en DU-LIB lista componentes base (LIB) e implementaciones (LIB-IMPL). " +
            "Observa la relación entre componentes base y sus implementaciones. " +
            "Flujo de trabajo: 1) Identifica componentes LIB disponibles → 2) Navega con 'cd <component-name>' → " +
            "3) Verifica dependencias con 'apx show' → 4) Usa estos artifact IDs en otros DU con 'apx add dep'. " +
            "Las librerías en DU-LIB son compartidas y reutilizables por múltiples DU-ONLINE del sistema.");
        
        hints.put("apx show:COMPONENT_IN_FOLDER", 
            "'apx show' en componente muestra metadata completa: artifact ID, descripción, UUAA " +
            "y todas las dependencias. Útil para documentar o auditar arquitecturas. " +
            "Flujo de inspección: 1) Revisa artifact ID y UUAA → 2) Analiza lista de dependencias → " +
            "3) Verifica coherencia arquitectónica → 4) Agrega dependencias faltantes con 'apx add dep'. " +
            "Próximos pasos: Navega a componentes dependientes para verificar referencias cruzadas, " +
            "usa 'apx list' para buscar alternativas, o actualiza dependencias según necesites.");
        
        hints.put("apx show:COMPONENT_IN_DULIB", 
            "Componentes de librería muestran dependencias y pueden indicar qué otros " +
            "componentes los referencian (dependencias inversas). " +
            "Flujo de análisis: 1) Revisa qué otros componentes dependen de esta librería → " +
            "2) Analiza las dependencias de la librería misma → 3) Evalúa impacto de cambios potenciales. " +
            "Importante: Modificar librerías en DU-LIB puede afectar múltiples DU-ONLINE. " +
            "Usa esta información para decisiones de arquitectura y refactoring seguro.");
        
        hints.put("apx show:COMPONENT_STANDALONE", 
            "Vista detallada del componente standalone con todas sus dependencias. " +
            "Los componentes standalone tienen metadata similar a componentes en carpetas. " +
            "Flujo de trabajo: 1) Inspecciona metadata y dependencias → 2) Agrega dependencias con 'apx add dep' si es necesario → " +
            "3) Verifica en el diagrama visual las conexiones → 4) Vuelve a ROOT con 'cd' para explorar arquitectura general. " +
            "Los componentes standalone están fuera de la jerarquía DU, útiles para casos especiales o prototipos.");
        
        hints.put("clear:ROOT", 
            "'clear' limpia el historial visual del terminal pero mantiene el estado " +
            "de tu sesión. Tu ubicación actual y componentes creados se preservan.");
        
        hints.put("clear:DU_ONLINE", 
            "Comando útil cuando el terminal tiene demasiado texto. No afecta " +
            "el estado de navegación ni la arquitectura creada.");
        
        hints.put("clear:DU_LIB", 
            "Limpia la pantalla sin afectar tu trabajo. Equivalente a Ctrl+L " +
            "en terminales Unix. El diagrama visual se mantiene intacto.");
        
        hints.put("clear:FOLDER", 
            "Resetea la vista del terminal manteniendo tu ubicación actual. " +
            "Útil para empezar con pantalla limpia antes de comandos complejos.");
        
        hints.put("clear:COMPONENT_IN_FOLDER", 
            "Limpieza visual del terminal. Tu contexto (ubicación, sesión, componentes) " +
            "permanece sin cambios. Solo remueve el historial de salida.");
        
        hints.put("clear:COMPONENT_IN_DULIB", 
            "Comando de conveniencia para limpiar el terminal. No afecta el estado " +
            "de la aplicación ni las operaciones en progreso.");
        
        hints.put("clear:COMPONENT_STANDALONE", 
            "Limpia el output acumulado. Especialmente útil después de comandos " +
            "con mucho output como 'apx show' o 'apx list'.");
        
        hints.put("exit:ROOT", 
            "'exit' cierra el terminal. Como la BD es en memoria (H2), al reiniciar " +
            "la aplicación se perderá todo tu trabajo. Úsalo solo cuando termines tu sesión de práctica.");
        
        hints.put("exit:DU_ONLINE", 
            "Salir del sistema desde cualquier ubicación cierra la sesión completa. " +
            "La arquitectura creada se perderá al ser base de datos en memoria.");
        
        hints.put("exit:DU_LIB", 
            "Comando de finalización de sesión. Recuerda que este es un entorno " +
            "educativo: cada reinicio te da un sistema limpio para practicar.");
        
        hints.put("exit:FOLDER", 
            "Cierra el terminal completamente. Para solo cambiar de ubicación " +
            "usa 'cd ..' o 'cd' (volver a root).");
        
        hints.put("exit:COMPONENT_IN_FOLDER", 
            "Finaliza la sesión del CLI. Todo el trabajo (componentes, dependencias) " +
            "se perderá ya que la BD es volátil. Úsalo solo al terminar tu práctica.");
        
        hints.put("exit:COMPONENT_IN_DULIB", 
            "Salida del sistema. En producción real, este comando cerraría tu conexión " +
            "al servidor APX. Aquí simplemente cierra el emulador educativo.");
        
        hints.put("exit:COMPONENT_STANDALONE", 
            "Comando de salida global. Funciona desde cualquier ubicación en la jerarquía. " +
            "Usa 'help' antes de salir si necesitas recordar comandos para tu próxima sesión.");
        
        hints.put("help:ROOT", 
            "'help' desde ROOT muestra todos los comandos disponibles. Presta atención " +
            "a qué comandos requieren contextos específicos (ej: 'apx init' solo funciona en ROOT).");
        
        hints.put("help:DU_ONLINE", 
            "El comando 'help' muestra la misma información desde cualquier ubicación, " +
            "pero algunos comandos solo funcionan en contextos específicos. 'apx add' solo funciona en DU-ONLINE.");
        
        hints.put("help:DU_LIB", 
            "Los DU-LIB son de solo lectura: solo puedes navegar y consultar, " +
            "no modificar. 'help' te muestra todos los comandos, pero 'apx add' estará bloqueado aquí.");
        
        hints.put("help:FOLDER", 
            "Desde carpetas puedes crear componentes con 'apx add' o eliminarlos con 'apx del'. " +
            "Revisa 'help' para ver la sintaxis completa de cada comando.");
        
        hints.put("help:COMPONENT_IN_FOLDER", 
            "Los comandos más útiles desde un componente son 'apx show' (ver detalles) " +
            "y 'apx add dep' (gestionar dependencias). Usa 'help' para ver ejemplos.");
        
        hints.put("help:COMPONENT_IN_DULIB", 
            "En componentes de librería, 'apx show' te muestra dependencias. " +
            "'help' incluye ejemplos de todos los comandos context-aware.");
        
        hints.put("help:COMPONENT_STANDALONE", 
            "Comando universal de ayuda. Muestra sintaxis, ejemplos y restricciones " +
            "de permisos por tipo de ubicación. Úsalo como referencia rápida.");
    }

    public String getHintFor(String command, PathType pathType) {
        if (command == null || pathType == null) {
            return null;
        }
        
        String key = command.toLowerCase() + ":" + pathType.name();
        return hints.get(key);
    }
}