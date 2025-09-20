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
