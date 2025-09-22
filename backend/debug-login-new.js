// Debug login response structure
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

async function debugLogin() {
    console.log('🔍 Debugging login response structure...');
    
    try {
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
        
        console.log('📊 Full login response:');
        console.log('Status:', loginResponse.status);
        console.log('Data structure:', JSON.stringify(loginResponse.data, null, 2));
        
        if (loginResponse.data && loginResponse.data.data) {
            console.log('\n🔑 Token details:');
            console.log('Token value:', loginResponse.data.data.token);
            console.log('Token type:', typeof loginResponse.data.data.token);
            console.log('Token length:', loginResponse.data.data.token ? loginResponse.data.data.token.length : 'undefined');
        }
        
    } catch (error) {
        console.error('❌ Debug failed:', error.message);
    }
}

debugLogin();