#!/usr/bin/env node

const http = require('http');

function resetPassword(username, newPassword) {
    return new Promise((resolve, reject) => {
        const postData = JSON.stringify({
            username: username,
            newPassword: newPassword
        });

        const options = {
            hostname: 'localhost',
            port: 8081,
            path: '/api/debug/reset-user-password',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(postData)
            }
        };

        const req = http.request(options, (res) => {
            let data = '';
            
            res.on('data', (chunk) => {
                data += chunk;
            });
            
            res.on('end', () => {
                resolve({
                    statusCode: res.statusCode,
                    body: data
                });
            });
        });

        req.on('error', (e) => {
            reject(e);
        });

        req.write(postData);
        req.end();
    });
}

function testLogin(username, password) {
    return new Promise((resolve, reject) => {
        const postData = JSON.stringify({
            usernameOrEmail: username,
            password: password
        });

        const options = {
            hostname: 'localhost',
            port: 8081,
            path: '/api/auth/login',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(postData)
            }
        };

        const req = http.request(options, (res) => {
            let data = '';
            
            res.on('data', (chunk) => {
                data += chunk;
            });
            
            res.on('end', () => {
                resolve({
                    statusCode: res.statusCode,
                    body: data
                });
            });
        });

        req.on('error', (e) => {
            reject(e);
        });

        req.write(postData);
        req.end();
    });
}

async function main() {
    console.log('=== 重置用户密码并测试登录 ===\n');
    
    const users = [
        { username: 'admin', password: 'admin123' },
        { username: 'superadmin', password: 'super123' },
        { username: 'manager1', password: 'manager123' }
    ];
    
    // Step 1: Reset passwords
    console.log('步骤1: 重置密码...\n');
    for (const user of users) {
        try {
            console.log(`重置 ${user.username} 的密码为 ${user.password}`);
            const resetResult = await resetPassword(user.username, user.password);
            
            if (resetResult.statusCode === 200) {
                const resetData = JSON.parse(resetResult.body);
                if (resetData.success) {
                    console.log(`✅ ${user.username} 密码重置成功`);
                } else {
                    console.log(`❌ ${user.username} 密码重置失败: ${resetData.error}`);
                }
            } else {
                console.log(`❌ ${user.username} 密码重置请求失败 (${resetResult.statusCode})`);
            }
        } catch (error) {
            console.log(`❌ ${user.username} 密码重置出错: ${error.message}`);
        }
    }
    
    console.log('\n步骤2: 测试登录...\n');
    
    // Step 2: Test login
    for (const user of users) {
        try {
            console.log(`测试登录: ${user.username} / ${user.password}`);
            const loginResult = await testLogin(user.username, user.password);
            
            console.log(`状态码: ${loginResult.statusCode}`);
            
            if (loginResult.statusCode === 200) {
                try {
                    const loginData = JSON.parse(loginResult.body);
                    if (loginData.success && loginData.data && loginData.data.accessToken) {
                        console.log(`✅ ${user.username} 登录成功！`);
                        console.log(`JWT令牌: ${loginData.data.accessToken.substring(0, 50)}...`);
                    } else {
                        console.log(`⚠️ ${user.username} 响应格式异常`);
                        console.log(`响应: ${loginResult.body}`);
                    }
                } catch (e) {
                    console.log(`⚠️ ${user.username} 响应解析失败`);
                    console.log(`响应: ${loginResult.body}`);
                }
            } else {
                console.log(`❌ ${user.username} 登录失败 (${loginResult.statusCode})`);
                console.log(`响应: ${loginResult.body}`);
            }
            
            console.log('---\n');
            
        } catch (error) {
            console.log(`❌ ${user.username} 登录测试出错: ${error.message}\n`);
        }
    }
    
    console.log('密码重置和登录测试完成！');
}

main().catch(console.error);