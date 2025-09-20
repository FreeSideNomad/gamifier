#!/bin/bash

# Starfleet Gamifier Development Startup Script

echo "🚀 Starting Starfleet Gamifier Development Environment..."

# Function to check if port is in use
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
        echo "⚠️  Port $1 is already in use"
        return 1
    else
        echo "✅ Port $1 is available"
        return 0
    fi
}

# Check ports
echo "🔍 Checking required ports..."
check_port 4200 # Angular dev server
check_port 9080 # Spring Boot backend

echo ""
echo "📦 Starting backend (Spring Boot)..."
gnome-terminal --tab --title="Backend" -- bash -c "cd $(pwd) && mvn spring-boot:run; exec bash" 2>/dev/null || \
osascript -e 'tell app "Terminal" to do script "cd '$(pwd)' && mvn spring-boot:run"' 2>/dev/null || \
(echo "Starting backend in current terminal..." && mvn spring-boot:run &)

echo "⏳ Waiting for backend to start..."
sleep 10

echo "🎨 Starting frontend (Angular)..."
gnome-terminal --tab --title="Frontend" -- bash -c "cd $(pwd)/frontend/gamifier-ui && npm start; exec bash" 2>/dev/null || \
osascript -e 'tell app "Terminal" to do script "cd '$(pwd)/frontend/gamifier-ui' && npm start"' 2>/dev/null || \
(echo "Starting frontend in current terminal..." && cd frontend/gamifier-ui && npm start &)

echo ""
echo "🎉 Starfleet Gamifier is starting up!"
echo "📱 Frontend will be available at: http://localhost:4200"
echo "🔧 Backend will be available at: http://localhost:9080"
echo ""
echo "⏳ Please wait a few moments for both services to fully start..."
echo "🔍 Run ./test-gamifier-app.sh to verify everything is working"
