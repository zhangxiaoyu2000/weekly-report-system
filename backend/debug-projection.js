/**
 * 调试projection查询问题
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

async function debugProjection() {
    try {
        console.log('=== 调试projection查询 ===\n');

        // 1. 管理员登录
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

        const token = loginResponse.data.data.accessToken;
        console.log('✅ 登录成功');

        // 2. 测试不同的项目查询端点
        console.log('\n2. 测试不同的项目查询端点...');
        
        // 测试获取所有项目
        console.log('\n2.1 测试获取所有项目...');
        const allProjectsResponse = await makeRequest({
            hostname: 'localhost',
            port: 8081,
            path: '/api/projects',
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            }
        });
        
        if (allProjectsResponse.status === 200) {
            const allProjects = allProjectsResponse.data.data;
            console.log(`   总项目数: ${allProjects.length}`);
            
            allProjects.forEach(project => {
                if (project.id === 55) {
                    console.log(`   ✓ 找到项目55: 状态=${project.approvalStatus}`);
                }
            });
        } else {
            console.log(`   ❌ 获取所有项目失败: ${allProjectsResponse.status}`);
        }

        // 测试获取已批准项目
        console.log('\n2.2 测试获取已批准项目...');
        const approvedResponse = await makeRequest({
            hostname: 'localhost',
            port: 8081,
            path: '/api/projects/approved',
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            }
        });
        
        console.log(`   状态码: ${approvedResponse.status}`);
        if (approvedResponse.status === 200) {
            const approvedProjects = approvedResponse.data.data;
            console.log(`   已批准项目数: ${approvedProjects.length}`);
            
            if (approvedProjects.length === 0) {
                console.log('   ❌ 没有找到已批准的项目，但数据库中项目55是SUPER_ADMIN_APPROVED状态');
                console.log('   这表明查询逻辑可能有问题');
            }
        }

        // 测试待审核项目
        console.log('\n2.3 测试获取待管理员审核项目...');
        const pendingResponse = await makeRequest({
            hostname: 'localhost',
            port: 8081,
            path: '/api/projects/pending-review',
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            }
        });
        
        if (pendingResponse.status === 200) {
            console.log(`   待管理员审核项目数: ${pendingResponse.data.data.length}`);
        }

        console.log('\n3. 分析结果:');
        console.log('   数据库状态: 项目55 = SUPER_ADMIN_APPROVED');
        console.log('   查询条件: SUPER_ADMIN_APPROVED, FINAL_APPROVED');
        console.log('   用户角色: ADMIN');
        console.log('   调用方法: findAdminApprovedProjectsWithDetails()');
        console.log('   预期: 应该找到项目55');
        console.log('   实际: 没有找到任何项目');
        console.log('\n   可能原因:');
        console.log('   1. Projection查询有语法错误');
        console.log('   2. LEFT JOIN条件有问题');
        console.log('   3. 数据类型转换问题');

    } catch (error) {
        console.error('❌ 调试失败:', error.message);
    }
}

debugProjection();