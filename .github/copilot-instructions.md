# APX CLI Banking Education Emulator

## Project Overview
Interactive educational application that emulates APX CLI commands for teaching banking software architecture to new employees. Features a classic terminal console with real-time architecture diagram visualization.

## Architecture
- **Backend**: Spring Boot with WebSocket for real-time commands
- **Frontend**: React with terminal component and ReactFlow for diagrams  
- **Database**: H2 in-memory for session data
- **Build**: Maven multi-module project

## Key Components
- **Terminal Console**: Classic CLI interface for APX command emulation
- **Architecture Visualizer**: Real-time diagram showing deployment units and relationships
- **Command Parser**: Hierarchical command system with interactive menus
- **Entity Model**: Deployment units (artifacts) containing library/transactions/dto folders

## Development Patterns
- Use WebSocket for real-time console-to-diagram communication
- Follow banking domain terminology and security practices
- Maintain command hierarchy structure similar to real APX CLI
- Keep entities and relationships visually clear in diagram

## Core Entities
- **Deployment Unit**: Main architectural artifact
- **Library**: Component folder within deployment unit
- **Transactions**: Business logic folder within deployment unit  
- **DTO**: Data transfer objects folder within deployment unit