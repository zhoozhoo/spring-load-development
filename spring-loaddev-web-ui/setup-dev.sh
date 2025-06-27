#!/bin/bash

# Spring Load Development Web UI - Development Setup Script

echo "🚀 Setting up Spring Load Development Web UI for development..."

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 18+ first."
    exit 1
fi

# Check Node.js version
NODE_VERSION=$(node -v | cut -d 'v' -f 2 | cut -d '.' -f 1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo "❌ Node.js version $NODE_VERSION is too old. Please install Node.js 18+ ."
    exit 1
fi

echo "✅ Node.js $(node -v) found"

# Navigate to frontend directory
cd "$(dirname "$0")/src/main/frontend" || exit 1

# Install dependencies
echo "📦 Installing frontend dependencies..."
npm install

# Copy environment file if it doesn't exist
if [ ! -f .env.local ]; then
    echo "🔧 Creating local environment file..."
    cp .env .env.local
    echo "📝 Edit .env.local to customize your local settings"
fi

# Create public directory if it doesn't exist
if [ ! -d public ]; then
    echo "📁 Creating public directory..."
    mkdir -p public
fi

# Create index.html if it doesn't exist
if [ ! -f public/index.html ]; then
    echo "📄 Creating index.html..."
    cat > public/index.html << 'EOF'
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <link rel="icon" href="%PUBLIC_URL%/favicon.ico" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <meta name="theme-color" content="#000000" />
    <meta name="description" content="Load Development Web Application" />
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>Load Development</title>
  </head>
  <body>
    <noscript>You need to enable JavaScript to run this app.</noscript>
    <div id="root"></div>
  </body>
</html>
EOF
fi

# Run linting
echo "🔍 Running linter..."
npm run lint

# Run tests
echo "🧪 Running tests..."
npm test -- --watchAll=false

echo ""
echo "✅ Development setup complete!"
echo ""
echo "To start development:"
echo "  cd src/main/frontend"
echo "  npm start"
echo ""
echo "To run tests:"
echo "  npm test"
echo ""
echo "To build for production:"
echo "  npm run build"
echo ""
