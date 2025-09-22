/**
 * 调试登录响应结构
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

async function debugLogin() {
    try {
        console.log('=== 调试登录响应结构 ===\n');

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

        console.log('登录响应状态:', loginResponse.status);
        console.log('登录响应完整结构:');
        console.log(JSON.stringify(loginResponse.data, null, 2));

        if (loginResponse.status === 200) {
            const data = loginResponse.data;
            console.log('\n解析结构:');
            console.log('- data 存在:', !!data.data);
            console.log('- token 路径 data.data.token:', data.data?.token ? 'exists' : 'missing');
            console.log('- token 路径 data.token:', data.token ? 'exists' : 'missing');
            console.log('- accessToken 路径 data.data.accessToken:', data.data?.accessToken ? 'exists' : 'missing');
            console.log('- accessToken 路径 data.accessToken:', data.accessToken ? 'exists' : 'missing');
            
            if (data.data?.token) {
                console.log('- Token长度:', data.data.token.length);
                console.log('- Token开头:', data.data.token.substring(0, 50) + '...');
            }
        }

    } catch (error) {
        console.error('❌ 调试失败:', error.message);
    }
}

debugLogin();