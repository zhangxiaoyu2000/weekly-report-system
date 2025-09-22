#!/usr/bin/env node

const http = require('http');

// Helper function to make HTTP requests
function makeRequest(options, data = null) {
    return new Promise((resolve, reject) => {
        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', (chunk) => {
                body += chunk;
            });
            res.on('end', () => {
                resolve({
                    statusCode: res.statusCode,
                    headers: res.headers,
                    body: body
                });
            });
        });

        req.on('error', (err) => {
            reject(err);
        });

        if (data) {
            req.write(data);
        }
        req.end();
    });
}

async function testInterface(name, options, data = null, expectedStatus = [200, 201]) {
    try {
        const result = await makeRequest(options, data);
        const success = expectedStatus.includes(result.statusCode);
        console.log(`${success ? '✅' : '❌'} ${name}: ${result.statusCode}`);
        
        if (!success && result.body) {
            try {
                const parsed = JSON.parse(result.body);
                console.log(`   Error: ${parsed.message || parsed.error || 'Unknown error'}`);
            } catch {
                const truncated = result.body.length > 100 ? 
                    result.body.substring(0, 100) + '...' : result.body;
                console.log(`   Error: ${truncated}`);
            }
        }
        
        return { success, status: result.statusCode, body: result.body };
    } catch (error) {
        console.log(`❌ ${name}: Connection Error - ${error.message}`);
        return { success: false, status: 0, body: error.message };
    }
}

async function comprehensiveTest() {
    console.log('=== Comprehensive Interface Testing Started ===\n');
    
    let successCount = 0;
    let totalCount = 0;
    let accessToken = null;

    // 1. Get access token
    console.log('1. Authentication Tests:');
    const loginResult = await testInterface(
        'Login', 
        {
            hostname: 'localhost',
            port: 8081,
            path: '/api/auth/login',
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        },
        JSON.stringify({ usernameOrEmail: 'admin', password: 'admin123' })
    );
    
    if (loginResult.success) {
        try {
            const loginData = JSON.parse(loginResult.body);
            accessToken = loginData.data.accessToken;
        } catch (e) {
            console.log('   Warning: Could not extract access token');
        }
    }
    
    totalCount++;
    if (loginResult.success) successCount++;

    // 2. Registration test
    const registerResult = await testInterface(
        'Registration',
        {
            hostname: 'localhost',
            port: 8081,
            path: '/api/auth/register',
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        },
        JSON.stringify({
            username: 'testuser' + Date.now(),
            email: 'test' + Date.now() + '@example.com',
            password: 'password123',
            confirmPassword: 'password123',
            role: 'MANAGER'
        }),
        [201]
    );
    totalCount++;
    if (registerResult.success) successCount++;

    // 3. Test logout (should require token)
    if (accessToken) {
        const logoutResult = await testInterface(
            'Logout',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/auth/logout',
                method: 'POST',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (logoutResult.success) successCount++;
    }

    console.log('\n2. User Management Tests:');
    if (accessToken) {
        // User profile
        const profileResult = await testInterface(
            'Get User Profile',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/users/profile',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (profileResult.success) successCount++;

        // User list
        const userListResult = await testInterface(
            'Get User List',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/users',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (userListResult.success) successCount++;

        // User search
        const userSearchResult = await testInterface(
            'Search Users',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/users/search?keyword=admin',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (userSearchResult.success) successCount++;
    }

    console.log('\n3. Task Management Tests:');
    if (accessToken) {
        // Task list
        const taskListResult = await testInterface(
            'Get Task List',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/tasks',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (taskListResult.success) successCount++;

        // Create task
        const createTaskResult = await testInterface(
            'Create Task',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/tasks',
                method: 'POST',
                headers: { 
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'
                }
            },
            JSON.stringify({
                taskName: 'Test Task',
                taskType: 'DEVELOPMENT',
                createdBy: 1
            }),
            [201]
        );
        totalCount++;
        if (createTaskResult.success) successCount++;

        // My tasks
        const myTasksResult = await testInterface(
            'Get My Tasks',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/tasks/my',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (myTasksResult.success) successCount++;
    }

    console.log('\n4. AI Service Tests:');
    if (accessToken) {
        // AI health
        const aiHealthResult = await testInterface(
            'AI Health Check',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/ai/health',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (aiHealthResult.success) successCount++;

        // AI metrics
        const aiMetricsResult = await testInterface(
            'AI Metrics',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/ai/metrics',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (aiMetricsResult.success) successCount++;

        // AI generate suggestions
        const aiSuggestionsResult = await testInterface(
            'AI Generate Suggestions',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/ai/generate-suggestions',
                method: 'POST',
                headers: { 
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'
                }
            },
            JSON.stringify({
                reportId: 1,
                analysisType: 'SUMMARY'
            })
        );
        totalCount++;
        if (aiSuggestionsResult.success) successCount++;
    }

    console.log('\n5. Project Management Tests:');
    if (accessToken) {
        // Project list
        const projectListResult = await testInterface(
            'Get Project List',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/projects',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (projectListResult.success) successCount++;

        // Create project
        const createProjectResult = await testInterface(
            'Create Project',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/projects',
                method: 'POST',
                headers: { 
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'
                }
            },
            JSON.stringify({
                name: 'Test Project',
                description: 'Test Description',
                priority: 'HIGH'
            }),
            [201]
        );
        totalCount++;
        if (createProjectResult.success) successCount++;
    }

    console.log('\n6. Weekly Report Tests:');
    if (accessToken) {
        // My weekly reports
        const myReportsResult = await testInterface(
            'Get My Weekly Reports',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/weekly-reports/my',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (myReportsResult.success) successCount++;

        // Pending reports
        const pendingReportsResult = await testInterface(
            'Get Pending Reports',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/weekly-reports/pending',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (pendingReportsResult.success) successCount++;

        // Create weekly report
        const createReportResult = await testInterface(
            'Create Weekly Report',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/weekly-reports',
                method: 'POST',
                headers: { 
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'
                }
            },
            JSON.stringify({
                title: 'Test Weekly Report',
                startDate: '2025-09-16',
                endDate: '2025-09-20',
                content: {
                    thisWeekSummary: 'Test summary',
                    nextWeekPlan: 'Test plan'
                }
            }),
            [201]
        );
        totalCount++;
        if (createReportResult.success) successCount++;
    }

    console.log('\n7. Health Check Tests:');
    // Basic health
    const healthResult = await testInterface(
        'Basic Health Check',
        {
            hostname: 'localhost',
            port: 8081,
            path: '/api/health',
            method: 'GET'
        }
    );
    totalCount++;
    if (healthResult.success) successCount++;

    // Authenticated health
    if (accessToken) {
        const authHealthResult = await testInterface(
            'Authenticated Health Check',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/health/authenticated',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (authHealthResult.success) successCount++;
    }

    console.log('\n8. 404 Error Handling Tests:');
    // Test 404 handling
    const notFoundResult = await testInterface(
        'Non-existent Endpoint',
        {
            hostname: 'localhost',
            port: 8081,
            path: '/api/nonexistent',
            method: 'GET'
        },
        null,
        [404]
    );
    totalCount++;
    if (notFoundResult.success) successCount++;

    // Summary
    console.log('\n=== TEST SUMMARY ===');
    console.log(`Total Tests: ${totalCount}`);
    console.log(`Successful: ${successCount}`);
    console.log(`Failed: ${totalCount - successCount}`);
    console.log(`Success Rate: ${((successCount / totalCount) * 100).toFixed(1)}%`);

    const improvementFromBefore = successCount / totalCount;
    const previousSuccessRate = 0.622; // 62.2% from before
    
    if (improvementFromBefore > previousSuccessRate) {
        console.log(`\n🎉 IMPROVEMENT: Success rate increased from ${(previousSuccessRate * 100).toFixed(1)}% to ${(improvementFromBefore * 100).toFixed(1)}%`);
        console.log(`Fixed ${Math.round((improvementFromBefore - previousSuccessRate) * totalCount)} additional interfaces!`);
    }

    console.log('\n=== Comprehensive Testing Complete ===');
}

comprehensiveTest();