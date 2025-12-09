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
  - `navigation/` - Comandos de navegación
  - `deletion/` - Comandos de eliminación
  - `dependencies/` - Gestión de dependencias
  - `forms/` - Procesamiento de formularios
  - `validation/` - Validación de códigos bancarios

### Frontend (React)
- **Framework**: React 18 con hooks modernos
- **Terminal**: Componente de terminal personalizado con historial de comandos
- **Diagramas**: ReactFlow para visualización interactiva y arrastrable
- **Comunicación**: Cliente WebSocket para actualizaciones en tiempo real
- **Estilos**: CSS personalizado con estética de terminal

### Estructura del Proyecto
```
apxemu/
├── backend/                          # Aplicación Spring Boot
│   ├── src/main/java/com/bank/education/apxcli/
│   │   ├── controller/              # Controladores REST y WebSocket
│   │   ├── dto/                     # Data Transfer Objects (FormState, CommandResponse)
│   │   ├── model/                   # Entidades JPA (DeploymentUnit, ComponentFolder)
│   │   ├── repository/              # Capa de acceso a datos
│   │   ├── service/
│   │   │   ├── deletion/            # Servicio de comandos de eliminación
│   │   │   ├── dependencies/        # Gestión de dependencias
│   │   │   ├── forms/               # Procesamiento de formularios y prompts
│   │   │   ├── info/                # Comandos de información y show
│   │   │   ├── navigation/          # Comandos de navegación (cd, pwd, ls)
│   │   │   ├── system/              # Comandos de sistema (help, clear)
│   │   │   ├── validation/          # Validación de códigos bancarios
│   │   │   ├── CommandParserService.java          # Router principal de comandos
│   │   │   ├── ContainableCreationService.java    # Creación de componentes
│   │   │   ├── ContainableInfoService.java        # Consultas de componentes
│   │   │   └── DiagramService.java                # Actualizaciones de diagrama vía WebSocket
│   │   └── config/                  # Configuración de seguridad y WebSocket
│   └── src/main/resources/
│       ├── application.properties   # Configuración de Spring Boot
│       └── static/                  # Archivos React compilados (producción)
│
├── frontend/                         # Aplicación React
│   ├── public/
│   │   └── index.html               # Template HTML
│   ├── src/
│   │   ├── components/
│   │   │   ├── Terminal.js          # Componente de interfaz de terminal
│   │   │   ├── Terminal.css         # Estilos del terminal
│   │   │   ├── ReactFlowDiagram.js  # Diagrama de arquitectura
│   │   │   ├── ReactFlowDiagram.css # Estilos del diagrama
│   │   │   ├── CustomNode.js        # Renderizador de nodos personalizados
│   │   │   ├── HierarchicalRenderer.js  # Layout jerárquico
│   │   │   └── HierarchicalRenderer.css
│   │   ├── services/
│   │   │   └── WebSocketService.js  # Cliente WebSocket
│   │   ├── utils/
│   │   │   └── dataConverter.js     # Conversión de datos backend a formato ReactFlow
│   │   ├── App.js                   # Componente principal de aplicación
│   │   ├── App.css                  # Estilos de aplicación
│   │   └── index.js                 # Punto de entrada de aplicación
│   └── package.json                 # Dependencias de Node
│
└── pom.xml                          # Configuración Maven multi-módulo
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
Lanza un menú interactivo para crear componentes bancarios:

**Características:**
- Selección de tipo de deployment unit (DU-ONLINE, DU-LIB)
- Prompts guiados para todos los campos requeridos
- Validación automática de códigos bancarios (UUAA)
- Creación en base de datos y actualización del diagrama

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

#### `apx add` - Adición de Componentes Context-Aware
Agrega componentes basándose en tu ubicación actual:

**Nivel 0 (Root):**
```bash
V-Ether/root> apx add
# Muestra menú para crear DU
```

**Nivel 1 (DU):**
```bash
V-Ether/customer-service> apx add

Seleccione el tipo de componente:
1. DTO
2. Library
3. Transaction
> 1

Ingrese el nombre del componente: CUSTDTO001
Ingrese la descripción: Data transfer object de cliente
Ingrese el código UUAA: CUST

Componente 'CUSTDTO001' creado en customer-service/dto
```

**Nivel 2 (Carpeta):**
```bash
V-Ether/customer-service/dto> apx add
# Crea componente directamente en la carpeta actual
```

#### `apx add dep` - Gestión Interactiva de Dependencias
Crea dependencias entre componentes con workflow guiado:

**Flujo completo:**
```bash
V-Ether/root> apx add dep

Paso 1: Seleccionar componente origen
Componentes disponibles:
1. customer-service
2. account-service
3. user-service
Seleccione el número del componente origen: 1

Paso 2: Seleccionar componente destino
Componentes disponibles:
1. account-service
2. user-service
Seleccione el número del componente destino: 2

Confirmar creación de dependencia: customer-service → user-service? (Y/n): Y

Dependencia creada exitosamente.
```

**Características:**
- Menús numerados interactivos
- Validación de componentes existentes
- Confirmación antes de crear
- Actualización automática del diagrama con nueva conexión

#### `apx del` - Eliminación Context-Aware
Elimina componentes con confirmaciones de seguridad:

**Nivel 0 (Root):**
```bash
V-Ether/root> apx del

¿Qué desea eliminar?
1. customer-service (DU-ONLINE)
2. account-service (DU-ONLINE)
> 1

¿Eliminar deployment unit 'customer-service'? (Y/n): Y
Deployment unit 'customer-service' eliminado.
```

**Nivel 1 (DU):**
```bash
V-Ether/customer-service> apx del

¿Qué desea eliminar?
1. dto (carpeta completa)
2. library (carpeta completa)
3. transactions (carpeta completa)
4. Navegar a componentes específicos
> 4

Seleccione carpeta:
1. dto
2. library
> 1

Componentes en dto:
1. CUSTDTO001
2. CUSTDTO002
> 1

¿Eliminar componente 'CUSTDTO001'? (Y/n): Y
Componente 'CUSTDTO001' eliminado.
```

**Nivel 2 (Carpeta):**
```bash
V-Ether/customer-service/dto> apx del

¿Qué desea eliminar?
1. dto (carpeta completa)
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
Ve todos los deployment units o filtra por tipo:

```bash
apx list                    # Listar todos los deployment units
apx list du-online          # Listar solo deployment units online
apx list du-lib             # Listar solo deployment units de librería
```

**Salida incluye:**
- Nombre y tipo del componente
- Estado de eliminación (marcado en rojo si está eliminado)
- Organizado por tipo

**Ejemplo de salida:**
```bash
V-Ether/root> apx list

Deployment Units:
=================

DU-ONLINE:
- customer-service
- account-service [DELETED]

DU-LIB:
- common-utils
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
[Aparece menú interactivo]
> Seleccionar: 1 (DU-ONLINE)
> Nombre: customer-service
> Descripción: Servicio de gestión de clientes
> UUAA: CUST

# Navegar y agregar componentes
V-Ether/root> cd customer-service
V-Ether/customer-service> apx add

# Seleccionar DTO
> Seleccionar: 1 (DTO)
> Nombre: CUSTDTO001
> Descripción: Objeto de datos de cliente

# Agregar componente de librería
V-Ether/customer-service> apx add
> Seleccionar: 2 (Library)
> Nombre: CUSTLIB001

# Ver la estructura
V-Ether/customer-service> apx show
# (Ver la estructura de árbol con tus nuevos componentes)
```

### Ejemplo 2: Creando Dependencias

```bash
# Crear dos componentes primero (usando apx init o apx add)
# Luego crear dependencia

V-Ether/root> apx add dep

# Paso 1: Seleccionar origen
Seleccionar componente origen (o DU):
1. customer-service
2. account-service
> 1

# Paso 2: Seleccionar destino
Seleccionar componente destino:
1. account-service
2. user-service
> 1

# Confirmación
¿Crear dependencia: customer-service → account-service? (Y/n)
> Y

# Dependencia creada, diagrama se actualiza automáticamente
```

### Ejemplo 3: Navegando y Explorando

```bash
# Listar todos los componentes
V-Ether/root> apx list

# Navegar a un DU
V-Ether/root> cd customer-service

# Listar contenido de carpetas
V-Ether/customer-service> ls
dto/
library/
transactions/

# Navegar a carpeta
V-Ether/customer-service> cd dto

# Listar componentes en carpeta
V-Ether/customer-service/dto> ls
CUSTDTO001 (dto)
CUSTDTO002 (dto)

# Navegar a componente
V-Ether/customer-service/dto> cd CUSTDTO001

# Mostrar detalles del componente
V-Ether/customer-service/dto/CUSTDTO001> apx show
```

### Ejemplo 4: Eliminando Componentes

```bash
# Eliminar desde nivel de componente (directo)
V-Ether/customer-service/dto/CUSTDTO001> apx del
¿Eliminar componente 'CUSTDTO001'? (Y/n): Y
Componente 'CUSTDTO001' eliminado

# Eliminar desde nivel de carpeta (menú)
V-Ether/customer-service/dto> apx del
¿Qué desea eliminar?
1. dto (carpeta completa)
2. CUSTDTO002
> 2
¿Eliminar componente 'CUSTDTO002'? (Y/n): Y

# Componentes eliminados aparecen en rojo al listar
V-Ether/customer-service/dto> ls
CUSTDTO001 (dto) [DELETED]
CUSTDTO002 (dto) [DELETED]
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

### Validación de Códigos Bancarios
- **UUAA (Código de Aplicación)**: 4 letras mayúsculas (ej: "CUST", "ACCT")
- **Código de Componente**: 3 dígitos (ej: "001", "002")
- **Nomenclatura de Componente**: Patrón `UUAA + TIPO + CODIGO` (ej: "CUSTDTO001")
- Validación automática previene códigos bancarios inválidos

### Patrón Soft Delete
- Componentes se marcan con flag `deleted = true`
- Componentes eliminados permanecen en base de datos (preserva integridad referencial)
- Filtrados en consultas y renderizado de diagrama
- Mostrados con marcador rojo `[DELETED]` en comandos de listado
- Dependencias a componentes eliminados se manejan automáticamente

### Comandos Context-Aware
Estado de navegación determina comportamiento del comando:
- **Nivel 0 (root)**: Crear DU, listar todos, gestionar operaciones a nivel DU
- **Nivel 1 (DU)**: Crear carpetas/componentes, gestionar contenidos de DU
- **Nivel 2 (carpeta)**: Crear componentes en carpeta, listar contenido de carpeta
- **Nivel 3 (componente)**: Mostrar detalles de componente, gestionar componente

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

**V-Ether** - Sistema de Entrenamiento en Arquitectura Bancaria Virtual
