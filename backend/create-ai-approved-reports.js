// Create reports and set them to AI_APPROVED status
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

async function createAIApprovedReports() {
    console.log('🤖 Creating AI-approved reports for admin testing...');
    
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
        
        // Create and immediately AI-approve reports
        const testReports = [
            {
                title: '管理员测试 - 待审批周报 A',
                reportWeek: '2025年第4周',
                additionalNotes: '这是一个等待管理员审批的测试周报，AI分析已通过',
                developmentOpportunities: '测试管理员审批流程'
            },
            {
                title: '管理员测试 - 待审批周报 B', 
                reportWeek: '2025年第4周',
                additionalNotes: '另一个等待管理员审批的测试周报，可以测试拒绝功能',
                developmentOpportunities: '验证管理员界面交互'
            }
        ];
        
        const aiApprovedReports = [];
        
        for (let i = 0; i < testReports.length; i++) {
            const reportData = testReports[i];
            
            // Create the report
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
                console.log(`✅ Created report ${i + 1}: ID ${report.id}`);
                
                // Immediately AI-approve it
                const aiApproveOptions = {
                    hostname: 'localhost',
                    port: 8081,
                    path: `/api/weekly-reports/${report.id}/ai-approve?aiAnalysisId=1`,
                    method: 'PUT',
                    headers
                };
                
                const aiApproveResponse = await makeRequest(aiApproveOptions);
                
                if (aiApproveResponse.status === 200) {
                    console.log(`🤖 AI approved report ${report.id}`);
                    aiApprovedReports.push(report);
                } else {
                    console.log(`⚠️ Failed to AI approve report ${report.id}:`, aiApproveResponse.data);
                }
            } else {
                console.log(`❌ Failed to create report ${i + 1}:`, createResponse.data);
            }
        }
        
        // Create one admin-approved report for testing the "已通过" tab
        const approvedReportData = {
            title: '管理员测试 - 已通过周报',
            reportWeek: '2025年第4周',
            additionalNotes: '这是一个管理员已经审批通过的测试周报',
            developmentOpportunities: '展示已通过状态',
            content: {
                routine_tasks: [],
                developmental_tasks: []
            }
        };
        
        const createApprovedResponse = await makeRequest({
            hostname: 'localhost',
            port: 8081,
            path: '/api/weekly-reports',
            method: 'POST',
            headers
        }, approvedReportData);
        
        if (createApprovedResponse.status === 200) {
            const approvedReport = createApprovedResponse.data.data;
            console.log(`✅ Created report for approval testing: ID ${approvedReport.id}`);
            
            // AI approve it first
            const aiApproveResponse = await makeRequest({
                hostname: 'localhost',
                port: 8081,
                path: `/api/weekly-reports/${approvedReport.id}/ai-approve?aiAnalysisId=1`,
                method: 'PUT',
                headers
            });
            
            if (aiApproveResponse.status === 200) {
                console.log(`🤖 AI approved report ${approvedReport.id}`);
                
                // Then admin approve it
                const adminApproveResponse = await makeRequest({
                    hostname: 'localhost',
                    port: 8081,
                    path: `/api/weekly-reports/${approvedReport.id}/admin-approve`,
                    method: 'PUT',
                    headers
                });
                
                if (adminApproveResponse.status === 200) {
                    console.log(`👨‍💼 Admin approved report ${approvedReport.id}`);
                } else {
                    console.log(`⚠️ Failed to admin approve report ${approvedReport.id}:`, adminApproveResponse.data);
                }
            }
        }
        
        // Verify final state
        console.log('\n📊 Verifying final state...');
        
        const finalReportsResponse = await makeRequest({
            hostname: 'localhost',
            port: 8081,
            path: '/api/weekly-reports',
            method: 'GET',
            headers
        });
        
        const finalReports = finalReportsResponse.data.data;
        const aiApprovedCount = finalReports.filter(r => r.approvalStatus === 'AI_APPROVED').length;
        const adminApprovedCount = finalReports.filter(r => r.approvalStatus === 'ADMIN_APPROVED').length;
        const adminRejectedCount = finalReports.filter(r => r.approvalStatus === 'ADMIN_REJECTED').length;
        
        console.log(`📋 Final status summary:`);
        console.log(`- AI_APPROVED (待审批): ${aiApprovedCount}`);
        console.log(`- ADMIN_APPROVED (已通过): ${adminApprovedCount}`);
        console.log(`- ADMIN_REJECTED (已拒绝): ${adminRejectedCount}`);
        
        console.log('\n🎉 Setup complete! Now you can test the admin interface:');
        console.log('1. Visit: http://localhost:3008/app/admin-reports');
        console.log('2. Login with admin/admin123');
        console.log('3. Check all three tabs and their functionality');
        console.log('4. Test approval and rejection buttons');
        
        console.log('\n📝 Expected behavior:');
        console.log(`- "待审批" tab should show ${aiApprovedCount} reports with action buttons`);
        console.log(`- "已通过" tab should show ${adminApprovedCount} reports (no action buttons)`);
        console.log(`- "已拒绝" tab should show ${adminRejectedCount} reports (no action buttons)`);
        
    } catch (error) {
        console.error('❌ Setup failed:', error.message);
    }
}

createAIApprovedReports();