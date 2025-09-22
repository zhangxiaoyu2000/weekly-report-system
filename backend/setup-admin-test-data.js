// Create test data for admin approval testing
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

async function setupAdminTestData() {
    console.log('🛠️ Setting up test data for admin approval testing...');
    
    try {
        // Login as admin
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
        
        console.log('✅ Logged in as admin');
        
        // Create test reports for different scenarios
        const testReports = [
            {
                title: '测试报告 - 等待管理员审批 (AI已通过)',
                reportWeek: '2025年第4周',
                additionalNotes: '这是一个AI分析通过，等待管理员审批的测试周报',
                developmentOpportunities: '测试管理员审批功能',
                targetStatus: 'AI_APPROVED'
            },
            {
                title: '测试报告 - AI分析通过 (可审批)',
                reportWeek: '2025年第4周',
                additionalNotes: '这是另一个AI分析通过的测试周报，管理员可以对其进行审批操作',
                developmentOpportunities: '验证管理员界面功能',
                targetStatus: 'AI_APPROVED'
            },
            {
                title: '测试报告 - 管理员已通过',
                reportWeek: '2025年第4周',
                additionalNotes: '这是一个管理员已经审批通过的测试周报',
                developmentOpportunities: '展示已通过状态',
                targetStatus: 'ADMIN_APPROVED'
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
                title: reportData.title,
                reportWeek: reportData.reportWeek,
                additionalNotes: reportData.additionalNotes,
                developmentOpportunities: reportData.developmentOpportunities,
                content: {
                    routine_tasks: [],
                    developmental_tasks: []
                }
            });
            
            if (createResponse.status === 200) {
                const report = createResponse.data.data;
                createdReports.push({
                    ...report,
                    targetStatus: reportData.targetStatus
                });
                console.log(`✅ Created report ${i + 1}: ID ${report.id} - ${report.title}`);
            }
        }
        
        console.log('\n📋 Test data created successfully!');
        console.log('\n🔧 Next steps:');
        console.log('1. Manually update the database to set the correct statuses:');
        
        createdReports.forEach((report, index) => {
            console.log(`   UPDATE weekly_reports SET approval_status = '${report.targetStatus}' WHERE id = ${report.id};`);
        });
        
        console.log('\n2. Or run this SQL script:');
        
        // Generate SQL commands
        const sqlCommands = createdReports.map(report => 
            `UPDATE weekly_reports SET approval_status = '${report.targetStatus}' WHERE id = ${report.id};`
        ).join('\n');
        
        console.log('SQL Commands:');
        console.log('```sql');
        console.log(sqlCommands);
        console.log('SELECT id, title, approval_status FROM weekly_reports WHERE id IN (' + 
                   createdReports.map(r => r.id).join(', ') + ');');
        console.log('```');
        
        console.log('\n3. After updating the database, test the admin interface:');
        console.log('   - Visit: http://localhost:3008/app/admin-reports');
        console.log('   - Login with admin/admin123');
        console.log('   - Check the different tabs: 待审批, 已通过, 已拒绝');
        console.log('   - Test approval and rejection actions');
        
        console.log('\n📝 Expected results after database update:');
        console.log('- 待审批 tab should show 2 reports');
        console.log('- 已通过 tab should show 1 report');
        console.log('- Approval/rejection buttons should work correctly');
        
    } catch (error) {
        console.error('❌ Setup failed:', error.message);
    }
}

setupAdminTestData();