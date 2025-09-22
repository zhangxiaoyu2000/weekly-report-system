const axios = require('axios');

// 配置
const BASE_URL = 'http://localhost:8080/api';
const USER_TOKEN = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtYW5hZ2VyMSIsInVzZXJJZCI6MiwiaWF0IjoxNzQzNDEwOTE1LCJleHAiOjE3NDM0OTczMTUsInR5cGUiOiJBQ0NFU1MifQ.example'; // 请替换为实际的token

// 获取API数据
async function getAPIData() {
    try {
        console.log('=== 调用 /api/weekly-reports/my 接口 ===');
        
        const response = await axios.get(`${BASE_URL}/weekly-reports/my`, {
            headers: {
                'Authorization': `Bearer ${USER_TOKEN}`
            }
        });
        
        console.log('接口返回状态:', response.status);
        console.log('接口返回数据总数:', response.data.data.length);
        
        // 检查是否有重复的周报ID
        const reportIds = response.data.data.map(report => report.id);
        const uniqueIds = [...new Set(reportIds)];
        
        console.log('所有周报ID:', reportIds);
        console.log('唯一周报ID:', uniqueIds);
        console.log('是否有重复ID:', reportIds.length !== uniqueIds.length);
        
        if (reportIds.length !== uniqueIds.length) {
            console.log('⚠️ 发现重复的周报数据！');
            
            // 找出重复的ID
            const duplicates = reportIds.filter((id, index) => reportIds.indexOf(id) !== index);
            console.log('重复的周报ID:', [...new Set(duplicates)]);
        }
        
        // 显示每个周报的基本信息
        console.log('\n=== 周报详情 ===');
        response.data.data.forEach((report, index) => {
            console.log(`[${index + 1}] ID: ${report.id}, 标题: ${report.title}, 用户ID: ${report.userId}, AI分析ID: ${report.aiAnalysisId || '无'}`);
        });
        
        return response.data.data;
        
    } catch (error) {
        console.error('API调用失败:', error.response?.data || error.message);
        return null;
    }
}

// 直接查询数据库数据（需要通过SQL）
async function suggestSQLQuery() {
    console.log('\n=== 建议的SQL查询语句 ===');
    console.log('请在数据库中执行以下SQL来验证数据：');
    console.log('');
    console.log('-- 查询manager1用户的周报总数');
    console.log('SELECT COUNT(*) as total_reports FROM weekly_reports WHERE user_id = 2;');
    console.log('');
    console.log('-- 查询manager1用户的所有周报详情');
    console.log('SELECT id, title, user_id, approval_status, created_at FROM weekly_reports WHERE user_id = 2 ORDER BY created_at DESC;');
    console.log('');
    console.log('-- 查询AI分析结果表，看是否有重复记录');
    console.log('SELECT report_id, COUNT(*) as ai_count FROM ai_analysis_results WHERE entity_type = \'WEEKLY_REPORT\' GROUP BY report_id HAVING COUNT(*) > 1;');
    console.log('');
    console.log('-- 查询LEFT JOIN的完整结果（可能会有重复）');
    console.log(`SELECT wr.id, wr.title, wr.user_id, ai.id as ai_id, ai.entity_type 
FROM weekly_reports wr 
LEFT JOIN ai_analysis_results ai ON ai.report_id = wr.id AND ai.entity_type = 'WEEKLY_REPORT' 
WHERE wr.user_id = 2 
ORDER BY wr.created_at DESC;`);
}

// 主函数
async function main() {
    console.log('开始调试周报数据重复问题...\n');
    
    const apiData = await getAPIData();
    
    if (apiData) {
        await suggestSQLQuery();
        
        console.log('\n=== 分析结果 ===');
        console.log('如果API返回的数据比数据库中的周报数量多，可能的原因：');
        console.log('1. LEFT JOIN导致的重复：一个周报对应多个AI分析结果');
        console.log('2. 查询逻辑错误：可能JOIN了其他表导致重复');
        console.log('3. 数据库中存在脏数据：重复的关联记录');
        console.log('');
        console.log('请检查上面的SQL查询结果来确认具体原因。');
    }
}

main().catch(console.error);