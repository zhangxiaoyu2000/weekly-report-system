/**
 * 检查项目55的审批人信息
 */
const http = require('http');

function makeRequest(options, data = null) {
    return new Promise((resolve, reject) => {
        const req = http.request(options, (res) => {
            let responseData = '';
            res.on('data', chunk => responseData += chunk);
            res.on('end', () => {
                try {
                    const parsed = JSON.parse(responseData);
                    resolve({ status: res.statusCode, data: parsed });
                } catch (e) {
                    resolve({ status: res.statusCode, data: responseData });
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

async function checkProjectReviewer() {
    try {
        console.log('=== 检查项目审批人信息 ===\n');

        // 1. 管理员登录
        console.log('1. 管理员登录...');
        const loginResponse = await makeRequest({
            hostname: 'localhost',
            port: 8081,
            path: '/api/auth/login',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        }, {
            usernameOrEmail: 'admin',
            password: 'admin123'
        });

        if (loginResponse.status !== 200) {
            throw new Error(`Login failed: ${loginResponse.status}`);
        }

        const token = loginResponse.data.data.token;
        const userId = loginResponse.data.data.user.id;
        const username = loginResponse.data.data.user.username;
        const userRole = loginResponse.data.data.user.role;
        
        console.log(`✅ 登录成功 - User ID: ${userId}, Username: ${username}, Role: ${userRole}`);

        // 2. 查询特定项目详情
        console.log('\n2. 查询项目55的详细信息...');
        const projectResponse = await makeRequest({
            hostname: 'localhost',
            port: 8081,
            path: '/api/projects/55',
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (projectResponse.status === 200) {
            const project = projectResponse.data.data;
            console.log('✅ 项目详情获取成功');
            console.log(`项目名称: ${project.name}`);
            console.log(`项目状态: ${project.approvalStatus}`);
            console.log(`创建者ID: ${project.createdBy}`);
            console.log(`管理员审批人ID: ${project.adminReviewerId || 'N/A'}`);
            console.log(`超级管理员审批人ID: ${project.superAdminReviewerId || 'N/A'}`);
            console.log(`拒绝理由: ${project.rejectionReason || 'N/A'}`);
        } else {
            console.log('❌ 项目详情获取失败:', projectResponse.data);
        }

        // 3. 查询所有已通过的项目（通过不同接口）
        console.log('\n3. 查询已通过项目列表...');
        const approvedResponse = await makeRequest({
            hostname: 'localhost',
            port: 8081,
            path: '/api/projects/approved',
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (approvedResponse.status === 200) {
            console.log(`✅ 找到 ${approvedResponse.data.data.length} 个已通过项目`);
            
            const project55 = approvedResponse.data.data.find(p => p.id === 55);
            if (project55) {
                console.log('\n📋 项目55在已通过列表中的信息:');
                console.log(`   状态: ${project55.approvalStatus}`);
                console.log(`   管理员审批人ID: ${project55.adminReviewerId || 'N/A'}`);
                console.log(`   管理员审批人用户名: ${project55.adminReviewerUsername || 'N/A'}`);
                console.log(`   超级管理员审批人ID: ${project55.superAdminReviewerId || 'N/A'}`);
                console.log(`   超级管理员审批人用户名: ${project55.superAdminReviewerUsername || 'N/A'}`);
            } else {
                console.log('⚠️  项目55不在已通过列表中');
            }
        } else {
            console.log('❌ 获取已通过项目失败:', approvedResponse.data);
        }

        console.log('\n🎉 检查完成');

    } catch (error) {
        console.error('❌ 检查失败:', error.message);
        process.exit(1);
    }
}

checkProjectReviewer();