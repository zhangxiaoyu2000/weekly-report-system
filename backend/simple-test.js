const http = require('http');

function testAPI() {
    console.log('=== 测试待审核项目API ===');
    
    const token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczNzQ1MzA5NSwiZXhwIjoxNzM3NDU2Njk1fQ.g1f3Jag-Y7z5aEWJP-d2vCu-2AphheKyNQGV4xwJKWY';
    
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
                    console.log('=== 第一个项目 ===');
                    console.log('项目ID:', project.id);
                    console.log('项目名称:', project.name);
                    console.log('审批状态:', project.approvalStatus);
                    console.log('是否有阶段数据:', !!project.phases);
                    console.log('阶段数量:', project.phases ? project.phases.length : 0);
                    console.log('是否有AI分析结果:', !!project.aiAnalysisResult);
                    
                    if (project.phases && project.phases.length > 0) {
                        console.log('第一个阶段名称:', project.phases[0].phaseName);
                    }
                    
                    if (project.aiAnalysisResult) {
                        console.log('AI分析结果类型:', typeof project.aiAnalysisResult);
                        if (typeof project.aiAnalysisResult === 'object') {
                            console.log('AI结果ID:', project.aiAnalysisResult.id);
                            console.log('置信度:', project.aiAnalysisResult.confidence);
                        }
                    }
                }
            } catch (error) {
                console.log('解析JSON失败:', error.message);
                console.log('原始响应:', data.substring(0, 200));
            }
        });
    });
    
    req.on('error', (error) => {
        console.error('请求失败:', error.message);
    });
    
    req.end();
}

testAPI();