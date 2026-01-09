# APX CLI Banking Education Emulator

## Project Overview
Educational tool emulating APX CLI commands for teaching banking software architecture. Features a terminal interface with real-time ReactFlow diagram visualization showing deployment units and their relationships.

## Quick Start

**Backend** (Terminal 1):
```bash
cd backend
mvn spring-boot:run  # Runs on http://localhost:8080
```

**Frontend** (Terminal 2):
```bash
cd frontend
npm install  # First time only
npm start    # Runs on http://localhost:3000 with proxy to backend
```

**Note**: Both must run simultaneously. Frontend proxies API calls to backend via `proxy` in package.json.

## Architecture

### Backend (Spring Boot 2.7.18 + Java 8)
- **WebSocket Communication**: SockJS + STOMP for real-time command/diagram updates
- **H2 In-Memory DB**: Session data only, recreated on restart (`spring.jpa.hibernate.ddl-auto=create-drop`)
- **Service Layer Structure**: Commands organized by functional domain:
  - `navigation/` - PathNavigationService (type-safe filesystem navigation with 7 PathTypes)
  - `deletion/` - DeletionCommandService (soft delete with auto-navigation)
  - `dependencies/` - DependencyCommandService (interactive dependency wizard)
  - `forms/` - FormProcessingService + FormInputService (multi-step form wizards)
  - `strategy/` - DeploymentUnitStrategy pattern implementations
  - `validation/` - Banking code validation
- **CommandParserService**: Main orchestrator routing commands to specialized services

### Frontend (React 18)
- **Terminal Component**: Custom CLI with command history (arrow keys)
- **ReactFlow Diagram**: Live architecture visualization with custom nodes
- **WebSocketService**: Singleton managing SockJS connection and subscriptions
- **dataConverter.js**: Transforms backend entities to ReactFlow graph structure

## Core Concepts

### PathType System (7 Types)
Type-safe navigation through virtual filesystem hierarchy:
- `ROOT` (level 0) → `DU_ONLINE`/`DU_LIB`/`COMPONENT_STANDALONE` (level 1) → `FOLDER` (level 2) → `COMPONENT_IN_FOLDER` (level 3)
- Managed by PathNavigationService with parser/resolver/validator/navigator sub-components
- CommandPermissionService enforces context-aware command availability

### Strategy Pattern for Deployment Units
Each component type implements DeploymentUnitStrategy:
- **Container Strategies**: DuOnlineStrategy, DuLibStrategy (contain folders)
- **Simple Strategies**: DtoStrategy, LibStrategy, TrxStrategy (standalone components)
- Defines form prompts, validation rules, folder types, creation behavior

### FormState Session Management
Interactive multi-step flows tracked via `FormState` in `activeSessions` Map:
- Wizard-style forms with step progression
- Boolean flags for state machines: `awaitingComponentSelection`, `awaitingDeletionSelection`, `awaitingConfirmationFor`
- Shared across form services via constructor injection

### Confirmation Flow Pattern
Standardized confirmation for destructive operations:
1. Set `sessionState.setAwaitingConfirmationFor("action-id-params")`
2. Return `CommandResponse.info(ConfirmationMessages.STANDARD_CONFIRMATION)`
3. CommandParserService intercepts next input, parses Y/n/Enter
4. Route to action handler based on stored action string

### Soft Delete Pattern
Components never hard-deleted from database:
- Set `deleted` flag to true
- Filter deleted items in queries
- Allows educational "undo" scenarios
- DeletionCommandService auto-navigates after deletion if user is "inside" deleted item

### Transaction Management
Critical: Use `@Transactional(readOnly = true)` on query services to prevent LazyInitializationException when accessing entity relationships outside session context (common with JPA lazy loading).

## Key Development Patterns

### Adding New Commands
1. Create service in appropriate domain package (e.g., `service/newdomain/`)
2. Inject into CommandParserService constructor
3. Add routing logic in `parseCommand()` method
4. Update CommandPermissionService if command has PathType restrictions
5. Send diagram updates via DiagramService.sendDiagramUpdate()

### WebSocket Diagram Updates
Backend pushes updates after state changes:
```java
diagramService.sendDiagramUpdate();  // Broadcasts to /topic/diagram
```
Frontend subscribes and auto-renders new graph structure.

### Banking Domain Rules
- UUAA codes: 4 uppercase alphanumeric characters (e.g., CUST)
- Component codes follow banking naming conventions
- Validation centralized in `service/validation/` package

## Project-Specific Files

- [CommandParserService.java](backend/src/main/java/com/bank/education/apxcli/service/CommandParserService.java) - Main command router
- [PathNavigationService.java](backend/src/main/java/com/bank/education/apxcli/navigation/PathNavigationService.java) - Navigation facade
- [PathType.java](backend/src/main/java/com/bank/education/apxcli/navigation/model/PathType.java) - Virtual filesystem types
- [FormState.java](backend/src/main/java/com/bank/education/apxcli/dto/FormState.java) - Session state model
- [WebSocketService.js](frontend/src/services/WebSocketService.js) - Frontend WebSocket singleton
- [DOCUMENTATION.md](DOCUMENTATION.md) - Detailed command reference and examples
- [STRATEGY_PATTERN_INTEGRATION.md](STRATEGY_PATTERN_INTEGRATION.md) - Strategy pattern architecture
- [FUTURE_JOB_UTIL_IMPLEMENTATION.md](FUTURE_JOB_UTIL_IMPLEMENTATION.md) - Planned JOB/UTIL component types

## Common Issues

- **LazyInitializationException**: Add `@Transactional(readOnly = true)` to query methods
- **WebSocket not connecting**: Verify backend started first, check SockJS timeout (7s default)
- **Diagram not updating**: Check DiagramService.sendDiagramUpdate() called after entity changes
- **Terminal errors shown**: Both backend and frontend must run simultaneously; check terminal exit codes