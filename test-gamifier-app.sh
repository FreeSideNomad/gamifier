#!/bin/bash

# =============================================================================
# Starfleet Gamifier Application Testing Script
# =============================================================================
# This script tests the complete Starfleet Gamifier application including
# frontend Angular components and backend Spring Boot API endpoints.
#
# Prerequisites:
# - Angular CLI installed (npm install -g @angular/cli)
# - Node.js and npm installed
# - Java 17+ and Maven installed
# - MongoDB running (or MongoDB Atlas connection)
# - Chrome/Chromium browser with DevTools support
#
# Usage:
#   chmod +x test-gamifier-app.sh
#   ./test-gamifier-app.sh
# =============================================================================

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
FRONTEND_URL="http://localhost:4200"
BACKEND_URL="http://localhost:9080"
FRONTEND_DIR="frontend/gamifier-ui"
BACKEND_PORT=9080
FRONTEND_PORT=4200

# Logging
LOG_FILE="test-results-$(date +%Y%m%d-%H%M%S).log"
exec > >(tee -a "$LOG_FILE")
exec 2>&1

echo -e "${CYAN}==============================================================================${NC}"
echo -e "${CYAN}🚀 STARFLEET GAMIFIER APPLICATION TESTING SCRIPT${NC}"
echo -e "${CYAN}==============================================================================${NC}"
echo "Test started at: $(date)"
echo "Logging to: $LOG_FILE"
echo ""

# =============================================================================
# Helper Functions
# =============================================================================

print_section() {
    echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}🔍 $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

print_test() {
    echo -e "${PURPLE}📋 Testing: $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ PASS: $1${NC}"
}

print_failure() {
    echo -e "${RED}❌ FAIL: $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  WARN: $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ️  INFO: $1${NC}"
}

check_url_accessible() {
    local url=$1
    local description=$2
    local timeout=${3:-10}

    print_test "$description"

    if curl -s --connect-timeout $timeout --max-time $timeout "$url" > /dev/null 2>&1; then
        print_success "$description is accessible"
        return 0
    else
        print_failure "$description is not accessible at $url"
        return 1
    fi
}

check_api_endpoint() {
    local endpoint=$1
    local description=$2
    local expected_status=${3:-200}

    print_test "$description"

    local response=$(curl -s -w "%{http_code}" -o /tmp/api_response.json "$BACKEND_URL$endpoint" 2>/dev/null)
    local status_code=$(echo "$response" | tail -n1)

    if [ "$status_code" = "$expected_status" ]; then
        print_success "$description (HTTP $status_code)"
        if [ -f /tmp/api_response.json ]; then
            echo "Response preview: $(head -c 200 /tmp/api_response.json)..."
        fi
        return 0
    else
        print_failure "$description (Expected HTTP $expected_status, got HTTP $status_code)"
        return 1
    fi
}

check_process_running() {
    local process_name=$1
    local port=$2

    if lsof -i :$port > /dev/null 2>&1; then
        print_success "$process_name is running on port $port"
        return 0
    else
        print_failure "$process_name is not running on port $port"
        return 1
    fi
}

# =============================================================================
# Test Results Tracking
# =============================================================================

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

record_test_result() {
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    if [ $1 -eq 0 ]; then
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

# =============================================================================
# Pre-flight Checks
# =============================================================================

print_section "PRE-FLIGHT SYSTEM CHECKS"

# Check required tools
print_test "Checking required tools"
missing_tools=()

if ! command -v node &> /dev/null; then
    missing_tools+=("node")
fi

if ! command -v npm &> /dev/null; then
    missing_tools+=("npm")
fi

if ! command -v ng &> /dev/null; then
    missing_tools+=("@angular/cli")
fi

if ! command -v java &> /dev/null; then
    missing_tools+=("java")
fi

if ! command -v mvn &> /dev/null; then
    missing_tools+=("maven")
fi

if ! command -v curl &> /dev/null; then
    missing_tools+=("curl")
fi

if [ ${#missing_tools[@]} -eq 0 ]; then
    print_success "All required tools are installed"
    record_test_result 0
else
    print_failure "Missing tools: ${missing_tools[*]}"
    print_info "Please install missing tools and try again"
    record_test_result 1
fi

# Check versions
print_info "Node.js version: $(node --version)"
print_info "npm version: $(npm --version)"
print_info "Angular CLI version: $(ng version --version 2>/dev/null || echo 'Not available')"
print_info "Java version: $(java -version 2>&1 | head -n1)"
print_info "Maven version: $(mvn --version 2>/dev/null | head -n1)"

# =============================================================================
# Application Status Checks
# =============================================================================

print_section "APPLICATION STATUS CHECKS"

# Check if processes are running
check_process_running "Frontend (Angular)" $FRONTEND_PORT
record_test_result $?

check_process_running "Backend (Spring Boot)" $BACKEND_PORT
record_test_result $?

# Check application accessibility
check_url_accessible "$FRONTEND_URL" "Frontend application"
record_test_result $?

check_url_accessible "$BACKEND_URL/actuator/health" "Backend health endpoint"
record_test_result $?

# =============================================================================
# Backend API Testing
# =============================================================================

print_section "BACKEND API ENDPOINT TESTING"

# Test core API endpoints
check_api_endpoint "/api/users" "Users API endpoint"
record_test_result $?

check_api_endpoint "/api/users/current" "Current user endpoint" 404
record_test_result $?

check_api_endpoint "/api/actions" "Actions API endpoint"
record_test_result $?

check_api_endpoint "/api/organization" "Organization API endpoint"
record_test_result $?

check_api_endpoint "/api/leaderboard" "Leaderboard API endpoint"
record_test_result $?

# Test actuator endpoints
check_api_endpoint "/actuator/health" "Health check endpoint"
record_test_result $?

check_api_endpoint "/actuator/info" "Info endpoint"
record_test_result $?

# =============================================================================
# Frontend Component Testing
# =============================================================================

print_section "FRONTEND COMPONENT TESTING"

# Test if we can reach different routes (basic connectivity test)
frontend_routes=(
    ""                      # Dashboard
    "/actions"             # Actions
    "/missions"            # Missions
    "/leaderboards"        # Leaderboards
    "/admin/organization"  # Admin Organization
    "/admin/users"         # Admin Users
    "/admin/reports"       # Admin Reports
)

for route in "${frontend_routes[@]}"; do
    route_name=$(echo "$route" | sed 's|^/||' | sed 's|/| |g' | tr '[:lower:]' '[:upper:]')
    if [ -z "$route_name" ]; then
        route_name="DASHBOARD"
    fi

    check_url_accessible "${FRONTEND_URL}${route}" "Frontend route: $route_name"
    record_test_result $?
done

# =============================================================================
# Integration Testing
# =============================================================================

print_section "INTEGRATION TESTING"

print_test "Testing API integration with CORS"
if curl -s -H "Origin: $FRONTEND_URL" \
        -H "Access-Control-Request-Method: GET" \
        -H "Access-Control-Request-Headers: Content-Type" \
        -X OPTIONS "$BACKEND_URL/api/users" | grep -q "Access-Control-Allow-Origin"; then
    print_success "CORS is properly configured"
    record_test_result 0
else
    print_warning "CORS configuration may need attention"
    record_test_result 1
fi

print_test "Testing MongoDB connection through API"
response=$(curl -s "$BACKEND_URL/api/organization")
if echo "$response" | grep -q "id\|name\|federationId"; then
    print_success "MongoDB connection is working (organization data found)"
    record_test_result 0
else
    print_failure "MongoDB connection may be failing (no organization data)"
    record_test_result 1
fi

# =============================================================================
# Security Testing
# =============================================================================

print_section "BASIC SECURITY TESTING"

print_test "Testing for sensitive information exposure"
sensitive_endpoints=(
    "/actuator/env"
    "/actuator/configprops"
    "/actuator/beans"
)

for endpoint in "${sensitive_endpoints[@]}"; do
    response_code=$(curl -s -w "%{http_code}" -o /dev/null "$BACKEND_URL$endpoint")
    if [ "$response_code" = "404" ] || [ "$response_code" = "401" ] || [ "$response_code" = "403" ]; then
        print_success "Sensitive endpoint $endpoint is protected (HTTP $response_code)"
        record_test_result 0
    else
        print_warning "Sensitive endpoint $endpoint may be exposed (HTTP $response_code)"
        record_test_result 1
    fi
done

# =============================================================================
# Performance Testing
# =============================================================================

print_section "BASIC PERFORMANCE TESTING"

print_test "Testing frontend load time"
start_time=$(date +%s%N)
curl -s "$FRONTEND_URL" > /dev/null
end_time=$(date +%s%N)
load_time=$(( (end_time - start_time) / 1000000 ))

if [ $load_time -lt 2000 ]; then
    print_success "Frontend loads quickly (${load_time}ms)"
    record_test_result 0
elif [ $load_time -lt 5000 ]; then
    print_warning "Frontend load time is acceptable (${load_time}ms)"
    record_test_result 0
else
    print_failure "Frontend load time is slow (${load_time}ms)"
    record_test_result 1
fi

print_test "Testing API response time"
start_time=$(date +%s%N)
curl -s "$BACKEND_URL/api/users" > /dev/null
end_time=$(date +%s%N)
api_time=$(( (end_time - start_time) / 1000000 ))

if [ $api_time -lt 1000 ]; then
    print_success "API responds quickly (${api_time}ms)"
    record_test_result 0
elif [ $api_time -lt 3000 ]; then
    print_warning "API response time is acceptable (${api_time}ms)"
    record_test_result 0
else
    print_failure "API response time is slow (${api_time}ms)"
    record_test_result 1
fi

# =============================================================================
# Browser Testing Commands
# =============================================================================

print_section "BROWSER TESTING COMMANDS"

print_info "To test the application manually in your browser:"
echo ""
echo -e "${CYAN}Frontend URLs to test:${NC}"
echo "  🏠 Dashboard:           $FRONTEND_URL"
echo "  ⚡ Actions:             $FRONTEND_URL/actions"
echo "  🎯 Missions:            $FRONTEND_URL/missions"
echo "  🏆 Leaderboards:        $FRONTEND_URL/leaderboards"
echo "  🔧 Admin Organization:  $FRONTEND_URL/admin/organization"
echo "  👥 Admin Users:         $FRONTEND_URL/admin/users"
echo "  📊 Admin Reports:       $FRONTEND_URL/admin/reports"
echo ""
echo -e "${CYAN}Backend API URLs to test:${NC}"
echo "  📋 Users API:           $BACKEND_URL/api/users"
echo "  ⚡ Actions API:         $BACKEND_URL/api/actions"
echo "  🏢 Organization API:    $BACKEND_URL/api/organization"
echo "  🏆 Leaderboard API:     $BACKEND_URL/api/leaderboard"
echo "  💊 Health Check:       $BACKEND_URL/actuator/health"
echo ""

# =============================================================================
# Browser Console Error Check Script
# =============================================================================

cat > browser-console-check.js << 'EOF'
// Starfleet Gamifier Browser Console Error Check
// Run this script in your browser's DevTools console to check for errors

console.log('🚀 Starting Starfleet Gamifier Browser Console Check...');

// Store original console methods
const originalError = console.error;
const originalWarn = console.warn;

// Track errors and warnings
let errors = [];
let warnings = [];

// Override console methods to capture logs
console.error = function(...args) {
    errors.push({
        timestamp: new Date().toISOString(),
        message: args.join(' '),
        stack: new Error().stack
    });
    originalError.apply(console, args);
};

console.warn = function(...args) {
    warnings.push({
        timestamp: new Date().toISOString(),
        message: args.join(' ')
    });
    originalWarn.apply(console, args);
};

// Test navigation to all routes
const routes = [
    '',
    '/actions',
    '/missions',
    '/leaderboards',
    '/admin/organization',
    '/admin/users',
    '/admin/reports'
];

let currentRouteIndex = 0;

function testNextRoute() {
    if (currentRouteIndex >= routes.length) {
        showResults();
        return;
    }

    const route = routes[currentRouteIndex];
    console.log(`🔍 Testing route: ${route || '/dashboard'}`);

    // Navigate to route
    window.location.hash = route;

    setTimeout(() => {
        currentRouteIndex++;
        testNextRoute();
    }, 2000); // Wait 2 seconds between route changes
}

function showResults() {
    console.log('\n📊 BROWSER CONSOLE TEST RESULTS');
    console.log('================================');

    if (errors.length === 0) {
        console.log('✅ No console errors detected!');
    } else {
        console.log(`❌ Found ${errors.length} console errors:`);
        errors.forEach((error, index) => {
            console.log(`  ${index + 1}. [${error.timestamp}] ${error.message}`);
        });
    }

    if (warnings.length === 0) {
        console.log('✅ No console warnings detected!');
    } else {
        console.log(`⚠️ Found ${warnings.length} console warnings:`);
        warnings.forEach((warning, index) => {
            console.log(`  ${index + 1}. [${warning.timestamp}] ${warning.message}`);
        });
    }

    // Restore original console methods
    console.error = originalError;
    console.warn = originalWarn;

    console.log('\n🏁 Browser console check completed!');
}

// Start the test
console.log('Starting route navigation test in 3 seconds...');
setTimeout(testNextRoute, 3000);
EOF

print_info "Browser console error checking script created: browser-console-check.js"
print_info "To use it:"
echo "  1. Open your browser and navigate to $FRONTEND_URL"
echo "  2. Open DevTools (F12)"
echo "  3. Go to the Console tab"
echo "  4. Copy and paste the contents of browser-console-check.js"
echo "  5. Press Enter to run the automated console error check"

# =============================================================================
# Application Startup Script
# =============================================================================

cat > start-gamifier-dev.sh << 'EOF'
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
EOF

chmod +x start-gamifier-dev.sh
print_info "Development startup script created: start-gamifier-dev.sh"

# =============================================================================
# Test Summary
# =============================================================================

print_section "TEST SUMMARY"

echo ""
echo -e "${CYAN}📊 STARFLEET GAMIFIER TEST RESULTS${NC}"
echo -e "${CYAN}=================================${NC}"
echo -e "Total Tests Run:    ${BLUE}$TOTAL_TESTS${NC}"
echo -e "Tests Passed:       ${GREEN}$PASSED_TESTS${NC}"
echo -e "Tests Failed:       ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n${GREEN}🎉 ALL TESTS PASSED! The Starfleet Gamifier is ready for action!${NC}"
    echo -e "${GREEN}🖖 Live long and prosper, Captain!${NC}"
    exit_code=0
elif [ $FAILED_TESTS -le 2 ]; then
    echo -e "\n${YELLOW}⚠️  Most tests passed with minor issues. Review failures above.${NC}"
    exit_code=1
else
    echo -e "\n${RED}❌ Multiple test failures detected. Please review and fix issues.${NC}"
    exit_code=2
fi

echo ""
echo -e "${CYAN}📋 NEXT STEPS:${NC}"
echo "1. Review any failed tests above"
echo "2. Open $FRONTEND_URL in your browser"
echo "3. Test each component manually"
echo "4. Run the browser console check script"
echo "5. Monitor application logs for any errors"
echo ""
echo "📂 Test log saved to: $LOG_FILE"
echo "🕐 Test completed at: $(date)"

# =============================================================================
# Cleanup
# =============================================================================

# Clean up temporary files
rm -f /tmp/api_response.json

exit $exit_code
EOF

<parameter name="record_test_result 1
fi
done

print_test "Testing MongoDB connection through API"
response=$(curl -s "$BACKEND_URL/api/organization")
if echo "$response" | grep -q "id\|name\|federationId"; then
    print_success "MongoDB connection is working (organization data found)"
    record_test_result 0
else
    print_failure "MongoDB connection may be failing (no organization data)"
    record_test_result 1
fi

# =============================================================================
# Security Testing
# =============================================================================

print_section "BASIC SECURITY TESTING"

print_test "Testing for sensitive information exposure"
sensitive_endpoints=(
    "/actuator/env"
    "/actuator/configprops"
    "/actuator/beans"
)

for endpoint in "${sensitive_endpoints[@]}"; do
    response_code=$(curl -s -w "%{http_code}" -o /dev/null "$BACKEND_URL$endpoint")
    if [ "$response_code" = "404" ] || [ "$response_code" = "401" ] || [ "$response_code" = "403" ]; then
        print_success "Sensitive endpoint $endpoint is protected (HTTP $response_code)"
        record_test_result 0
    else
        print_warning "Sensitive endpoint $endpoint may be exposed (HTTP $response_code)"
        record_test_result 1
    fi
done

# =============================================================================
# Performance Testing
# =============================================================================

print_section "BASIC PERFORMANCE TESTING"

print_test "Testing frontend load time"
start_time=$(date +%s%N)
curl -s "$FRONTEND_URL" > /dev/null
end_time=$(date +%s%N)
load_time=$(( (end_time - start_time) / 1000000 ))

if [ $load_time -lt 2000 ]; then
    print_success "Frontend loads quickly (${load_time}ms)"
    record_test_result 0
elif [ $load_time -lt 5000 ]; then
    print_warning "Frontend load time is acceptable (${load_time}ms)"
    record_test_result 0
else
    print_failure "Frontend load time is slow (${load_time}ms)"
    record_test_result 1
fi

print_test "Testing API response time"
start_time=$(date +%s%N)
curl -s "$BACKEND_URL/api/users" > /dev/null
end_time=$(date +%s%N)
api_time=$(( (end_time - start_time) / 1000000 ))

if [ $api_time -lt 1000 ]; then
    print_success "API responds quickly (${api_time}ms)"
    record_test_result 0
elif [ $api_time -lt 3000 ]; then
    print_warning "API response time is acceptable (${api_time}ms)"
    record_test_result 0
else
    print_failure "API response time is slow (${api_time}ms)"
    record_test_result 1
fi

# =============================================================================
# Browser Testing Commands
# =============================================================================

print_section "BROWSER TESTING COMMANDS"

print_info "To test the application manually in your browser:"
echo ""
echo -e "${CYAN}Frontend URLs to test:${NC}"
echo "  🏠 Dashboard:           $FRONTEND_URL"
echo "  ⚡ Actions:             $FRONTEND_URL/actions"
echo "  🎯 Missions:            $FRONTEND_URL/missions"
echo "  🏆 Leaderboards:        $FRONTEND_URL/leaderboards"
echo "  🔧 Admin Organization:  $FRONTEND_URL/admin/organization"
echo "  👥 Admin Users:         $FRONTEND_URL/admin/users"
echo "  📊 Admin Reports:       $FRONTEND_URL/admin/reports"
echo ""
echo -e "${CYAN}Backend API URLs to test:${NC}"
echo "  📋 Users API:           $BACKEND_URL/api/users"
echo "  ⚡ Actions API:         $BACKEND_URL/api/actions"
echo "  🏢 Organization API:    $BACKEND_URL/api/organization"
echo "  🏆 Leaderboard API:     $BACKEND_URL/api/leaderboard"
echo "  💊 Health Check:       $BACKEND_URL/actuator/health"
echo ""

# =============================================================================
# Browser Console Error Check Script
# =============================================================================

cat > browser-console-check.js << 'EOF'
// Starfleet Gamifier Browser Console Error Check
// Run this script in your browser's DevTools console to check for errors

console.log('🚀 Starting Starfleet Gamifier Browser Console Check...');

// Store original console methods
const originalError = console.error;
const originalWarn = console.warn;

// Track errors and warnings
let errors = [];
let warnings = [];

// Override console methods to capture logs
console.error = function(...args) {
    errors.push({
        timestamp: new Date().toISOString(),
        message: args.join(' '),
        stack: new Error().stack
    });
    originalError.apply(console, args);
};

console.warn = function(...args) {
    warnings.push({
        timestamp: new Date().toISOString(),
        message: args.join(' ')
    });
    originalWarn.apply(console, args);
};

// Test navigation to all routes
const routes = [
    '',
    '/actions',
    '/missions',
    '/leaderboards',
    '/admin/organization',
    '/admin/users',
    '/admin/reports'
];

let currentRouteIndex = 0;

function testNextRoute() {
    if (currentRouteIndex >= routes.length) {
        showResults();
        return;
    }

    const route = routes[currentRouteIndex];
    console.log(`🔍 Testing route: ${route || '/dashboard'}`);

    // Navigate to route
    window.location.hash = route;

    setTimeout(() => {
        currentRouteIndex++;
        testNextRoute();
    }, 2000); // Wait 2 seconds between route changes
}

function showResults() {
    console.log('\n📊 BROWSER CONSOLE TEST RESULTS');
    console.log('================================');

    if (errors.length === 0) {
        console.log('✅ No console errors detected!');
    } else {
        console.log(`❌ Found ${errors.length} console errors:`);
        errors.forEach((error, index) => {
            console.log(`  ${index + 1}. [${error.timestamp}] ${error.message}`);
        });
    }

    if (warnings.length === 0) {
        console.log('✅ No console warnings detected!');
    } else {
        console.log(`⚠️ Found ${warnings.length} console warnings:`);
        warnings.forEach((warning, index) => {
            console.log(`  ${index + 1}. [${warning.timestamp}] ${warning.message}`);
        });
    }

    // Restore original console methods
    console.error = originalError;
    console.warn = originalWarn;

    console.log('\n🏁 Browser console check completed!');
}

// Start the test
console.log('Starting route navigation test in 3 seconds...');
setTimeout(testNextRoute, 3000);
EOF

print_info "Browser console error checking script created: browser-console-check.js"
print_info "To use it:"
echo "  1. Open your browser and navigate to $FRONTEND_URL"
echo "  2. Open DevTools (F12)"
echo "  3. Go to the Console tab"
echo "  4. Copy and paste the contents of browser-console-check.js"
echo "  5. Press Enter to run the automated console error check"

# =============================================================================
# Application Startup Script
# =============================================================================

cat > start-gamifier-dev.sh << 'EOF'
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
EOF

chmod +x start-gamifier-dev.sh
print_info "Development startup script created: start-gamifier-dev.sh"

# =============================================================================
# Test Summary
# =============================================================================

print_section "TEST SUMMARY"

echo ""
echo -e "${CYAN}📊 STARFLEET GAMIFIER TEST RESULTS${NC}"
echo -e "${CYAN}=================================${NC}"
echo -e "Total Tests Run:    ${BLUE}$TOTAL_TESTS${NC}"
echo -e "Tests Passed:       ${GREEN}$PASSED_TESTS${NC}"
echo -e "Tests Failed:       ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n${GREEN}🎉 ALL TESTS PASSED! The Starfleet Gamifier is ready for action!${NC}"
    echo -e "${GREEN}🖖 Live long and prosper, Captain!${NC}"
    exit_code=0
elif [ $FAILED_TESTS -le 2 ]; then
    echo -e "\n${YELLOW}⚠️  Most tests passed with minor issues. Review failures above.${NC}"
    exit_code=1
else
    echo -e "\n${RED}❌ Multiple test failures detected. Please review and fix issues.${NC}"
    exit_code=2
fi

echo ""
echo -e "${CYAN}📋 NEXT STEPS:${NC}"
echo "1. Review any failed tests above"
echo "2. Open $FRONTEND_URL in your browser"
echo "3. Test each component manually"
echo "4. Run the browser console check script"
echo "5. Monitor application logs for any errors"
echo ""
echo "📂 Test log saved to: $LOG_FILE"
echo "🕐 Test completed at: $(date)"

# =============================================================================
# Cleanup
# =============================================================================

# Clean up temporary files
rm -f /tmp/api_response.json

exit $exit_code