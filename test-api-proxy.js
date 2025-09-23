// 简单的API代理测试脚本
const http = require('http');

// 模拟前端的API请求
function testAPIProxy() {
    console.log('🔍 测试API代理配置...');
    
    // 测试前端应该如何调用API (通过代理)
    const proxyOptions = {
        hostname: '23.95.193.155',
        port: 3002,
        path: '/api/health',
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    };
    
    // 测试后端直接调用 (应该工作)
    const directOptions = {
        hostname: '23.95.193.155',
        port: 8081,
        path: '/api/health',
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    };
    
    console.log('📡 测试直接后端访问...');
    const directReq = http.request(directOptions, (res) => {
        let data = '';
        res.on('data', (chunk) => data += chunk);
        res.on('end', () => {
            if (res.statusCode === 200) {
                console.log('✅ 直接后端访问成功:', res.statusCode);
                console.log('📄 响应数据:', data);
            } else {
                console.log('❌ 直接后端访问失败:', res.statusCode);
            }
        });
    });
    
    directReq.on('error', (err) => {
        console.log('❌ 直接后端访问错误:', err.message);
    });
    
    directReq.end();
    
    // 测试代理访问
    console.log('🔄 测试代理访问...');
    const proxyReq = http.request(proxyOptions, (res) => {
        let data = '';
        res.on('data', (chunk) => data += chunk);
        res.on('end', () => {
            if (res.statusCode === 200) {
                console.log('✅ 代理访问成功:', res.statusCode);
                console.log('📄 响应数据:', data);
            } else {
                console.log('❌ 代理访问失败:', res.statusCode);
                console.log('📄 错误响应:', data);
            }
        });
    });
    
    proxyReq.on('error', (err) => {
        console.log('❌ 代理访问错误:', err.message);
    });
    
    proxyReq.end();
}

console.log('🚀 启动API代理测试');
testAPIProxy();

setTimeout(() => {
    console.log('\n📋 测试总结:');
    console.log('1. 如果直接后端访问成功，说明后端运行正常');
    console.log('2. 如果代理访问失败，说明nginx代理配置需要修复');
    console.log('3. 修复方法：更新nginx配置 proxy_pass http://23.95.193.155:8081/api/');
}, 3000);