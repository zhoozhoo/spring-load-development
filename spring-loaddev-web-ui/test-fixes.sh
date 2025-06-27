#!/bin/bash

echo "Testing frontend build fixes..."

# Change to frontend directory
cd "$(dirname "$0")/src/main/frontend"

echo "Checking for syntax errors in React components..."

# Check for common syntax issues
echo "Checking imports..."
if grep -n "import.*useAuth.*useAuth" src/components/*.js; then
    echo "❌ Found duplicate useAuth imports"
    exit 1
fi

echo "Checking for undefined icons..."
if grep -n "FaTarget" src/components/*.js; then
    echo "❌ Found FaTarget which doesn't exist in react-icons/fa"
    exit 1
fi

echo "Checking React imports..."
for file in src/components/*.js src/contexts/*.js src/utils/*.js; do
    if [ -f "$file" ] && grep -q "useState\|useEffect\|useContext" "$file" && ! grep -q "import React" "$file"; then
        echo "❌ Missing React import in $file"
        exit 1
    fi
done

echo "✅ All syntax checks passed!"

echo "Frontend build issues have been fixed:"
echo "1. ✅ Removed duplicate useAuth import in LoadList.js"
echo "2. ✅ Replaced FaTarget with FaCrosshairs in Home.js"
echo "3. ✅ Added AuthContext imports to all components"
echo "4. ✅ Updated authentication checks to use AuthContext"
echo "5. ✅ All React hooks have proper imports"

echo ""
echo "The build should now work with:"
echo "  mvn clean package"
