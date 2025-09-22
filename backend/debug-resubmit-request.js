/**
 * Debug script to test resubmit endpoint with various data formats
 * This helps identify the exact cause of the validation error
 */

const API_BASE = 'http://localhost:8081/api';

// Use existing manager token
const authToken = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtYW5hZ2VyMSIsInJvbGVzIjoiUk9MRV9NQU5BR0VSIiwiaWF0IjoxNzU4NTMzMjE3LCJleHAiOjE3NTg1MzY4MTcsInVzZXJJZCI6MTAwMDQsImZ1bGxOYW1lIjoibWFuYWdlcjEiLCJlbWFpbCI6Im1hbmFnZXIxQHdlZWtseXJlcG9ydC5jb20ifQ.TzM7CQ7Vv7-rJweVk9LjVamG0Q_EQ37-7WftluvaH8l9LwdekGJ8gmsaTv0A8vB-VargB02ehjYreJSPQJn4Ow';

async function testValidationError() {
  console.log('🐛 调试resubmit请求验证错误\n');
  
  // Test case 1: 正常的数据格式
  console.log('📋 测试1: 正常的数据格式');
  const normalData = {
    name: '正常项目名称',
    description: '正常项目描述',
    members: '正常项目成员',
    expectedResults: '正常预期结果',
    timeline: '正常时间线',
    stopLoss: '正常止损点',
    projectPhases: [
      {
        phaseName: '阶段1',
        description: '阶段描述',
        assignedMembers: '负责成员',
        schedule: '时间安排',
        expectedResults: '预期结果'
      }
    ]
  };
  
  await testResubmit(66, normalData, '正常格式');
  
  // Test case 2: 模拟前端可能发送的错误格式
  console.log('\n📋 测试2: 模拟name字段为数组的情况');
  const errorData1 = {
    name: [9], // 这可能是导致错误的原因
    description: '测试描述',
    members: '测试成员',
    expectedResults: '测试预期结果',
    timeline: '测试时间线',
    stopLoss: '测试止损点',
    projectPhases: []
  };
  
  await testResubmit(66, errorData1, 'name字段为数组');
  
  // Test case 3: 模拟name字段为数字的情况
  console.log('\n📋 测试3: 模拟name字段为数字的情况');
  const errorData2 = {
    name: 9, // 数字而不是字符串
    description: '测试描述',
    members: '测试成员',
    expectedResults: '测试预期结果',
    timeline: '测试时间线',
    stopLoss: '测试止损点',
    projectPhases: []
  };
  
  await testResubmit(66, errorData2, 'name字段为数字');
  
  // Test case 4: 检查项目66的状态
  console.log('\n📋 测试4: 检查项目66的当前状态');
  await checkProjectStatus(66);
}

async function testResubmit(projectId, data, testName) {
  try {
    console.log(`\n🔄 执行测试: ${testName}`);
    console.log('📤 发送的数据:', JSON.stringify(data, null, 2));
    
    const response = await fetch(`${API_BASE}/simple/projects/${projectId}/resubmit`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`
      },
      body: JSON.stringify(data)
    });
    
    const result = await response.json();
    
    console.log(`📥 响应状态: ${response.status}`);
    console.log('📥 响应内容:', JSON.stringify(result, null, 2));
    
    if (result.success) {
      console.log('✅ 请求成功');
    } else {
      console.log('❌ 请求失败:', result.message);
    }
    
  } catch (error) {
    console.error('💥 请求发生错误:', error.message);
  }
}

async function checkProjectStatus(projectId) {
  try {
    const response = await fetch(`${API_BASE}/simple/projects/${projectId}`, {
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    });
    
    const result = await response.json();
    
    if (result.success) {
      console.log('✅ 项目信息获取成功');
      console.log('📊 项目ID:', result.data.id);
      console.log('📋 项目名称:', result.data.name);
      console.log('📊 当前状态:', result.data.approvalStatus);
      console.log('🔍 拒绝原因:', result.data.rejectionReason || '无');
      
      if (result.data.approvalStatus === 'REJECTED') {
        console.log('✅ 项目状态为REJECTED，可以进行resubmit测试');
      } else {
        console.log('⚠️ 项目状态不是REJECTED，resubmit会失败');
      }
    } else {
      console.log('❌ 获取项目信息失败:', result.message);
    }
    
  } catch (error) {
    console.error('💥 获取项目信息发生错误:', error.message);
  }
}

async function debugFieldTypes() {
  console.log('\n🔍 调试字段类型问题');
  
  // 检查各种可能导致验证错误的数据类型
  const testCases = [
    { name: 'name字段为undefined', data: { name: undefined } },
    { name: 'name字段为null', data: { name: null } },
    { name: 'name字段为空字符串', data: { name: '' } },
    { name: 'name字段为单字符', data: { name: 'A' } },
    { name: 'name字段为数组', data: { name: [9] } },
    { name: 'name字段为对象', data: { name: { value: 9 } } },
    { name: 'name字段为数字', data: { name: 9 } },
    { name: 'name字段为布尔', data: { name: true } },
  ];
  
  console.log('📋 测试各种name字段类型的验证错误:');
  
  for (const testCase of testCases) {
    console.log(`\n🧪 ${testCase.name}:`);
    console.log('📤 数据:', JSON.stringify(testCase.data));
    
    // 模拟验证错误
    const nameValue = testCase.data.name;
    const nameType = typeof nameValue;
    const isString = typeof nameValue === 'string';
    const length = isString ? nameValue.length : 'N/A';
    
    console.log(`📊 类型: ${nameType}, 是字符串: ${isString}, 长度: ${length}`);
    
    if (!isString) {
      console.log('❌ 这会导致验证错误：name字段必须是字符串');
    } else if (nameValue.length < 2 || nameValue.length > 100) {
      console.log('❌ 这会导致验证错误：name长度必须在2-100字符之间');
    } else {
      console.log('✅ 这个值应该通过验证');
    }
  }
}

async function runDebug() {
  console.log('🚀 开始调试resubmit请求验证错误\n');
  
  console.log('📝 问题描述:');
  console.log('   - 用户报告resubmit请求失败');
  console.log('   - 错误信息显示name字段值为[9]');
  console.log('   - 验证错误：name必须在2-100字符之间');
  console.log('   - 请求URL显示为localhost:3005而不是8081\n');
  
  await debugFieldTypes();
  await testValidationError();
  
  console.log('\n📊 调试总结:');
  console.log('🎯 可能的问题原因:');
  console.log('  1. 前端projectForm.projectName字段数据类型错误');
  console.log('  2. 前端发送到错误的端口(3005而不是8081)');
  console.log('  3. 前端数据绑定问题导致name字段变成数组');
  console.log('  4. 代理配置问题导致请求被转发到其他服务');
  
  console.log('\n💡 建议解决方案:');
  console.log('  1. 检查前端Vue组件中projectForm的数据绑定');
  console.log('  2. 确认前端请求的URL和端口正确');
  console.log('  3. 在前端提交前添加数据类型验证');
  console.log('  4. 检查开发环境的代理配置');
}

// 运行调试
runDebug();