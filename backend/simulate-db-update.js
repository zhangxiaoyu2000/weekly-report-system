// Simulate database updates by creating reports with the desired status through workflow
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

async function simulateStatusUpdates() {
    console.log('🔄 Simulating status updates for admin testing...');
    
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
        
        // Let's simulate the reports by using AI approve on some existing reports
        // First, get all reports to see which ones we can work with
        const reportsOptions = {
            hostname: 'localhost',
            port: 8081,
            path: '/api/weekly-reports',
            method: 'GET',
            headers
        };
        
        const reportsResponse = await makeRequest(reportsOptions);
        const reports = reportsResponse.data.data;
        
        console.log(`📋 Found ${reports.length} total reports`);
        
        // Find reports that we can use for testing (created recently)
        const testReports = reports.filter(r => 
            r.title && r.title.includes('测试报告') && 
            r.approvalStatus === 'AI_ANALYZING'
        ).slice(0, 2); // Take first 2 for testing
        
        if (testReports.length > 0) {
            console.log(`🎯 Found ${testReports.length} test reports to work with`);
            
            // Simulate AI approval on these reports
            for (const report of testReports) {
                console.log(`🤖 Simulating AI approval for report ${report.id}...`);
                
                const aiApproveOptions = {
                    hostname: 'localhost',
                    port: 8081,
                    path: `/api/weekly-reports/${report.id}/ai-approve?aiAnalysisId=1`,
                    method: 'PUT',
                    headers
                };
                
                const aiApproveResponse = await makeRequest(aiApproveOptions);
                
                if (aiApproveResponse.status === 200) {
                    console.log(`✅ Report ${report.id} now has AI_APPROVED status`);
                } else {
                    console.log(`⚠️ Could not AI approve report ${report.id}:`, aiApproveResponse.data);
                }
            }
        }
        
        // Now test the complete workflow
        console.log('\n🧪 Testing complete admin workflow...');
        
        // Get updated reports
        const updatedReportsResponse = await makeRequest(reportsOptions);
        const updatedReports = updatedReportsResponse.data.data;
        
        const aiApprovedReports = updatedReports.filter(r => r.approvalStatus === 'AI_APPROVED');
        const adminApprovedReports = updatedReports.filter(r => r.approvalStatus === 'ADMIN_APPROVED');
        const adminRejectedReports = updatedReports.filter(r => r.approvalStatus === 'ADMIN_REJECTED');
        
        console.log(`📊 Status summary after simulation:`);
        console.log(`- AI_APPROVED (待审批): ${aiApprovedReports.length}`);
        console.log(`- ADMIN_APPROVED (已通过): ${adminApprovedReports.length}`);
        console.log(`- ADMIN_REJECTED (已拒绝): ${adminRejectedReports.length}`);
        
        if (aiApprovedReports.length > 0) {
            console.log('\n✅ Great! Now you can test admin approval functionality:');
            console.log('1. Visit: http://localhost:3008/app/admin-reports');
            console.log('2. Login with admin/admin123');
            console.log('3. You should see reports in the "待审批" tab');
            console.log('4. Test the approval and rejection buttons');
        } else {
            console.log('\n⚠️ No AI_APPROVED reports available for admin testing');
            console.log('The admin interface will still work, but there will be no reports to approve');
        }
        
        console.log('\n🎯 Frontend testing checklist:');
        console.log('- ✅ Tab switching (待审批, 已通过, 已拒绝)');
        console.log('- ✅ Report count badges');
        console.log('- ✅ Status filtering');
        console.log('- ✅ Approval button functionality');
        console.log('- ✅ Rejection modal and functionality');
        console.log('- ✅ Status display and styling');
        
    } catch (error) {
        console.error('❌ Simulation failed:', error.message);
    }
}

simulateStatusUpdates();