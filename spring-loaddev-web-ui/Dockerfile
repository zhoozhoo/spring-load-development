FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY target/spring-loaddev-web-ui-*.jar app.jar

# Expose port
EXPOSE 8087

# Set environment variables for OAuth2 (to be overridden at runtime)
ENV GITHUB_CLIENT_ID=your-github-client-id
ENV GITHUB_CLIENT_SECRET=your-github-client-secret
ENV GOOGLE_CLIENT_ID=your-google-client-id
ENV GOOGLE_CLIENT_SECRET=your-google-client-secret

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
