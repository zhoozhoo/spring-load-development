# Spring Load Development Web UI

A React-based web user interface for the Spring Load Development microservices project. This module provides a modern, responsive web application for managing ammunition load development data through the API Gateway with OAuth2 authentication.

## Architecture

This Web UI module follows microservices best practices:
- **API Gateway Communication Only**: All data operations go through `spring-loaddev-api-gateway`
- **No Direct Database Access**: The UI backend has no database dependencies
- **Stateless Authentication**: OAuth2 integration with token relay to API Gateway
- **Separation of Concerns**: Frontend handles UI, backend handles authentication and proxying

## Features

- **Authentication**: Secure OAuth2 authentication with Keycloak
- **CRUD Operations**: Create, Read, Update, and Delete load data via API Gateway
- **Search & Filter**: Advanced search and filtering capabilities
- **Responsive Design**: Modern, mobile-friendly UI using React Bootstrap
- **Real-time Feedback**: Toast notifications for user actions
- **Data Validation**: Client-side validation with backend validation via API Gateway
- **Pagination**: Efficient data loading with pagination support

## Technology Stack

### Backend (Authentication & Proxy Layer)
- Spring Boot 3.5.3
- Spring Security with OAuth2 (Keycloak integration)
- Spring WebFlux (WebClient for API Gateway communication)
- Spring Cloud Config (centralized configuration)
- Spring Cloud Eureka Client
- **No Database Dependencies** - Data comes from API Gateway

### Frontend
- React 18.2.0
- React Router 6.8.1
- React Bootstrap 2.7.2
- Bootstrap 5.2.3
- Axios for API calls (configured to use API Gateway)
- React Icons
- React Toastify for notifications

## Getting Started

### Prerequisites

- Java 21 or later
- Node.js 18+ and npm (for frontend development)
- Maven 3.8+
- **Dependencies**: 
  - `spring-loaddev-config-server` running on port 8888
  - `spring-loaddev-api-gateway` running on port 8080
  - `spring-loaddev-discovery-server` running on port 8761
  - **Keycloak server** running on port 7080 with `reloading` realm configured
  - Backend data services (loads-service, etc.) registered with Eureka

### Keycloak Setup

The application uses Keycloak for authentication. Ensure Keycloak is configured with:
- **Realm**: `reloading`
- **Client ID**: `reloading-client`
- **Client Secret**: `2EvQuluZfxaaRms8V4NhzBDWzVCSXtty`
- **Redirect URI**: `http://localhost:8087/login/oauth2/code/keycloak`
- **Scopes**: `openid`, `email`, `profile`, `roles`

### Environment Variables

The application uses Spring Cloud Config for centralized configuration. Set these environment variables if needed:

```bash
# Infrastructure URLs
CONFIG_SERVER_URL=http://localhost:8888  # Spring Cloud Config Server
API_GATEWAY_URL=http://localhost:8080    # API Gateway URL (for local override)
DISCOVERY_SERVER=http://localhost:8761/eureka  # Eureka Discovery Server
KEYCLOAK_BASE_URL=http://localhost:7080  # Keycloak server URL

# Profile Management
SPRING_PROFILES_ACTIVE=default|docker    # Application profile

# Server Configuration (optional overrides)
PORT=8087  # Server port, defaults to 8087
```

**Note**: Most configuration is managed centrally via Spring Cloud Config Server in the `spring-load-development-config` repository.

### Running the Application

#### Development Mode (Frontend + Backend separately)

1. **Start the Spring Boot backend:**
   ```bash
   cd spring-loaddev-web-ui
   mvn spring-boot:run
   ```

2. **Start the React frontend (in another terminal):**
   ```bash
   cd spring-loaddev-web-ui/src/main/frontend
   npm install
   npm start
   ```

   The React app will run on http://localhost:3000 and proxy API calls to the API Gateway on http://localhost:8080.

#### Code Quality & Linting

```bash
# Run ESLint to check code quality
npm run lint

# Auto-fix ESLint issues
npm run lint:fix

# Check formatting (if Prettier CLI is installed globally)
npx prettier --check src/

# Format code
npx prettier --write src/
```

#### Production Mode (Frontend built into backend)

```bash
cd spring-loaddev-web-ui
mvn clean package
java -jar target/spring-loaddev-web-ui-0.0.6-SNAPSHOT.jar
```

The application will be available at http://localhost:8087.

## Application Structure

### Backend Structure
```
src/main/java/ca/zhoozhoo/load_development/webui/
├── config/          # Security and JPA configuration
├── controller/      # REST controllers and web controllers
├── dto/            # Data Transfer Objects
├── entity/         # JPA entities
├── mapper/         # MapStruct mappers
├── repository/     # Spring Data repositories
└── service/        # Business logic services
```

### Frontend Structure
```
src/main/frontend/src/
├── components/     # React components
├── services/      # API service functions
├── App.js         # Main App component
└── index.js       # Application entry point
```

## API Documentation

Once the application is running, you can access:

- **Swagger UI**: http://localhost:8087/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8087/v3/api-docs
- **H2 Console**: http://localhost:8087/h2-console (development only)

## Key Features

### Authentication & Authorization
- OAuth2 integration with GitHub and Google
- Role-based access control (RBAC)
- Session management
- Secure API endpoints

### Load Management
- Create new ammunition load entries
- View detailed load information
- Edit existing loads
- Delete loads with confirmation
- Search and filter loads
- Pagination for large datasets

### Data Security
- User isolation (users can only see their own data)
- Input validation and sanitization
- CSRF protection
- Secure headers

### User Experience
- Responsive design for mobile and desktop
- Loading states and error handling
- Toast notifications for feedback
- Clean, intuitive interface
- Search functionality

## Configuration

### Database
The application uses H2 in-memory database by default for development. For production, configure a persistent database in `application.yml`.

### Security
OAuth2 providers can be configured in `application.yml`. Additional providers can be added by extending the security configuration.

### Frontend Build
The Maven build process automatically:
1. Installs Node.js and npm
2. Runs `npm install`
3. Builds the React application
4. Copies the build output to Spring Boot's static resources

## Development

### Adding New Features
1. **Backend**: Add new entities, repositories, services, and controllers as needed
2. **Frontend**: Create new React components and integrate with the backend APIs
3. **API Documentation**: Use OpenAPI annotations to document new endpoints

### Testing
- Backend tests: Use Spring Boot test framework
- Frontend tests: Use Jest and React Testing Library
- Integration tests: Test the full application stack

## Security Considerations

- Always use HTTPS in production
- Regularly update dependencies
- Store OAuth2 secrets securely
- Implement proper session timeout
- Use Content Security Policy headers
- Validate all user inputs

## Troubleshooting

### Common Issues

1. **OAuth2 authentication fails**: Check client IDs and secrets, ensure callback URLs are correct
2. **Frontend not loading**: Verify the React build completed successfully during Maven build
3. **API calls failing**: Check CORS configuration and ensure backend is running
4. **Database connection issues**: Verify H2 console access or database configuration

### Logs
Check application logs for detailed error information:
```bash
tail -f logs/spring-loaddev-web-ui.log
```

## Contributing

1. Follow existing code style and conventions
2. Add tests for new functionality
3. Update documentation as needed
4. Use meaningful commit messages

## License

This project is licensed under the same license as the parent Spring Load Development project.
