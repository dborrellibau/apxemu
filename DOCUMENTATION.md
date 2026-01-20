# APX CLI Banking Education Emulator - Documentación Completa

## Tabla de Contenidos
1. [Arquitectura del Sistema](#arquitectura-del-sistema)
2. [Entidades Principales](#entidades-principales)
3. [Comandos Disponibles](#comandos-disponibles)
4. [Ejemplos de Uso](#ejemplos-de-uso)
5. [Desarrollo y Debugging](#desarrollo-y-debugging)
6. [Detalles Técnicos](#detalles-técnicos)
7. [Casos de Uso Educativos](#casos-de-uso-educativos)
8. [Contribución](#contribución)

## Arquitectura del Sistema

### Backend (Spring Boot)
- **Framework**: Spring Boot 2.7.18 con Java 8
- **Base de Datos**: H2 in-memory para datos de sesión
- **Comunicación**: WebSocket (SockJS + STOMP) para actualizaciones en tiempo real
- **Seguridad**: Configuración básica de Spring Security
- **Arquitectura**: Orientada a servicios con packages separados por funcionalidad
  - `info/` - Comandos de información
  - `navigation/` - Comandos de navegación con PathNavigationService
  - `deletion/` - Comandos de eliminación con auto-navegación
  - `dependencies/` - Gestión interactiva de dependencias
  - `forms/` - Procesamiento de formularios con validación
  - `validation/` - Validación de códigos bancarios
  - `permission/` - Sistema centralizado de permisos por PathType
  - `strategy/` - Implementaciones del patrón DeploymentUnitStrategy
- **Sistema de Navegación**: PathNavigationService con 7 PathTypes para navegación type-safe
- **Gestión de Transacciones**: @Transactional para prevenir LazyInitializationException

### Frontend (React)
- **Framework**: React 18 con hooks modernos
- **Terminal**: Componente de terminal personalizado con historial de comandos
- **Diagramas**: ReactFlow para visualización interactiva y arrastrable
- **Comunicación**: Cliente WebSocket para actualizaciones en tiempo real
- **Estilos**: CSS personalizado con estética de terminal

### Estructura del Proyecto
```
apxemu/
├── backend/
│   ├── src/main/java/com/bank/education/apxcli/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── navigation/
│   │   │   ├── PathNavigationService.java
│   │   │   ├── model/
│   │   │   ├── navigator/
│   │   │   ├── parser/
│   │   │   ├── resolver/
│   │   │   ├── validator/
│   │   │   └── permission/
│   │   ├── service/
│   │   │   ├── deletion/
│   │   │   ├── dependencies/
│   │   │   ├── forms/
│   │   │   ├── info/
│   │   │   ├── navigation/
│   │   │   ├── system/
│   │   │   ├── validation/
│   │   │   ├── strategy/                # Estrategias de DeploymentUnit
│   │   │   ├── CommandParserService.java
│   │   │   ├── ContainableCreationService.java
│   │   │   ├── ContainableInfoService.java
│   │   │   └── DiagramService.java
│   │   └── config/
│   └── src/main/resources/
│       ├── application.properties
│       └── static/
│
├── frontend/
│   ├── public/
│   │   └── index.html
│   ├── src/
│   │   ├── components/
│   │   │   ├── Terminal.js
│   │   │   ├── Terminal.css
│   │   │   ├── ReactFlowDiagram.js
│   │   │   ├── ReactFlowDiagram.css
│   │   │   ├── CustomNode.js
│   │   │   ├── HierarchicalRenderer.js
│   │   │   └── HierarchicalRenderer.css
│   │   ├── services/
│   │   │   └── WebSocketService.js
│   │   ├── utils/
│   │   │   └── dataConverter.js
│   │   ├── App.js
│   │   ├── App.css
│   │   └── index.js
│   └── package.json
└── pom.xml
```

## Entidades Principales

### Deployment Units (DU)
Artefactos arquitectónicos principales que representan componentes desplegables:

#### DU-ONLINE
Deployment units de banca online:
- **Contiene**: Carpetas DTO, Library, Transactions
- **Ejemplos**: Servicio de clientes, Gestión de cuentas
- **Uso**: Componentes de aplicaciones web/móvil

#### DU-LIB
Deployment units de librerías compartidas:
- **Contiene**: Componentes Base e Implementation
- **Ejemplos**: Utilidades comunes, Servicios compartidos
- **Uso**: Código reutilizable entre múltiples aplicaciones

### Componentes
Artefactos individuales dentro de deployment units:

- **Library (LIB)**: Componentes y utilidades reutilizables
- **Library Implementation (LIB-IMPL)**: Implementaciones concretas
- **DTO**: Data Transfer Objects para comunicación
- **Transactions (TRX)**: Lógica de negocio y manejadores de transacciones

### Carpetas de Componentes
Estructura organizacional dentro de deployment units:
- **dto/**: Objetos de transferencia de datos
- **library/**: Componentes compartidos
- **transactions/**: Lógica de negocio

## Comandos Disponibles

### Comandos de Navegación
Navega a través de la arquitectura como un sistema de archivos:

#### `cd <directory>`
Navega a un deployment unit o carpeta.

**Ejemplos:**
```bash
cd customer-service         # Navegar a un deployment unit
cd dto                      # Navegar a carpeta dto (desde DU)
cd customer-service/dto     # Navegación directa a carpeta
cd ../library               # Navegar a carpeta hermana
cd                          # Volver a root
```

#### `cd ..`
Retrocede al directorio padre.

```bash
V-Ether/customer-service/dto> cd ..
V-Ether/customer-service>
```

#### `pwd`
Muestra la ruta del directorio actual.

```bash
V-Ether/customer-service/dto> pwd
/customer-service/dto
```

#### `ls`
Lista el contenido del directorio actual.

```bash
V-Ether/customer-service> ls
dto/
library/
transactions/

V-Ether/customer-service/dto> ls
CUSTDTO001 (dto)
CUSTDTO002 (dto)
```

### Comandos APX de Gestión

#### `apx init` - Creación Interactiva de Componentes
Lanza un menú interactivo para crear deployment units bancarios.

**Restricciones de Permisos:**
- ✅ Solo permitido desde **ROOT**
- ❌ Bloqueado en DU, carpetas y componentes

**Características:**
- Selección de tipo de deployment unit (DU-ONLINE, DU-LIB)
- Prompts guiados para todos los campos requeridos
- Validación automática de códigos bancarios (UUAA)
- Creación en base de datos y actualización del diagrama
- Mantiene el prompt correcto durante todo el flujo

**Flujo:**
```bash
V-Ether/root> apx init

Seleccione el tipo de componente:
1. DU-ONLINE
2. DU-LIB
> 1

Ingrese el nombre del deployment unit: customer-service
Ingrese la descripción: Servicio de gestión de clientes
Ingrese el código UUAA (4 letras mayúsculas): CUST

Deployment unit 'customer-service' creado exitosamente.
```

#### `apx add` - Adición de Componentes
Agrega componentes (DTO, Library, Transaction) dentro de deployment units.

**Restricciones de Permisos:**
- ✅ Solo permitido desde **DU_ONLINE**
- ❌ Bloqueado en ROOT, DU_LIB, carpetas y componentes

**Flujo desde DU_ONLINE:**
```bash
V-Ether/customer-service> apx add

Seleccione el tipo de componente:
1. DTO (Data Transfer Objects)
2. Transaction (Business Transaction)
3. Library (Library Components)
vether/customer-service> 1

UUAA: CUST
Enter DTO Code - 3 digits (000-999):
vether/customer-service> 001
Enter Class Name:
vether/customer-service> CustomerDto
Enter Description:
vether/customer-service> Data transfer object de cliente
Do you want to continue with the operation? (Y/n): 
vether/customer-service> y

Created DTO 'CUSTC001' in customer-service/dto
```

**Características:**
- UUAA auto-detectado del DU padre
- Validación de códigos únicos
- Confirmación antes de crear
- Prompt mantiene contexto de navegación durante todo el flujo
- Actualización automática del diagrama

#### `apx add dep` - Gestión Interactiva de Dependencias
Crea dependencias entre componentes con workflow guiado.

**Restricciones de Permisos:**
- ✅ Solo permitido desde **COMPONENTES** (COMPONENT_IN_FOLDER, COMPONENT_IN_DULIB, COMPONENT_STANDALONE)
- ❌ Bloqueado en ROOT, DU y carpetas

**Flujo completo desde componente:**
```bash
V-Ether/customer-service/library/CUSTLIB001> apx add dep

Select dependency type for CUSTLIB001 (LIB):
1. DTO

Enter type number or name:
vether/customer-service/library/CUSTLIB001> 1

Enter artifact ID of the dependency (DTO):
vether/customer-service/library/CUSTLIB001> CUSTC001

Do you want to continue with the operation? (Y/n): 
vether/customer-service/library/CUSTLIB001> y

Created dependency: CUSTLIB001 -> CUSTC001
```

**Características:**
- Auto-detección del componente origen basado en tu ubicación
- Tipos de dependencia permitidos según el tipo de componente origen
- Validación de artifact IDs existentes
- Prevención de dependencias a LIB_IMPL (solo base LIB permitido)
- Confirmación antes de crear
- Prompt mantiene contexto de navegación durante todo el flujo
- Actualización automática del diagrama con nueva conexión

#### `apx del` - Eliminación de Componentes
Elimina componentes con confirmaciones de seguridad y menús interactivos.

**Restricciones de Permisos:**
- ✅ Solo permitido desde **DU_ONLINE**
- ❌ Bloqueado en ROOT, DU_LIB, carpetas y componentes

**Flujo desde DU_ONLINE:**
```bash
V-Ether/customer-service> apx del

Select component type to delete:
1. DTO (Data Transfer Objects)
2. Transaction (Business Transaction)
3. Library (Library Components)

Enter selection (1-3 or type name):
vether/customer-service> 1

Select DTO component to delete:
1. CUSTC001
2. CUSTC002

Enter selection (1-2):
vether/customer-service> 1

╔══════════════════════════════════════════════════════════╗
║          CONFIRMACIÓN DE ELIMINACIÓN                     ║
╚══════════════════════════════════════════════════════════╝

Componente: CUSTC001
Ubicación: customer-service/dto
Tipo: dto
Descripción: Data transfer object de cliente

NOTA: Si este elemento es dependencia de otro, recordá eliminar
      la dependencia manualmente con el comando apropiado.

¿Confirmar eliminación? (Y/n): 
vether/customer-service> y

✓ Component successfully marked as deleted

Component: CUSTC001
Type: dto

NOTE: If this component is referenced as a dependency elsewhere,
remember to remove those dependencies manually.
```

**Características:**
- Menús interactivos con selecciones numeradas por tipo
- Pantalla de confirmación detallada con información del componente
- Confirmación Y/n antes de eliminar
- Soft delete (marcado como eliminado, no removido físicamente)
- Auto-navegación al padre si eliminas el componente donde estás ubicado
- Items eliminados mostrados en rojo con marcador `[DELETED]`
- Actualizaciones del diagrama en tiempo real
2. CUSTDTO001
3. CUSTDTO002
> 2

¿Eliminar componente 'CUSTDTO001'? (Y/n): Y
Componente 'CUSTDTO001' eliminado.
```

**Nivel 3 (Componente):**
```bash
V-Ether/customer-service/dto/CUSTDTO001> apx del

¿Eliminar componente 'CUSTDTO001'? (Y/n): Y
Componente 'CUSTDTO001' eliminado.
```

**Características:**
- Menús interactivos con selecciones numeradas
- Confirmación Y/n antes de eliminar
- Soft delete (marcado como eliminado, no removido físicamente)
- Items eliminados mostrados en rojo con marcador `[DELETED]`
- Actualizaciones del diagrama en tiempo real

#### `apx list [type]` - Listar Componentes
Ve todos los deployment units o filtra por tipo. Comando global independiente de ubicación.

**Diferencia con `ls`:**
- `ls`: Muestra contenido local según tu ubicación (context-aware)
- `apx list`: Búsqueda global en todo el sistema

```bash
apx list                    # Listar todos los deployment units
apx list du-online          # Listar solo deployment units online
apx list du-lib             # Listar solo deployment units de librería
apx list dto                # Listar todos los DTOs del sistema
apx list lib                # Listar todas las librerías del sistema
apx list trx                # Listar todas las transacciones del sistema
```

**Salida incluye:**
- Solo nombres de componentes (sin prefijo de tipo)
- Estado de eliminación (marcado en rojo si está eliminado)
- Organizado por tipo

**Ejemplo de salida:**
```bash
V-Ether/root> apx list

customer-service
account-service [DELETED]
common-utils

Total deployment units: 3
```

#### `apx show` - Mostrar Detalles de Componentes
Muestra información detallada sobre componentes (context-aware):

**Nivel 1 (DU):**
```bash
V-Ether/customer-service> apx show
```

**Formato de salida DU:**
```
Artifact(du-online)
- Artifact: customer-service
- Description: Servicio de gestión de clientes
- Application(UUAA): CUST

Artifacts:
-----------
├─ customer-service\dto: 2
│  ├─ CUSTDTO001
│  ╰─ CUSTDTO002
├─ customer-service\library: 1
│  ╰─ CUSTLIB001
╰─ customer-service\transactions: 0
```

**Nivel 3 (Componente):**
```bash
V-Ether/customer-service/dto/CUSTDTO001> apx show
```

**Formato de salida Componente:**
```
Artifact(dto)
- Artifact: CUSTDTO001
- Description: Data transfer object de cliente
- Application(UUAA): CUST

Dependencies: 2
- ACCTDTO001
- USERDTO001
```

**Restricciones:**
- No disponible desde root (nivel 0)
- No disponible desde carpetas (nivel 2)
- Muestra estructura de árbol para DUs
- Muestra dependencias para componentes

### Comandos de Terminal

#### `clear`
Limpia la pantalla del terminal.

#### `exit`
Sale del terminal.

#### `help` / `apx help`
Muestra todos los comandos disponibles con ejemplos.

## Ejemplos de Uso

### Ejemplo 1: Creando tu Primera Arquitectura

```bash
# Comenzar en root
V-Ether/root> apx init

# Seguir prompts para crear DU-ONLINE "customer-service"
> Seleccionar: 1 (DU-ONLINE)
> Nombre: customer-service
> Descripción: Servicio de gestión de clientes
> UUAA: CUST

# Navegar y agregar componentes
V-Ether/root> cd customer-service
V-Ether/customer-service> apx add

# Seleccionar DTO
> Seleccionar: 1 (DTO)
> Código: 001
> Clase: CustomerDto
> Descripción: Data transfer object de cliente
> Confirmar: y

Created DTO 'CUSTC001' in customer-service/dto

# Agregar componente de librería
V-Ether/customer-service> apx add
> Seleccionar: 3 (Library)
> Código: 001
> Clase: CustomerLib
> Descripción: Librería de cliente
> Confirmar: y

Created Library 'CUSTLIB001' in customer-service/library

# Ver la estructura
V-Ether/customer-service> apx show
# (Ver la estructura de árbol con tus nuevos componentes)
```

### Ejemplo 2: Creando Dependencias

```bash
# Navegar a componente origen
V-Ether/customer-service/library/CUSTLIB001> apx add dep

Select dependency type for CUSTLIB001 (LIB):
1. DTO

> 1

Enter artifact ID of the dependency (DTO):
> CUSTC001

Do you want to continue with the operation? (Y/n):
> y

Created dependency: CUSTLIB001 -> CUSTC001
```

### Ejemplo 3: Navegando y Explorando

```bash
V-Ether/root> apx list

V-Ether/root> cd customer-service

V-Ether/customer-service> ls
dto/
library/
transactions/

V-Ether/customer-service> cd dto

V-Ether/customer-service/dto> ls
CUSTDTO001 (dto)
CUSTDTO002 (dto)

V-Ether/customer-service/dto> cd CUSTDTO001

V-Ether/customer-service/dto/CUSTDTO001> apx show
```

### Ejemplo 4: Eliminando Componentes

```bash
V-Ether/customer-service/dto/CUSTDTO001> apx del
¿Eliminar componente 'CUSTDTO001'? (Y/n): Y
Componente 'CUSTDTO001' eliminado

V-Ether/customer-service/dto> ls
CUSTDTO001 (dto) [DELETED]
CUSTDTO002 (dto)
```

## Desarrollo y Debugging

### Puntos de Acceso
- **Aplicación Principal**: http://localhost:3000 (dev) o http://localhost:8080 (prod)
- **Consola H2**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:apxdb`
  - Usuario: `sa`
  - Contraseña: (vacía)
- **API Backend**: http://localhost:8080/api/*
- **Endpoint WebSocket**: http://localhost:8080/ws

### Tips de Desarrollo

#### Hot Reload
- **Backend**: Spring Boot DevTools habilitado - recarga automáticamente en cambios de archivos
- **Frontend**: Servidor de desarrollo React tiene hot module replacement

#### Debugging
1. **Logs del Backend**: Revisar salida de consola para logs de nivel de servicio
2. **Consola del Frontend**: Abrir DevTools del navegador para ver mensajes WebSocket y errores de React
3. **Consola H2**: Consultar la base de datos en memoria directamente para inspeccionar estado de entidades
4. **Tráfico WebSocket**: Usar pestaña Network de DevTools del navegador para monitorear frames de WebSocket

#### Problemas Comunes

**Puerto Ya en Uso:**
```bash
# Backend (8080)
netstat -ano | findstr :8080
taskkill /PID <process_id> /F

# Frontend (3000)
netstat -ano | findstr :3000
taskkill /PID <process_id> /F
```

**Conexión WebSocket Fallida:**
- Asegurar que el backend está corriendo en puerto 8080
- Revisar configuración CORS en `SecurityConfig.java`
- Verificar endpoint WebSocket en `WebSocketService.js`

**Diagrama No se Actualiza:**
- Revisar consola del navegador para errores de WebSocket
- Verificar que DiagramService está llamando `notifyDiagramUpdate()`
- Asegurar que el método tiene anotación `@Transactional` si accede a colecciones lazy

## Detalles Técnicos

### Sistema de Navegación PathNavigationService
Sistema centralizado y type-safe para manejar navegación jerárquica:

**PathTypes (7 niveles):**
- `ROOT`: Nivel raíz del sistema
- `DU_ONLINE`: Deployment Unit Online
- `DU_LIB`: Deployment Unit Library
- `FOLDER`: Carpeta dentro de DU (dto/, library/, transactions/)
- `COMPONENT_IN_FOLDER`: Componente dentro de carpeta de DU_ONLINE
- `COMPONENT_IN_DULIB`: Componente dentro de DU_LIB
- `COMPONENT_STANDALONE`: Componente independiente (futuro)

**Componentes:**
- `PathParser`: Divide rutas en segmentos
- `PathTypeResolver`: Determina PathType con caché para performance
- `PathValidator`: Valida existencia de rutas con @Transactional
- `PathNavigator`: Maneja transiciones válidas entre tipos
- `NavigationPath`: Modelo inmutable de ruta con tipo

**Ventajas:**
- Type-safety en toda la navegación
- Validación centralizada de transiciones
- Caché para optimizar performance
- Prevención de LazyInitializationException con @Transactional

### Sistema de Permisos Centralizado
CommandPermissionService centraliza todas las validaciones de permisos:

**Métodos de Validación:**
- `canCreateDeploymentUnit(PathType)`: Solo ROOT
- `canCreateComponent(PathType)`: Solo DU_ONLINE
- `canCreateDependency(PathType)`: Solo componentes
- `canDelete(PathType)`: Solo DU_ONLINE
- `canDeleteDependency(PathType)`: Solo componentes

**Mensajes de Error:**
- `getPermissionDeniedMessage(command, PathType)`: Genera mensajes descriptivos
- `getPathTypeDescription(PathType)`: Descripciones legibles de cada tipo

**Flujo:**
1. CommandParserService detecta comando
2. Obtiene PathType actual usando PathNavigationService
3. Valida permisos con CommandPermissionService
4. Si permitido → ejecuta comando
5. Si bloqueado → retorna mensaje de error descriptivo

### Gestión de Prompts Consistente
Mantiene el prompt correcto durante todos los flujos interactivos:

**Implementación:**
- `FormState.getCurrentPrompt()`: Genera prompt basado en currentDirectory
- Todos los `CommandResponse` llevan el prompt actualizado
- Establecido en múltiples puntos:
  - Después de navegación (cd)
  - Durante flujos de formularios (apx add)
  - Durante flujos de dependencias (apx add dep)
  - Después de confirmaciones (Y/n)
  - En mensajes de error de permisos

**Formato:**
- Root: `vether>`
- DU: `vether/customer-service>`
- Carpeta: `vether/customer-service/library>`
- Componente: `vether/customer-service/library/CUSTLIB001>`

### Validación de Códigos Bancarios
- **UUAA (Código de Aplicación)**: 4 letras mayúsculas (ej: "CUST", "ACCT")
- **Código de Componente**: 3 dígitos (ej: "001", "002")
- **Nomenclatura de Componente**: Patrón `UUAA + TIPO + CODIGO` (ej: "CUSTC001")
- Validación automática previene códigos bancarios inválidos

### Patrón Soft Delete
- Componentes se marcan con flag `deleted = true`
- Componentes eliminados permanecen en base de datos (preserva integridad referencial)
- Filtrados en consultas y renderizado de diagrama
- Mostrados con marcador rojo `[DELETED]` en comandos de listado
- Dependencias a componentes eliminados se manejan automáticamente

### Auto-Navegación en Eliminación
Cuando eliminas un componente donde estás ubicado:
1. Sistema detecta que currentDirectory coincide con componente eliminado
2. Navega automáticamente al directorio padre (carpeta contenedora)
3. Actualiza sessionState.currentDirectory
4. Próximo prompt refleja nueva ubicación

**Ejemplo:**
```bash
vether/customer-service/library/CUSTLIB001> apx del
# (elimina CUSTLIB001)
✓ Component successfully marked as deleted
vether/customer-service/library> # Auto-navegado al padre
```

### Comandos Context-Aware
Estado de navegación determina comportamiento del comando:
- **ROOT**: `apx init` (crear DU), `apx list` (listar todos), `ls` (listar DUs)
- **DU_ONLINE**: `apx add` (crear componentes), `apx del` (eliminar), `ls` (listar carpetas)
- **DU_LIB**: Solo navegación y consulta, sin modificaciones
- **FOLDER**: `ls` (listar componentes en carpeta)
- **COMPONENT**: `apx add dep` (crear dependencias), `apx show` (ver detalles)

### Prevención de LazyInitializationException
Uso extensivo de `@Transactional(readOnly = true)` en métodos que acceden a colecciones lazy:

**Métodos anotados:**
- `PathValidator.folderExists()`
- `PathValidator.componentExists()`
- `PathValidator.componentExistsInFolder()`
- `ContainableInfoService.listComponentsInFolder()`
- `DeploymentUnitQueryService.listDeploymentUnits()`
- `DeploymentUnitQueryService.getAllDeploymentUnits()`

**Beneficios:**
- Previene excepciones al acceder a colecciones lazy
- Mantiene sesión Hibernate abierta durante traversal de entidades
- Performance optimizada con readOnly=true

### Arquitectura WebSocket
- **Protocolo**: STOMP sobre SockJS
- **Topics**: `/topic/diagram` para actualizaciones de arquitectura
- **Formato de Mensaje**: ContainableDto con estructura jerárquica
- **Trigger de Actualización**: Automático después de operaciones de crear/eliminar/dependencias
- **Sincronización en Tiempo Real**: Diagrama del frontend se actualiza sin recargar página

## Casos de Uso Educativos

### Aprendizaje de Arquitectura
- Entender relaciones y jerarquías de deployment units
- Visualizar dependencias de servicios en tiempo real
- Practicar creación de arquitecturas modulares por capas
- Aprender patrones de componentes de software bancario (DTO, Library, Transactions)

### Proficiencia en Línea de Comandos
- Dominar patrones y sintaxis de comandos APX CLI
- Practicar navegación jerárquica y comandos context-aware
- Entender terminología de software bancario (UUAA, deployment units)
- Aprender workflows interactivos guiados por menú

### Experimentación Segura
- Sin acceso a sistemas bancarios reales - completamente aislado
- Exploración libre de riesgos de comandos y arquitecturas
- Retroalimentación visual inmediata sobre cambios arquitectónicos
- Soft delete permite revertir errores
- Base de datos en memoria se reinicia al reiniciar

## Contribución

### Workflow de Desarrollo
1. Fork del repositorio
2. Crear rama de feature (`git checkout -b feature/amazing-feature`)
3. Realizar cambios
4. Probar exhaustivamente (backend + frontend)
5. Commit con mensajes claros (`git commit -m 'Agregar feature increíble'`)
6. Push a tu rama (`git push origin feature/amazing-feature`)
7. Abrir Pull Request

### Estándares de Código
- **Java**: Seguir convenciones de Spring Boot, usar `@Transactional` para operaciones de entidades
- **JavaScript**: Usar ES6+, componentes funcionales con hooks
- **Comentarios**: Documentar lógica compleja, especialmente patrones específicos de banca
- **Testing**: Agregar tests unitarios para cambios en capa de servicio

## Licencia

Este proyecto es para propósitos educativos dentro de programas de entrenamiento de organizaciones bancarias.

## Agradecimientos

- Diseñado para educación y onboarding de empleados bancarios
- Construido con Spring Boot, React, y ReactFlow
- Inspirado en patrones reales de APX CLI usados en software bancario

---

**V-Ether** 
