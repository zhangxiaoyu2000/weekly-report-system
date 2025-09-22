const http = require('http');

function login() {
    console.log('=== 获取JWT Token ===');
    
    const postData = JSON.stringify({
        usernameOrEmail: 'admin',
        password: 'admin123'
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
            try {
                const jsonData = JSON.parse(data);
                if (jsonData.success && jsonData.data && (jsonData.data.token || jsonData.data.accessToken)) {
                    const token = jsonData.data.token || jsonData.data.accessToken;
                    console.log('登录成功！');
                    
                    // 检查项目状态
                    checkProjectStatus(token, 55);
                    
                    // 获取待审核项目列表
                    setTimeout(() => {
                        getPendingProjects(token);
                    }, 1000);
                } else {
                    console.log('登录失败:', jsonData.message);
                }
            } catch (error) {
                console.log('解析JSON失败:', error.message);
            }
        });
    });
    
    req.on('error', (error) => {
        console.error('请求失败:', error.message);
    });
    
    req.write(postData);
    req.end();
}

function checkProjectStatus(token, projectId) {
    console.log('\\n=== 检查项目状态 ===');
    console.log('项目ID:', projectId);
    
    const options = {
        hostname: 'localhost',
        port: 8081,
        path: `/api/projects/${projectId}`,
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    };
    
    const req = http.request(options, (res) => {
        console.log('项目详情API状态:', res.statusCode);
        
        let data = '';
        res.on('data', (chunk) => {
            data += chunk;
        });
        
        res.on('end', () => {
            try {
                const jsonData = JSON.parse(data);
                if (jsonData.success && jsonData.data) {
                    const project = jsonData.data;
                    console.log('项目名称:', project.name);
                    console.log('当前状态:', project.approvalStatus);
                    console.log('AI分析ID:', project.aiAnalysisId || 'null');
                    
                    // 解释状态含义
                    const statusExplanation = {
                        'AI_ANALYZING': 'AI分析中',
                        'AI_APPROVED': 'AI分析通过，等待管理员审核',
                        'AI_REJECTED': 'AI分析拒绝',
                        'ADMIN_REVIEWING': '管理员审核中',
                        'ADMIN_APPROVED': '管理员审核通过，等待超级管理员审核',
                        'ADMIN_REJECTED': '管理员审核拒绝',
                        'SUPER_ADMIN_REVIEWING': '超级管理员审核中',
                        'SUPER_ADMIN_APPROVED': '超级管理员审核通过',
                        'SUPER_ADMIN_REJECTED': '超级管理员审核拒绝',
                        'FINAL_APPROVED': '最终批准'
                    };
                    
                    console.log('状态说明:', statusExplanation[project.approvalStatus] || '未知状态');
                    
                    if (project.approvalStatus === 'ADMIN_REVIEWING') {
                        console.log('✅ 该项目可以进行管理员审批/拒绝操作');
                    } else {
                        console.log('❌ 该项目当前状态不能进行管理员审批操作');
                        console.log('需要状态为: AI_APPROVED 或 AI_ANALYZING');
                    }
                } else {
                    console.log('获取项目详情失败:', jsonData.message);
                }
            } catch (error) {
                console.log('解析项目详情失败:', error.message);
                console.log('原始响应:', data);
            }
        });
    });
    
    req.on('error', (error) => {
        console.error('获取项目详情失败:', error.message);
    });
    
    req.end();
}

function getPendingProjects(token) {
    console.log('\\n=== 获取待审核项目列表 ===');
    
    const options = {
        hostname: 'localhost',
        port: 8081,
        path: '/api/projects/pending',
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    };
    
    const req = http.request(options, (res) => {
        console.log('待审核项目API状态:', res.statusCode);
        
        let data = '';
        res.on('data', (chunk) => {
            data += chunk;
        });
        
        res.on('end', () => {
            try {
                const jsonData = JSON.parse(data);
                if (jsonData.success && jsonData.data) {
                    const projects = jsonData.data;
                    console.log('待审核项目数量:', projects.length);
                    
                    projects.forEach((project, index) => {
                        console.log(`\\n项目 ${index + 1}:`);
                        console.log('  ID:', project.id);
                        console.log('  名称:', project.name);
                        console.log('  状态:', project.approvalStatus);
                        console.log('  创建者:', project.createdByUsername || 'unknown');
                        
                        if (project.approvalStatus === 'ADMIN_REVIEWING') {
                            console.log('  🎯 此项目可用于测试审批功能');
                        }
                    });
                } else {
                    console.log('获取待审核项目失败:', jsonData.message);
                }
            } catch (error) {
                console.log('解析待审核项目失败:', error.message);
                console.log('原始响应:', data.substring(0, 500));
            }
        });
    });
    
    req.on('error', (error) => {
        console.error('获取待审核项目失败:', error.message);
    });
    
    req.end();
}

login();