const fs = require('fs');

async function debugWeeklyReportData() {
    console.log('🔍 Debug: Testing weekly report data reception...');
    
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    try {
        const token = fs.readFileSync('manager_token_fresh.txt', 'utf8').trim();
        console.log('✅ Token loaded');

        const url = 'http://localhost:8081/api/weekly-reports';
        
        // 简单的测试数据
        const weeklyReportData = {
            title: "数据接收测试",
            reportWeek: "2024年1月第1周（周五）",
            content: {
                routineTasks: [
                    {
                        task_id: "1",
                        actual_result: "测试实际结果",
                        AnalysisofResultDifferences: "测试差异分析"
                    }
                ],
                developmentalTasks: [
                    {
                        project_id: "69",
                        phase_id: "1", 
                        actual_result: "测试项目结果",
                        AnalysisofResultDifferences: "测试项目差异"
                    }
                ]
            },
            nextWeekPlan: {
                routineTasks: [
                    {
                        task_id: "1"
                    }
                ],
                developmentalTasks: [
                    {
                        project_id: "69",
                        phase_id: "1"
                    }
                ]
            },
            additionalNotes: "数据接收测试备注",
            developmentOpportunities: "测试发展机会"
        };
        
        console.log('📤 Sending data structure:');
        console.log('- content.routineTasks:', weeklyReportData.content.routineTasks);
        console.log('- content.developmentalTasks:', weeklyReportData.content.developmentalTasks);
        console.log('- nextWeekPlan.routineTasks:', weeklyReportData.nextWeekPlan.routineTasks);
        console.log('- nextWeekPlan.developmentalTasks:', weeklyReportData.nextWeekPlan.developmentalTasks);
        
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(weeklyReportData)
        });

        console.log(`🌐 Response status: ${response.status}`);
        
        const data = await response.json();
        console.log('📋 Full Response:', JSON.stringify(data, null, 2));
        
        // 检查是否成功创建
        if (response.status === 200 || response.status === 201) {
            console.log('✅ Request accepted');
            
            // 等待几秒钟让AI分析完成
            console.log('⏳ Waiting for AI analysis...');
            await new Promise(resolve => setTimeout(resolve, 10000));
            
            // 获取创建的周报详情查看数据是否正确保存
            if (data.data && data.data.id) {
                const detailUrl = `http://localhost:8081/api/weekly-reports/${data.data.id}`;
                console.log(`🔍 Fetching detail from: ${detailUrl}`);
                
                const detailResponse = await fetch(detailUrl, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                
                if (detailResponse.status === 200) {
                    const detailData = await detailResponse.json();
                    console.log('📊 Weekly Report Detail:', JSON.stringify(detailData, null, 2));
                    
                    // 检查数据是否正确存储
                    if (detailData.data && detailData.data.taskReports) {
                        console.log('✅ TaskReports found:', detailData.data.taskReports.length);
                    }
                    if (detailData.data && detailData.data.devTaskReports) {
                        console.log('✅ DevTaskReports found:', detailData.data.devTaskReports.length);
                    }
                } else {
                    console.log('❌ Failed to fetch detail');
                }
            }
        } else {
            console.log('❌ Request failed');
        }

    } catch (error) {
        console.error('❌ Test failed:', error.message);
    }
}

debugWeeklyReportData();