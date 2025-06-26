#!/bin/bash

echo "Building Spring Load Development Web UI..."

# Set colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if OAuth2 environment variables are set
check_oauth_vars() {
    print_status "Checking OAuth2 environment variables..."
    
    if [ -z "$GITHUB_CLIENT_ID" ] || [ "$GITHUB_CLIENT_ID" = "your-github-client-id" ]; then
        print_warning "GITHUB_CLIENT_ID not set or using default value"
    fi
    
    if [ -z "$GITHUB_CLIENT_SECRET" ] || [ "$GITHUB_CLIENT_SECRET" = "your-github-client-secret" ]; then
        print_warning "GITHUB_CLIENT_SECRET not set or using default value"
    fi
    
    if [ -z "$GOOGLE_CLIENT_ID" ] || [ "$GOOGLE_CLIENT_ID" = "your-google-client-id" ]; then
        print_warning "GOOGLE_CLIENT_ID not set or using default value"
    fi
    
    if [ -z "$GOOGLE_CLIENT_SECRET" ] || [ "$GOOGLE_CLIENT_SECRET" = "your-google-client-secret" ]; then
        print_warning "GOOGLE_CLIENT_SECRET not set or using default value"
    fi
}

# Clean previous builds
clean() {
    print_status "Cleaning previous builds..."
    mvn clean
}

# Run tests
test() {
    print_status "Running tests..."
    mvn test
    if [ $? -ne 0 ]; then
        print_error "Tests failed!"
        exit 1
    fi
}

# Build the application
build() {
    print_status "Building application (this may take a while for the first build)..."
    mvn package -DskipTests
    if [ $? -ne 0 ]; then
        print_error "Build failed!"
        exit 1
    fi
}

# Main execution
main() {
    cd "$(dirname "$0")"
    
    print_status "Starting build process for Spring Load Development Web UI"
    
    check_oauth_vars
    
    case "${1:-build}" in
        "clean")
            clean
            ;;
        "test")
            test
            ;;
        "build")
            clean
            test
            build
            print_status "Build completed successfully!"
            print_status "You can now run: java -jar target/spring-loaddev-web-ui-*.jar"
            ;;
        "package")
            clean
            build
            print_status "Package completed successfully!"
            ;;
        *)
            echo "Usage: $0 {clean|test|build|package}"
            echo "  clean   - Clean previous builds"
            echo "  test    - Run tests only"
            echo "  build   - Clean, test, and build (default)"
            echo "  package - Clean and build without tests"
            exit 1
            ;;
    esac
}

main "$@"
