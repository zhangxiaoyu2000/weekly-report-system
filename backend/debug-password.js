#!/usr/bin/env node

const http = require('http');

function testPasswordDebug() {
    return new Promise((resolve, reject) => {
        const postData = JSON.stringify({
            username: "admin"
        });

        const options = {
            hostname: 'localhost',
            port: 8081,
            path: '/debug/check-password',  // Need to create this endpoint
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

async function testBCryptValidation() {
    const bcrypt = require('bcrypt');
    
    console.log('=== BCrypt验证测试 ===');
    
    // Test known BCrypt hash patterns
    const testCases = [
        { 
            plain: 'admin123', 
            hash: '$2a$10$', // BCrypt prefix 
            desc: 'BCrypt pattern test'
        }
    ];
    
    // Test if a password could match the BCrypt format
    try {
        const testHash = await bcrypt.hash('admin123', 10);
        console.log(`生成的BCrypt hash示例: ${testHash}`);
        
        const isValid = await bcrypt.compare('admin123', testHash);
        console.log(`BCrypt验证测试: ${isValid ? '成功' : '失败'}`);
        
    } catch (error) {
        console.log(`BCrypt测试失败: ${error.message}`);
    }
}

async function main() {
    console.log('=== 密码调试工具 ===\n');
    
    // First test if bcrypt is available
    try {
        await testBCryptValidation();
    } catch (error) {
        console.log('BCrypt模块不可用，跳过本地测试');
    }
    
    console.log('\n验证结论:');
    console.log('- 如果用户名admin对应的数据库密码hash是BCrypt格式');
    console.log('- 且原始密码确实是admin123');
    console.log('- 那么BCrypt.matches(admin123, hash)应该返回true');
    console.log('- 当前返回false表明要么hash不对，要么原始密码不对');
}

main().catch(console.error);