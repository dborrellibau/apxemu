# V-Ether Banking Education Emulator

## Descripción
Aplicación educativa interactiva que emula comandos APX CLI para la enseñanza de arquitectura de software bancario. Proporciona una interfaz de terminal clásica combinada con visualización de diagramas de arquitectura en tiempo real.

## Inicio Rápido

### Requisitos Previos
- Java 8 o superior
- Node.js 14+ con npm
- Maven 3.6+

### Instalación y Ejecución

**1. Clonar el repositorio**
```bash
git clone https://github.com/dborrellibau/apxemu.git
cd apxemu
```

**2. Iniciar el Backend**
```bash
cd backend
mvn spring-boot:run
```
El backend se iniciará en `http://localhost:8080`

**3. Iniciar el Frontend** (en una nueva terminal)
```bash
cd frontend
npm install
npm start
```
El frontend se iniciará en `http://localhost:3000`

**4. Abrir el navegador**
Navegar a `http://localhost:3000`

## Funcionalidades Principales

### Comandos de Navegación
- `cd` - Navegar entre deployment units y carpetas
- `pwd` - Mostrar directorio actual
- `ls` - Listar contenidos del directorio actual

### Comandos APX
- `apx init` - Creación interactiva de componentes bancarios
- `apx add` - Agregar componentes (context-aware)
- `apx add dep` - Crear dependencias entre componentes
- `apx del` - Eliminar componentes (soft delete)
- `apx list` - Listar deployment units
- `apx show` - Mostrar detalles de componentes

### Características
- Interfaz de terminal con historial de comandos
- Visualización de arquitectura en tiempo real con ReactFlow
- Navegación jerárquica tipo sistema de archivos
- Comandos que se adaptan según el contexto de navegación
- Eliminación segura con confirmación (soft delete)
- Validación automática de códigos bancarios
- Base de datos en memoria H2
- Comunicación WebSocket para actualizaciones en tiempo real

## Arquitectura
- **Backend**: Spring Boot 2.7.18 con Java 8
- **Frontend**: React 18
- **Base de Datos**: H2 in-memory
- **Comunicación**: WebSocket (SockJS + STOMP)

## Documentación Completa
Para información detallada sobre comandos, ejemplos de uso, guías de desarrollo y troubleshooting, consulta la [Documentación Completa](DOCUMENTATION.md).

---

**V-Ether** - Sistema de Entrenamiento en Arquitectura Bancaria