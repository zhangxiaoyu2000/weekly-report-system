// Set up test scenario by creating reports with different statuses
const http = require('http');

function makeRequest(options, data) {
    return new Promise((resolve, reject) => {
        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => {
                try {
                    const response = {
                        status: res.statusCode,
                        data: JSON.parse(body)
                    };
                    resolve(response);
                } catch (e) {
                    resolve({
                        status: res.statusCode,
                        data: body
                    });
                }
            });
        });
        
        req.on('error', reject);
        
        if (data) {
            req.write(JSON.stringify(data));
        }
        req.end();
    });
}

async function setupTestScenario() {
    console.log('🛠️ Setting up test scenario for force submit functionality...');
    
    try {
        // Login
        const loginOptions = {
            hostname: 'localhost',
            port: 8081,
            path: '/api/auth/login',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        };
        
        const loginResponse = await makeRequest(loginOptions, {
            usernameOrEmail: 'admin',
            password: 'admin123'
        });
        
        const token = loginResponse.data.data.accessToken;
        const headers = {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        };
        
        console.log('✅ Logged in successfully');
        
        // Create test reports with different titles for testing
        const testReports = [
            {
                title: '测试报告 - AI拒绝状态 (用于测试强行提交)',
                reportWeek: '2025年第3周',
                additionalNotes: '这是一个用于测试强行提交功能的AI拒绝状态报告',
                developmentOpportunities: '测试AI拒绝场景的发展机会',
            },
            {
                title: '测试报告 - 管理员拒绝状态 (用于测试修改按钮)',
                reportWeek: '2025年第3周',
                additionalNotes: '这是一个用于测试修改按钮功能的管理员拒绝状态报告',
                developmentOpportunities: '测试管理员拒绝场景的发展机会',
            }
        ];
        
        const createdReports = [];
        
        for (let i = 0; i < testReports.length; i++) {
            const reportData = testReports[i];
            
            const createOptions = {
                hostname: 'localhost',
                port: 8081,
                path: '/api/weekly-reports',
                method: 'POST',
                headers
            };
            
            const createResponse = await makeRequest(createOptions, {
                ...reportData,
                content: {
                    routine_tasks: [],
                    developmental_tasks: []
                }
            });
            
            if (createResponse.status === 200) {
                const report = createResponse.data.data;
                createdReports.push(report);
                console.log(`✅ Created report ${i + 1}: ID ${report.id} - ${report.title}`);
            }
        }
        
        console.log('\n📋 Test scenario setup complete!');
        console.log('\n🔧 Manual steps needed:');
        console.log('1. Use a database client to update the weekly_reports table:');
        
        if (createdReports.length >= 1) {
            console.log(`   UPDATE weekly_reports SET approval_status = 'AI_REJECTED' WHERE id = ${createdReports[0].id};`);
        }
        if (createdReports.length >= 2) {
            console.log(`   UPDATE weekly_reports SET approval_status = 'ADMIN_REJECTED' WHERE id = ${createdReports[1].id};`);
        }
        
        console.log('\n2. Then refresh the frontend at http://localhost:3008/app/reports');
        console.log('3. You should see:');
        console.log('   - 强行提交 button for AI_REJECTED status report');
        console.log('   - 修改 (edit) button for ADMIN_REJECTED status report');
        
        console.log('\n🧪 To test the force submit:');
        console.log('1. Click the 强行提交 button');
        console.log('2. Confirm the action');
        console.log('3. The status should change to ADMIN_REVIEWING');
        console.log('4. The button should disappear');
        
        // Save report IDs for reference
        console.log('\n📝 Report IDs created for testing:');
        createdReports.forEach((report, index) => {
            console.log(`Report ${index + 1}: ID ${report.id} - ${report.title}`);
        });
        
    } catch (error) {
        console.error('❌ Setup failed:', error.message);
    }
}

setupTestScenario();