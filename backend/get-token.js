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
        console.log('Response status:', res.statusCode);
        
        let data = '';
        res.on('data', (chunk) => {
            data += chunk;
        });
        
        res.on('end', () => {
            try {
                const jsonData = JSON.parse(data);
                console.log('响应数据:', JSON.stringify(jsonData, null, 2));
                
                if (jsonData.success && jsonData.data && (jsonData.data.token || jsonData.data.accessToken)) {
                    console.log('登录成功！');
                    const token = jsonData.data.token || jsonData.data.accessToken;
                    console.log('Token:', token);
                    
                    // 测试pending projects API
                    testPendingProjects(token);
                } else {
                    console.log('登录失败:', jsonData.message || 'Unknown error');
                    console.log('完整响应:', jsonData);
                }
            } catch (error) {
                console.log('解析JSON失败:', error.message);
                console.log('原始响应:', data);
            }
        });
    });
    
    req.on('error', (error) => {
        console.error('请求失败:', error.message);
    });
    
    req.write(postData);
    req.end();
}

function testPendingProjects(token) {
    console.log('\\n=== 测试待审核项目API ===');
    
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
        console.log('Response status:', res.statusCode);
        
        let data = '';
        res.on('data', (chunk) => {
            data += chunk;
        });
        
        res.on('end', () => {
            try {
                const jsonData = JSON.parse(data);
                console.log('=== API响应成功 ===');
                console.log('成功:', jsonData.success);
                console.log('项目数量:', jsonData.data ? jsonData.data.length : 0);
                
                if (jsonData.data && jsonData.data.length > 0) {
                    const project = jsonData.data[0];
                    console.log('\\n=== 第一个项目 ===');
                    console.log('项目ID:', project.id);
                    console.log('项目名称:', project.name);
                    console.log('审批状态:', project.approvalStatus);
                    console.log('创建者用户名:', project.createdByUsername);
                    console.log('是否有阶段数据:', !!project.phases);
                    console.log('阶段数量:', project.phases ? project.phases.length : 0);
                    console.log('是否有AI分析结果:', !!project.aiAnalysisResult);
                    
                    if (project.phases && project.phases.length > 0) {
                        const phase = project.phases[0];
                        console.log('\\n=== 第一个阶段详情 ===');
                        console.log('阶段名称 (phaseName):', phase.phaseName);
                        console.log('阶段描述 (description):', phase.description);
                        console.log('负责成员 (assignedMembers):', phase.assignedMembers);
                        console.log('时间安排 (schedule):', phase.schedule);
                        console.log('预期结果 (expectedResults):', phase.expectedResults);
                    }
                    
                    if (project.aiAnalysisResult) {
                        console.log('\\n=== AI分析结果 ===');
                        console.log('AI结果ID:', project.aiAnalysisResult.id);
                        console.log('置信度:', project.aiAnalysisResult.confidence);
                        console.log('分析状态:', project.aiAnalysisResult.status);
                    }
                }
            } catch (error) {
                console.log('解析JSON失败:', error.message);
                console.log('原始响应:', data.substring(0, 500));
            }
        });
    });
    
    req.on('error', (error) => {
        console.error('请求失败:', error.message);
    });
    
    req.end();
}

login();