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

### Quick Setup

Run the development setup script for automated configuration:

```bash
./setup-dev.sh
```

### Manual Setup

#### Prerequisites

- **Java 21 or later** - for backend Spring Boot application
- **Node.js 18+ and npm** - for frontend React application  
- **Maven 3.8+** - for building the project
- **Dependencies**: 
  - `spring-loaddev-config-server` running on port 8888
  - `spring-loaddev-api-gateway` running on port 8080 (or configured URL)

#### Installation Steps

1. **Clone and navigate to the project**:
   ```bash
   cd spring-loaddev-web-ui
   ```

2. **Install frontend dependencies**:
   ```bash
   cd src/main/frontend
   npm install
   ```

3. **Configure environment variables**:
   ```bash
   cp .env .env.local
   # Edit .env.local with your settings
   ```

4. **Run tests**:
   ```bash
   npm test
   ```

5. **Start development server**:
   ```bash
   npm start
   ```
   Frontend will be available at http://localhost:3000

6. **Build and run backend** (in separate terminal):
   ```bash
   cd ../../..  # Back to project root
   mvn spring-boot:run
   ```
   Backend will be available at http://localhost:8081

### Development Commands

| Command | Description |
|---------|-------------|
| `npm start` | Start development server with hot reload |
| `npm test` | Run tests in watch mode |
| `npm run test:coverage` | Run tests with coverage report |
| `npm run build` | Build production bundle |
| `npm run lint` | Run ESLint |
| `npm run lint:fix` | Fix ESLint issues automatically |

### Project Structure

```
spring-loaddev-web-ui/
├── src/main/
│   ├── frontend/               # React frontend
│   │   ├── src/
│   │   │   ├── components/     # React components
│   │   │   ├── contexts/       # React contexts (Auth, etc.)
│   │   │   ├── services/       # API services
│   │   │   ├── utils/          # Utility functions
│   │   │   └── __tests__/      # Test files
│   │   ├── public/             # Static assets
│   │   ├── .env                # Environment variables
│   │   └── package.json        # Frontend dependencies
│   └── java/                   # Spring Boot backend
│       └── ca/zhoozhoo/load_development/webui/
├── setup-dev.sh               # Development setup script
└── README.md
```
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

### Environment Variables

The application uses environment variables for configuration:

| Variable | Description | Default |
|----------|-------------|---------|
| `REACT_APP_API_GATEWAY_URL` | API Gateway endpoint | `http://localhost:8080` |
| `REACT_APP_NAME` | Application name | `Load Development` |
| `REACT_APP_ENABLE_ANALYTICS` | Enable analytics features | `false` |

### Security Configuration

The application implements several security measures:

- **CSRF Protection**: Automatic CSRF token handling
- **XSS Prevention**: Input sanitization and validation
- **Authentication**: OAuth2 integration with Keycloak
- **Authorization**: Role-based access control

### API Gateway Integration

All API calls go through the API Gateway. Ensure your gateway is configured with:

- **CORS enabled** for frontend origin
- **OAuth2 token relay** for authentication
- **Load balancing** to backend services
- **Circuit breaker** for resilience

### Testing

#### Running Tests

```bash
# Run all tests
npm test

# Run tests with coverage
npm run test:coverage

# Run tests in CI mode (single run)
npm test -- --watchAll=false
```

#### Test Structure

- **Unit Tests**: Component and utility function tests
- **Integration Tests**: API service tests
- **Accessibility Tests**: ARIA and keyboard navigation tests

### Production Deployment

#### Building for Production

```bash
# Build optimized production bundle
npm run build

# Build Docker image
docker build -t spring-loaddev-web-ui .
```

#### Docker Deployment

```bash
# Run with Docker
docker run -p 8081:8081 \
  -e REACT_APP_API_GATEWAY_URL=https://api.your-domain.com \
  spring-loaddev-web-ui
```

### Troubleshooting

#### Common Issues

**Frontend not connecting to backend:**
- Check API Gateway URL in `.env.local`
- Verify CORS configuration on API Gateway
- Ensure authentication tokens are valid

**Build failures:**
- Clear node_modules: `rm -rf node_modules package-lock.json && npm install`
- Check Node.js version: `node -v` (requires 18+)
- Verify all dependencies are installed

**Authentication issues:**
- Check Keycloak configuration
- Verify OAuth2 client settings
- Clear browser cookies and local storage

**Test failures:**
- Update test snapshots: `npm test -- --updateSnapshot`
- Check Jest configuration in `jest.config.js`
- Verify test environment setup in `setupTests.js`

#### Development Tips

- **Hot Reload**: Changes to React components reload automatically
- **API Proxy**: Development server proxies `/api` calls to backend
- **Source Maps**: Available in development for debugging
- **ESLint**: Run `npm run lint:fix` to auto-fix formatting issues

#### Performance Optimization

- **Code Splitting**: Implement lazy loading for routes
- **Bundle Analysis**: Use `npm run build` and analyze bundle size
- **Caching**: Configure proper cache headers for static assets
- **CDN**: Consider serving static assets from CDN

### Security Considerations

#### Development Security

- **Never commit** sensitive data to version control
- **Use environment variables** for all configuration
- **Keep dependencies updated** with `npm audit`
- **Follow OWASP guidelines** for web security

#### Production Security

- **HTTPS only** - never serve over HTTP in production
- **Secure headers** - implement CSP, HSTS, etc.
- **Regular updates** - keep all dependencies current
- **Security scanning** - use tools like Snyk or OWASP ZAP

### Contributing

#### Code Standards

- **ES6+** JavaScript with modern React patterns
- **TypeScript** support for type safety
- **ESLint** for code quality
- **Prettier** for consistent formatting
- **React Testing Library** for component testing

#### Pull Request Process

1. Fork the repository
2. Create a feature branch
3. Write tests for new functionality
4. Ensure all tests pass
5. Update documentation
6. Submit pull request with clear description

### License

This project is licensed under the MIT License - see the LICENSE file for details.
