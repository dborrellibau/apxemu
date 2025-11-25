# APX CLI Banking Education Emulator

## Overview
Interactive educational application that emulates APX CLI commands for teaching banking software architecture to new employees. The application provides a classic terminal interface combined with real-time architecture diagram visualization.

## Features
- **Terminal Interface**: Classic command-line interface similar to real APX CLI
- **Real-time Visualization**: Interactive architecture diagrams showing deployment units and their relationships
- **Educational Focus**: Designed specifically for training new bank employees on software architecture
- **Secure Learning Environment**: Practice environment without access to real banking systems

## Architecture

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.2.0 with Java 17
- **Database**: H2 in-memory database for session data
- **Communication**: WebSocket for real-time terminal-to-diagram updates
- **Security**: Basic Spring Security configuration

### Frontend (React)
- **Framework**: React 18 with modern hooks
- **Terminal**: Custom terminal component with command history
- **Diagrams**: ReactFlow for interactive architecture visualization
- **Communication**: SockJS + STOMP for WebSocket connection

## Core Entities

### Deployment Units
Main architectural artifacts that can be created and connected:
- **ARTIFACT**: Standard deployment artifact
- **SERVICE**: Microservice deployment unit  
- **MODULE**: Modular component

Each deployment unit contains three component folders:
- **Library**: Shared components and utilities
- **Transactions**: Business logic and transaction handling
- **DTO**: Data Transfer Objects for communication

## Available Commands

### Basic Commands
```bash
help                           # Show available commands
create <type> <name>          # Create deployment unit (artifact|service|module)
list [type]                   # List all or filtered deployment units
connect <source> <target>     # Create dependency between units
show <name>                   # Show detailed information about a unit
clear                         # Remove all deployment units
```

### Example Usage
```bash
apx> create artifact customer-service
apx> create artifact account-service
apx> connect customer-service account-service
apx> list artifacts
apx> show customer-service
```

## Development Setup

### Prerequisites
- Java 17+
- Node.js 20+
- Maven 3.6+

### Running the Application

1. **Backend (Spring Boot)**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   Server starts on http://localhost:8080

2. **Frontend (React) - Development Mode**
   ```bash
   cd frontend
   npm install
   npm start
   ```
   Development server starts on http://localhost:3000

3. **Full Build**
   ```bash
   mvn clean install
   ```
   This builds both backend and frontend, copying React build to Spring Boot static resources.

### Access Points
- **Main Application**: http://localhost:8080
- **H2 Database Console**: http://localhost:8080/h2-console
  - URL: jdbc:h2:mem:apxdb
  - Username: sa
  - Password: (empty)

## Project Structure
```
apx-cli-emulator/
├── backend/                   # Spring Boot application
│   ├── src/main/java/com/bank/education/apxcli/
│   │   ├── model/            # JPA entities (DeploymentUnit, ComponentFolder)
│   │   ├── repository/       # Data access layer
│   │   ├── service/          # Business logic (ArchitectureService, CommandParserService)
│   │   ├── controller/       # REST and WebSocket controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   └── config/          # Security and WebSocket configuration
│   └── src/main/resources/
│       └── application.properties
├── frontend/                  # React application
│   ├── src/
│   │   ├── components/       # Terminal and ArchitectureDiagram components
│   │   ├── services/         # WebSocket service
│   │   └── App.js           # Main application component
│   └── package.json
└── pom.xml                   # Maven multi-module configuration
```

## Educational Use Cases

### Architecture Learning
- Understand deployment unit relationships
- Visualize service dependencies
- Practice creating modular architectures

### Command-Line Proficiency
- Learn APX CLI command patterns
- Practice hierarchical command structures
- Understand banking software terminology

### Safe Environment
- No access to real banking systems
- Risk-free exploration of commands
- Immediate visual feedback on architectural changes

## Extension Points
The application is designed to be easily extended with:
- Additional command types and hierarchies
- More complex entity relationships
- Enhanced visualization features
- Role-based learning paths
- Progress tracking and assessment tools