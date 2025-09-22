/**
 * 调试用户信息和审批记录
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

async function debugUsers() {
    try {
        console.log('=== 调试用户信息 ===\n');

        // 测试多个用户登录
        const users = [
            { username: 'admin', password: 'admin123' },
            { username: 'admin2', password: 'admin123' },
            { username: 'manager1', password: 'manager123' }
        ];

        for (const user of users) {
            console.log(`尝试登录用户: ${user.username}`);
            
            const loginResponse = await makeRequest({
                hostname: 'localhost',
                port: 8081,
                path: '/api/auth/login',
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            }, {
                usernameOrEmail: user.username,
                password: user.password
            });

            if (loginResponse.status === 200) {
                const userData = loginResponse.data.data.user;
                console.log(`✅ ${user.username} 登录成功:`);
                console.log(`   ID: ${userData.id}`);
                console.log(`   用户名: ${userData.username}`);
                console.log(`   角色: ${userData.role}`);
                console.log(`   邮箱: ${userData.email || 'N/A'}`);
                console.log('');
            } else {
                console.log(`❌ ${user.username} 登录失败: ${loginResponse.status}`);
                if (loginResponse.data && loginResponse.data.message) {
                    console.log(`   错误: ${loginResponse.data.message}`);
                }
                console.log('');
            }
        }

        console.log('=== 用户信息调试完成 ===');
        console.log('');
        console.log('分析:');
        console.log('- 检查哪个用户ID对应哪个用户名');
        console.log('- 确认实际审批项目55的用户应该是谁');
        console.log('- 如果审批人ID不匹配，可能的原因：');
        console.log('  1. 前端传递的用户token不正确');
        console.log('  2. getCurrentUser()方法返回了错误的用户信息');
        console.log('  3. 数据库中用户ID映射不正确');

    } catch (error) {
        console.error('❌ 调试失败:', error.message);
        process.exit(1);
    }
}

debugUsers();