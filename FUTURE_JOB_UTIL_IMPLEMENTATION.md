# Implementación Futura: JOB y UTIL como Tipos Completos (Opción B)

**Fecha de documentación**: 3 de diciembre de 2025  
**Estado actual**: Implementado con Opción A (solo en `apx del`)  
**Estado futuro**: Pendiente migración a Opción B (tipos completos en el sistema)

---

## 📋 Resumen Ejecutivo

Este documento detalla cómo migrar de la implementación actual (Opción A) donde JOB y UTIL solo aparecen en el comando `apx del`, hacia una implementación completa (Opción B) donde JOB y UTIL son tipos de componentes reconocidos en todo el sistema, aunque sin funcionalidad completa.

---

## 🎯 Objetivos de la Opción B

1. ✅ JOB y UTIL aparecen en todos los menús relevantes (`apx add`, `apx del`, etc.)
2. ✅ Respuestas educativas consistentes en todo el sistema
3. ✅ Experiencia realista que emula la consola APX real
4. ❌ Sin implementación de creación/almacenamiento completo
5. ❌ Sin formularios interactivos complejos
6. ❌ Sin carpetas visuales en el diagrama (opcional)

---

## 📦 Implementación Actual (Opción A)

### Estructura del Comando `apx del`

**Ubicación**: `DeletionCommandService.java`

```java
public class DeletionCommandService {
    
    // JOB y UTIL solo existen aquí, no en el modelo de dominio
    private static final List<String> DELETION_OPTIONS = Arrays.asList(
        "dep", "dto", "job", "lib", "trx", "util"
    );
    
    public CommandResponse showDeletionMenu() {
        List<String> menu = Arrays.asList(
            "Select element type to delete:",
            "1. dep  - Delete dependency",
            "2. dto  - Delete DTO",
            "3. job  - Delete job",
            "4. lib  - Delete library", 
            "5. trx  - Delete transaction",
            "6. util - Delete utility"
        );
        // ...
    }
    
    public CommandResponse handleDeletionSelection(String selection) {
        // Lógica específica para JOB
        if (selection.equals("job") || selection.equals("3")) {
            return handleJobDeletion();
        }
        
        // Lógica específica para UTIL
        if (selection.equals("util") || selection.equals("6")) {
            return handleUtilDeletion();
        }
        
        // ... resto de opciones
    }
    
    private CommandResponse handleJobDeletion() {
        String currentPath = navigationService.getCurrentPath();
        
        if (isRootPath(currentPath)) {
            return CommandResponse.error("Error: no se puede eliminar elementos desde root");
        }
        
        if (isComponentPath(currentPath)) {
            return CommandResponse.error("This artifact is not a deployment unit online or batch");
        }
        
        if (isFolderPath(currentPath)) {
            return CommandResponse.error("This artifact is not a deployment unit online or batch");
        }
        
        if (isDUPath(currentPath)) {
            return CommandResponse.error("Error: file not found: pom.xml. No se encuentra implementado en el emulador");
        }
        
        return CommandResponse.error("Unknown context");
    }
    
    private CommandResponse handleUtilDeletion() {
        // En TODOS los contextos: mismo mensaje
        return CommandResponse.error("The artifact does not allow managing utilities");
    }
}
```

**Características clave de Opción A**:
- ✅ JOB y UTIL son strings literales, no enums
- ✅ Lógica de mensajes contenida en un solo servicio
- ✅ No afecta el resto del sistema
- ✅ Fácil de mantener y modificar

---

## 🔄 Migración a Opción B: Plan Detallado

### Paso 1: Actualizar el Enum `DeploymentUnitType`

**Archivo**: `DeploymentUnit.java`

```java
public enum DeploymentUnitType {
    DU_ONLINE("du-online"),
    DU_LIB("du-lib"),
    DTO("dto"), 
    LIB("lib"),
    LIB_IMPL("lib-impl"),
    TRX("trx"),
    JOB("job"),          // ← NUEVO
    UTIL("util");        // ← NUEVO
    
    private final String value;
    
    DeploymentUnitType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static DeploymentUnitType fromString(String text) {
        for (DeploymentUnitType type : DeploymentUnitType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        return DU_ONLINE; // default
    }
    
    // NUEVO: Helper para identificar tipos no implementados
    public boolean isNotImplemented() {
        return this == JOB || this == UTIL;
    }
    
    // NUEVO: Mensaje educativo para tipos no implementados
    public String getNotImplementedMessage() {
        switch (this) {
            case JOB:
                return "Error: file not found: pom.xml. No se encuentra implementado en el emulador";
            case UTIL:
                return "The artifact does not allow managing utilities";
            default:
                return null;
        }
    }
}
```

**Impacto**: 
- ⚠️ Todos los switches que procesan `DeploymentUnitType` necesitarán casos para JOB/UTIL o un `default` robusto
- ⚠️ La base de datos aceptará estos valores (ya que es `@Enumerated(EnumType.STRING)`)

---

### Paso 2: Actualizar Strategy Pattern

**Archivo**: `DeploymentUnitStrategyFactory.java`

```java
public class DeploymentUnitStrategyFactory {
    
    private final Map<DeploymentUnit.DeploymentUnitType, DeploymentUnitStrategy> strategies;
    
    public DeploymentUnitStrategyFactory() {
        strategies = new HashMap<>();
        
        // Estrategias existentes
        strategies.put(DeploymentUnit.DeploymentUnitType.DU_ONLINE, new DuOnlineStrategy());
        strategies.put(DeploymentUnit.DeploymentUnitType.DU_LIB, new DuLibStrategy());
        strategies.put(DeploymentUnit.DeploymentUnitType.DTO, new DtoStrategy());
        strategies.put(DeploymentUnit.DeploymentUnitType.LIB, new LibStrategy());
        strategies.put(DeploymentUnit.DeploymentUnitType.LIB_IMPL, new LibImplStrategy());
        strategies.put(DeploymentUnit.DeploymentUnitType.TRX, new TrxStrategy());
        
        // NUEVO: Estrategias para tipos no implementados
        strategies.put(DeploymentUnit.DeploymentUnitType.JOB, new NotImplementedStrategy(
            "Error: file not found: pom.xml. No se encuentra implementado en el emulador"
        ));
        strategies.put(DeploymentUnit.DeploymentUnitType.UTIL, new NotImplementedStrategy(
            "The artifact does not allow managing utilities"
        ));
    }
    
    public DeploymentUnitStrategy getStrategy(DeploymentUnit.DeploymentUnitType type) {
        DeploymentUnitStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for type: " + type);
        }
        return strategy;
    }
}
```

**Nueva clase**: `NotImplementedStrategy.java`

```java
package com.bank.education.apxcli.strategy;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.model.ComponentFolder;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for component types that are recognized but not fully implemented
 * Used for educational purposes to mirror real APX CLI behavior
 */
public class NotImplementedStrategy implements DeploymentUnitStrategy {
    
    private final String errorMessage;
    
    public NotImplementedStrategy(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    @Override
    public List<ComponentFolder.FolderType> getAllowedFolders() {
        // No folders allowed
        return Collections.emptyList();
    }
    
    @Override
    public List<ComponentFolder.FolderType> getRequiredFolders() {
        // No required folders
        return Collections.emptyList();
    }
    
    @Override
    public boolean canCreateComponent(String artifactId) {
        // Cannot create
        return false;
    }
    
    @Override
    public CommandResponse validateCreation(String artifactId) {
        return CommandResponse.error(errorMessage);
    }
    
    @Override
    public String getComponentDescription() {
        return "Not implemented component type";
    }
}
```

**Impacto**:
- ✅ Cualquier intento de crear JOB/UTIL retorna mensaje educativo
- ✅ No requiere modificar strategies existentes

---

### Paso 3: Actualizar Servicios de Validación

**Archivo**: `ContainableValidationService.java`

```java
private DeploymentUnit.DeploymentUnitType parseTypeFromString(String typeStr) {
    switch (typeStr.toLowerCase()) {
        case "du-online": return DeploymentUnit.DeploymentUnitType.DU_ONLINE;
        case "du-lib": return DeploymentUnit.DeploymentUnitType.DU_LIB;
        case "dto": case "dtos": return DeploymentUnit.DeploymentUnitType.DTO;
        case "lib": case "libs": return DeploymentUnit.DeploymentUnitType.LIB;
        case "lib-impl": return DeploymentUnit.DeploymentUnitType.LIB_IMPL;
        case "trx": case "trxs": return DeploymentUnit.DeploymentUnitType.TRX;
        case "job": case "jobs": return DeploymentUnit.DeploymentUnitType.JOB;      // ← NUEVO
        case "util": case "utils": return DeploymentUnit.DeploymentUnitType.UTIL;   // ← NUEVO
        default: return null;
    }
}
```

**Archivo**: `DeploymentUnitQueryService.java` (mismo cambio)

**Impacto**:
- ⚠️ Ahora `parseTypeFromString("job")` retorna un tipo válido en lugar de null
- ⚠️ Necesitarás manejar estos tipos en servicios que los procesen

---

### Paso 4: Actualizar Servicio de Creación

**Archivo**: `ContainableCreationService.java`

```java
public CommandResponse createDeploymentUnit(/* params */) {
    // ... validaciones previas ...
    
    // NUEVO: Validación temprana para tipos no implementados
    if (type.isNotImplemented()) {
        return CommandResponse.error(type.getNotImplementedMessage());
    }
    
    // Determinar la carpeta destino
    ComponentFolder.FolderType folderType;
    switch (type) {
        case DTO:
            folderType = ComponentFolder.FolderType.DTO;
            break;
        case LIB:
        case LIB_IMPL:
            folderType = ComponentFolder.FolderType.LIBRARY;
            break;
        case TRX:
            folderType = ComponentFolder.FolderType.TRANSACTIONS;
            break;
        case JOB:      // ← NUEVO: casos explícitos
        case UTIL:     // ← NUEVO: casos explícitos
            // Nunca debería llegar aquí por validación temprana
            return CommandResponse.error("Component type not supported");
        default:
            return CommandResponse.error("Invalid component type for folder creation");
    }
    
    // ... resto de la lógica ...
}
```

**Impacto**:
- ✅ Los usuarios pueden intentar crear JOB/UTIL
- ✅ Reciben mensajes educativos apropiados
- ✅ Nada se crea en base de datos

---

### Paso 5: Actualizar Comando `apx add`

**Archivo**: `CommandParserService.java`

```java
case "add":
    if (parts.length > 1) {
        String subCommand = parts[1].toLowerCase();
        switch (subCommand) {
            case "dto":
            case "lib":
            case "trx":
            case "job":      // ← NUEVO
            case "util":     // ← NUEVO
                // Iniciar flujo de creación
                return handleAddComponentCommand(subCommand);
                
            case "dep":
                return dependencyCommandService.handleAddDepCommand();
                
            // ... resto ...
        }
    }
    // Si solo escribió "apx add", mostrar menú
    return showAddMenu();
```

**Método**: `showAddMenu()`

```java
private CommandResponse showAddMenu() {
    List<String> menu = Arrays.asList(
        "Select element type to create:",
        "1. dto  - Create Data Transfer Object",
        "2. job  - Create Job",           // ← NUEVO
        "3. lib  - Create Library",
        "4. trx  - Create Transaction",
        "5. util - Create Utility"        // ← NUEVO
    );
    
    formState.setAwaitingAddSelection(true);
    
    return new CommandResponse(true, "", menu, 
        CommandResponse.ResponseType.MENU, null);
}
```

**Impacto**:
- ⚠️ Los usuarios verán JOB y UTIL en el menú de `apx add`
- ✅ Al intentar crearlos, recibirán mensajes educativos
- ⚠️ Necesitarás actualizar `handleAddComponentCommand()` para manejar JOB/UTIL

---

### Paso 6: Actualizar FormState (Opcional)

**Archivo**: `FormState.java`

**Decisión**: ¿Agregar flags para JOB y UTIL?

**Opción 6A: NO agregar flags** (Recomendado)
```java
// NO agregar:
// private boolean awaitingJobInput;
// private boolean awaitingUtilInput;

// En su lugar, usar un flag genérico:
private boolean awaitingNotImplementedTypeInput;
private String notImplementedType; // "JOB" o "UTIL"

public void setAwaitingNotImplementedType(String type) {
    clearAllFlags();
    this.awaitingNotImplementedTypeInput = true;
    this.notImplementedType = type;
}
```

**Opción 6B: Agregar flags completos** (Solo si quieres simetría)
```java
// Agregar como los demás tipos:
private boolean awaitingJobCode;
private boolean awaitingJobDescription;
// ... etc para cada campo

// PERO: Estos flujos siempre fallarán educativamente
```

**Recomendación**: Opción 6A. No necesitas formularios completos porque nunca se completarán.

---

### Paso 7: Actualizar ComponentFolder.FolderType (Opcional)

**Archivo**: `ComponentFolder.java`

**Decisión**: ¿Crear carpetas JOBS y UTILS en DU_ONLINE/DU_LIB?

**Si NO quieres carpetas visuales** (Recomendado):
```java
// No agregar nada. JOB y UTIL no tienen carpetas
```

**Si SÍ quieres carpetas visuales** (Para máximo realismo):
```java
public enum FolderType {
    LIBRARY("Library Components"),
    TRANSACTIONS("Business Transactions"), 
    DTO("Data Transfer Objects"),
    JOBS("Job Components"),         // ← NUEVO
    UTILS("Utility Components"),    // ← NUEVO
    SRC("Source Code"),
    TEST("Test Code"),
    RESOURCES("Resources"),
    PARENT("Parent Container");
    
    // ...
}
```

**Y actualizar**: `DuOnlineStrategy.java`

```java
@Override
public List<ComponentFolder.FolderType> getAllowedFolders() {
    return Arrays.asList(
        ComponentFolder.FolderType.TRANSACTIONS,
        ComponentFolder.FolderType.LIBRARY,
        ComponentFolder.FolderType.DTO,
        ComponentFolder.FolderType.JOBS,     // ← NUEVO
        ComponentFolder.FolderType.UTILS     // ← NUEVO
    );
}
```

**Impacto**:
- ⚠️ Las carpetas aparecerán vacías (no se pueden crear componentes)
- ⚠️ Navegación funcionará pero `apx list` mostrará "0 elements"
- ✅ Visualmente más realista

---

### Paso 8: Actualizar ArtifactIdValidationService (NO NECESARIO)

**NO hacer nada aquí**. JOB y UTIL no tienen patrones de validación definidos en tu consola real.

Si algún día necesitas validación:
```java
// Potencial patrón futuro (inventado):
private static final Pattern JOB_PATTERN = Pattern.compile("^[A-Z]{4}J\\d{3}$");
private static final Pattern UTIL_PATTERN = Pattern.compile("^[A-Z]{4}U\\d{3}$");
```

---

### Paso 9: Actualizar DeletionCommandService

**Simplificación**: Con Opción B, el código se vuelve más limpio:

```java
public CommandResponse handleDeletionSelection(String selection) {
    DeploymentUnit.DeploymentUnitType type = parseSelectionToType(selection);
    
    // NUEVO: Manejo unificado de tipos no implementados
    if (type.isNotImplemented()) {
        return handleNotImplementedTypeDeletion(type);
    }
    
    // Resto de lógica para tipos implementados
    // ...
}

private CommandResponse handleNotImplementedTypeDeletion(DeploymentUnit.DeploymentUnitType type) {
    String currentPath = navigationService.getCurrentPath();
    
    if (isRootPath(currentPath)) {
        return CommandResponse.error("Error: no se puede eliminar elementos desde root");
    }
    
    // Para JOB: diferentes mensajes según contexto
    if (type == DeploymentUnit.DeploymentUnitType.JOB) {
        if (isComponentPath(currentPath) || isFolderPath(currentPath)) {
            return CommandResponse.error("This artifact is not a deployment unit online or batch");
        }
        if (isDUPath(currentPath)) {
            return CommandResponse.error("Error: file not found: pom.xml. No se encuentra implementado en el emulador");
        }
    }
    
    // Para UTIL: siempre el mismo mensaje
    if (type == DeploymentUnit.DeploymentUnitType.UTIL) {
        return CommandResponse.error("The artifact does not allow managing utilities");
    }
    
    return CommandResponse.error("Unknown error");
}
```

---

## 📊 Resumen de Cambios por Archivo

| Archivo | Cambios Requeridos | Riesgo | Prioridad |
|---------|-------------------|--------|-----------|
| `DeploymentUnit.java` | Agregar JOB/UTIL al enum | ⚠️ Medio | 🔴 Alta |
| `DeploymentUnitStrategyFactory.java` | Agregar NotImplementedStrategy | ✅ Bajo | 🔴 Alta |
| `NotImplementedStrategy.java` | Crear nueva clase | ✅ Bajo | 🔴 Alta |
| `ContainableValidationService.java` | Actualizar parseTypeFromString() | ✅ Bajo | 🔴 Alta |
| `DeploymentUnitQueryService.java` | Actualizar parseTypeFromString() | ✅ Bajo | 🔴 Alta |
| `ContainableCreationService.java` | Agregar validación temprana | ⚠️ Medio | 🔴 Alta |
| `CommandParserService.java` | Actualizar menú y casos | ⚠️ Medio | 🟡 Media |
| `FormState.java` | Opcional: agregar flags | ✅ Bajo | 🟢 Baja |
| `ComponentFolder.java` | Opcional: agregar carpetas | ⚠️ Medio | 🟢 Baja |
| `DuOnlineStrategy.java` | Opcional: permitir carpetas | ⚠️ Medio | 🟢 Baja |
| `DeletionCommandService.java` | Simplificar con enum | ✅ Bajo | 🟡 Media |
| `ArtifactIdValidationService.java` | NO CAMBIAR | ✅ N/A | N/A |

---

## 🧪 Plan de Testing para Opción B

### Test 1: Intentar crear JOB desde DU
```bash
vether/mydu> apx add job
# Esperado: "Error: file not found: pom.xml. No se encuentra implementado en el emulador"
```

### Test 2: Intentar crear UTIL desde DU
```bash
vether/mydu> apx add util
# Esperado: "The artifact does not allow managing utilities"
```

### Test 3: Listar carpetas con JOBS/UTILS (si se implementó)
```bash
vether/mydu> apx list
# Esperado: Mostrar carpetas jobs/ y utils/ vacías
```

### Test 4: JOB y UTIL en dependencias
```bash
vether/mydu/transactions/MYTRX001-01-AR> apx add dep
# JOB y UTIL NO deben aparecer como opciones de dependencia
```

### Test 5: Eliminar JOB desde root
```bash
vether> apx del
> job
# Esperado: "Error: no se puede eliminar elementos desde root"
```

---

## ⚠️ Consideraciones Importantes

### 1. Base de Datos
- ⚠️ JOB y UTIL podrían ser guardados accidentalmente si no validas temprano
- ✅ Agregar constraint en aplicación, no en DB

### 2. Frontend
- ⚠️ Si creas carpetas JOBS/UTILS, necesitarás colores en `dataConverter.js`
- ✅ Reusar color genérico o agregar específicos

### 3. Mensajes en Español vs Inglés
- ⚠️ Inconsistencia actual: algunos errores en español, otros en inglés
- 📝 Decisión pendiente: estandarizar idioma

### 4. Ayuda (Help)
- 📝 Actualizar `apx help` para mencionar JOB y UTIL
- 📝 Agregar nota educativa: "job y util no están completamente implementados"

---

## 🎯 Orden de Implementación Recomendado

### Fase 1: Core (Mínimo viable)
1. ✅ Agregar JOB/UTIL a `DeploymentUnitType` enum
2. ✅ Crear `NotImplementedStrategy.java`
3. ✅ Actualizar `DeploymentUnitStrategyFactory`
4. ✅ Actualizar servicios de parsing

### Fase 2: Integración (Funcionalidad completa)
5. ✅ Actualizar `ContainableCreationService`
6. ✅ Actualizar `CommandParserService` (menú add)
7. ✅ Actualizar `DeletionCommandService` (simplificar)

### Fase 3: Opcionales (Realismo extra)
8. 🔶 Agregar carpetas JOBS/UTILS
9. 🔶 Actualizar estrategias de DU para permitir carpetas
10. 🔶 Actualizar frontend para visualizar carpetas

### Fase 4: Pulido (Experiencia final)
11. 🔶 Actualizar mensajes de ayuda
12. 🔶 Agregar tests unitarios
13. 🔶 Documentar en README

---

## 📝 Notas Finales

- **Tiempo estimado Opción B**: 3-4 horas de desarrollo + 2 horas de testing
- **Líneas de código afectadas**: ~300-400 líneas
- **Archivos modificados**: 8-12 archivos
- **Riesgo de regresión**: Medio (requiere testing exhaustivo)

**Recomendación**: Implementar Opción B después de completar y estabilizar Opción A. Usar esta documentación como guía paso a paso.

---

**Documento creado**: 3 de diciembre de 2025  
**Autor**: Sistema de desarrollo APX Emulator  
**Versión**: 1.0
