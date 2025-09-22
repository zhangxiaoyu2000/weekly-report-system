/**
 * Debug script to check project 66's actual data in database
 */

const API_BASE = 'http://localhost:8081/api';

// Use existing manager token
const authToken = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtYW5hZ2VyMSIsInJvbGVzIjoiUk9MRV9NQU5BR0VSIiwiaWF0IjoxNzU4NTMzMjE3LCJleHAiOjE3NTg1MzY4MTcsInVzZXJJZCI6MTAwMDQsImZ1bGxOYW1lIjoibWFuYWdlcjEiLCJlbWFpbCI6Im1hbmFnZXIxQHdlZWtseXJlcG9ydC5jb20ifQ.TzM7CQ7Vv7-rJweVk9LjVamG0Q_EQ37-7WftluvaH8l9LwdekGJ8gmsaTv0A8vB-VargB02ehjYreJSPQJn4Ow';

async function debugProject66Data() {
  console.log('🔍 调试项目66的实际数据');
  
  try {
    // 获取项目66的原始数据
    console.log('\n📋 1. 获取项目66的原始数据 (直接调用后端API)');
    const rawResponse = await fetch(`${API_BASE}/projects/66`, {
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    });
    
    const rawData = await rawResponse.json();
    console.log('📤 原始后端数据:', JSON.stringify(rawData, null, 2));
    
    if (rawData.success) {
      const project = rawData.data;
      console.log('\n📊 项目字段分析:');
      console.log('  - name类型:', typeof project.name, '值:', project.name);
      console.log('  - description类型:', typeof project.description, '值:', project.description);
      console.log('  - members类型:', typeof project.members, '值:', project.members);
      console.log('  - expectedResults类型:', typeof project.expectedResults, '值:', project.expectedResults);
      console.log('  - timeline类型:', typeof project.timeline, '值:', project.timeline);
      console.log('  - stopLoss类型:', typeof project.stopLoss, '值:', project.stopLoss);
      
      // 检查是否有奇怪的数据
      Object.entries(project).forEach(([key, value]) => {
        if (typeof value === 'string' && value === '9') {
          console.log(`🚨 发现可疑数据: ${key} = "${value}"`);
        }
      });
    }
    
    // 获取项目66通过simpleProjectAPI (前端API接口)
    console.log('\n📋 2. 测试simpleProjectAPI接口的数据转换');
    const simpleResponse = await fetch(`${API_BASE}/simple/projects/66`, {
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    });
    
    if (simpleResponse.ok) {
      const simpleData = await simpleResponse.json();
      console.log('📤 simpleProjectAPI数据:', JSON.stringify(simpleData, null, 2));
      
      if (simpleData.success) {
        const simpleProject = simpleData.data;
        console.log('\n📊 simpleProjectAPI字段分析:');
        console.log('  - projectName类型:', typeof simpleProject.projectName, '值:', simpleProject.projectName);
        console.log('  - projectContent类型:', typeof simpleProject.projectContent, '值:', simpleProject.projectContent);
        console.log('  - projectMembers类型:', typeof simpleProject.projectMembers, '值:', simpleProject.projectMembers);
      }
    } else {
      console.log('❌ simpleProjectAPI接口不存在或出错');
    }
    
    // 模拟前端数据加载流程
    console.log('\n📋 3. 模拟前端数据加载流程');
    if (rawData.success) {
      const projectResponse = rawData;
      
      // 模拟前端的数据映射（假设使用simpleProjectAPI的格式）
      const frontendData = {
        projectName: projectResponse.data.name,  // 这里可能是问题
        projectContent: projectResponse.data.description,
        projectMembers: projectResponse.data.members,
        expectedResults: projectResponse.data.expectedResults,
        timeline: projectResponse.data.timeline,
        stopLoss: projectResponse.data.stopLoss
      };
      
      console.log('📊 前端映射后的数据:');
      Object.entries(frontendData).forEach(([key, value]) => {
        console.log(`  - ${key}: ${typeof value} = "${value}"`);
        if (value === undefined || value === null) {
          console.log(`🚨 ${key}字段为undefined/null，可能导致前端显示问题`);
        }
      });
    }
    
  } catch (error) {
    console.error('💥 调试过程出错:', error.message);
  }
}

async function checkUserReportedError() {
  console.log('\n🚨 检查用户报告的具体错误');
  console.log('用户报告的错误信息:');
  console.log('  - URL: http://localhost:3005/api/simple/projects/66/resubmit');
  console.log('  - 错误: name field rejected value [9]');
  console.log('  - 这说明前端发送的name字段值是数组[9]而不是字符串');
  
  console.log('\n🔍 可能的原因分析:');
  console.log('1. 前端页面中projectForm.projectName被意外设置为数组');
  console.log('2. Vue的数据绑定出现问题');
  console.log('3. 前端代码中某处错误地操作了表单数据');
  console.log('4. 用户的浏览器环境有缓存或其他问题');
  
  console.log('\n💡 建议调试步骤:');
  console.log('1. 在前端EditProjectView.vue的loadProjectData函数中添加console.log');
  console.log('2. 在handleSubmit函数开始时打印projectForm的所有字段值');
  console.log('3. 检查是否有其他地方修改了projectForm.projectName');
  console.log('4. 检查用户的具体操作流程');
}

async function runDebug() {
  console.log('🚀 开始调试项目66数据问题\n');
  
  await debugProject66Data();
  await checkUserReportedError();
  
  console.log('\n📊 调试总结:');
  console.log('🎯 需要重点检查:');
  console.log('  1. 项目66在数据库中的实际数据是否正常');
  console.log('  2. simpleProjectAPI是否正确处理数据转换');
  console.log('  3. 前端表单数据绑定是否有问题');
  console.log('  4. 是否有代码在无意中修改了projectForm.projectName');
}

// 运行调试
runDebug();