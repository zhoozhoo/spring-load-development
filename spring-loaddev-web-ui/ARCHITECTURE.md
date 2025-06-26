# Web UI Architecture

## Overview
The `spring-loaddev-web-ui` module is a React-based frontend with a Spring Boot backend that serves as the user interface for the Spring Load Development microservices system.

## Key Design Principles

### 1. API Gateway Only Communication
- **All backend API calls** go through the `spring-loaddev-api-gateway` (http://localhost:8080)
- **No direct database access** - the Web UI backend has no database dependencies
- **No direct microservice communication** - all data fetching is via the API Gateway

### 2. Technology Stack

#### Backend (Spring Boot)
- **Purpose**: Serve React frontend and handle OAuth2 authentication
- **Dependencies**:
  - `spring-boot-starter-web` - Web framework
  - `spring-boot-starter-security` - Security framework
  - `spring-boot-starter-oauth2-client` - OAuth2 authentication
  - `spring-boot-starter-webflux` - WebClient for API Gateway calls
  - `spring-cloud-starter-netflix-eureka-client` - Service discovery
  - `spring-cloud-starter-config` - Configuration management

#### Frontend (React)
- **Purpose**: User interface for CRUD operations on Load data
- **API Communication**: All requests routed through API Gateway proxy
- **Authentication**: OAuth2 integration with GitHub/Google

### 3. Component Architecture

#### Backend Components
```
WebUiApplication.java - Main application class (excludes JPA auto-configuration)
├── config/
│   ├── SecurityConfig.java - OAuth2 and security configuration
│   └── WebClientConfig.java - API Gateway WebClient configuration
├── controller/
│   └── WebController.java - Serves React frontend and login pages
├── service/
│   └── LoadService.java - Business logic using WebClient for API Gateway calls
└── dto/
    └── LoadDto.java - Data transfer objects (no JPA annotations)
```

#### Frontend Components
```
src/
├── components/
│   ├── Navigation.js - Main navigation
│   ├── Home.js - Landing page
│   ├── LoadList.js - List and search loads
│   ├── LoadForm.js - Create/edit load form
│   ├── LoadDetail.js - View load details
│   └── Profile.js - User profile
├── services/
│   └── api.js - Axios configuration pointing to API Gateway
└── App.js - Main application component
```

### 4. Communication Flow

#### Data Flow
```
React Frontend → API Gateway (localhost:8080) → Loads Service → Database
     ↑                                                ↓
Spring Boot Backend (WebClient) ←←←←←←←←←←←←←←←←←←←←←←←←
```

#### Authentication Flow
```
User → Web UI OAuth2 → GitHub/Google → Access Token → API Gateway (with token)
```

### 5. Configuration

#### Backend Configuration (`application.yml`)
- **No database configuration** - all JPA settings removed
- **API Gateway URL**: `api.gateway.base-url: http://localhost:8080`
- **OAuth2 providers**: GitHub and Google
- **Server port**: 8087

#### Frontend Configuration
- **API Base URL**: `http://localhost:8080` (API Gateway)
- **Webpack proxy**: Routes `/api/*` to API Gateway
- **Development port**: 3000

### 6. Security
- OAuth2 authentication with GitHub/Google
- WebClient configured with OAuth2 token relay to API Gateway
- CSRF protection enabled
- Session-based authentication for React frontend

### 7. Build Process
1. **Frontend build**: Node.js/npm builds React app to `build/` directory
2. **Resource copy**: Maven copies React build to Spring Boot `static/` resources
3. **Spring Boot package**: Creates executable JAR with embedded React frontend

### 8. Deployment
- **Port**: 8087
- **Docker**: Dockerfile available for containerization
- **Dependencies**: Requires API Gateway (port 8080) and Eureka (port 8761)

## Removed Components
To ensure proper separation of concerns and API Gateway-only communication, the following were removed:
- Entity classes (`Load.java`)
- Repository interfaces (`LoadRepository.java`)
- JPA configuration (`JpaConfig.java`)
- Database dependencies (H2, JPA)
- MapStruct mapping
- OpenAPI documentation (not needed for UI-only module)

## Testing
The module includes:
- Unit tests for services and controllers
- Integration tests for security configuration
- Frontend tests for React components

This architecture ensures the Web UI follows microservices best practices by communicating only through the API Gateway, maintaining loose coupling and proper separation of concerns.
