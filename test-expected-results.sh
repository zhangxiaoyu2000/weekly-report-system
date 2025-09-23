#!/bin/bash

# 测试 expected_results 字段的处理

echo "=== 测试 expected_results 字段 ==="

# 获取JWT Token
echo "1. 登录获取JWT Token..."
LOGIN_RESPONSE=$(curl -s --noproxy localhost -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "testuser", "password": "test12345"}')

JWT_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$JWT_TOKEN" ]; then
    echo "登录失败，无法获取JWT Token"
    echo "登录响应: $LOGIN_RESPONSE"
    exit 1
fi

echo "JWT Token 获取成功: ${JWT_TOKEN:0:50}..."

# 测试创建项目并检查 expected_results 字段
echo ""
echo "2. 创建包含详细 expected_results 的项目..."

PROJECT_DATA='{
  "name": "测试项目ExpectedResults",
  "description": "用于测试 expected_results 字段的项目",
  "members": "测试团队",
  "expectedResults": "项目级别的预期结果",
  "timeline": "2024年Q1",
  "stopLoss": "测试止损点",
  "phases": [
    {
      "phaseName": "第一阶段",
      "description": "第一个阶段的描述",
      "assignedMembers": "张三, 李四",
      "schedule": "2024年1月-2月",
      "expectedResults": "第一阶段的详细预期结果：完成需求分析和设计文档"
    },
    {
      "phaseName": "第二阶段", 
      "description": "第二个阶段的描述",
      "assignedMembers": "王五, 赵六",
      "schedule": "2024年3月-4月",
      "expectedResults": "第二阶段的详细预期结果：完成系统开发和单元测试，确保所有功能模块正常运行"
    }
  ]
}'

echo "发送的JSON数据:"
echo "$PROJECT_DATA" | jq '.'

CREATE_RESPONSE=$(curl -s --noproxy localhost -X POST http://localhost:8081/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "$PROJECT_DATA")

echo ""
echo "3. 创建项目的响应:"
echo "$CREATE_RESPONSE" | jq '.'

# 提取项目ID
PROJECT_ID=$(echo $CREATE_RESPONSE | jq -r '.data.id // empty')

if [ -z "$PROJECT_ID" ] || [ "$PROJECT_ID" = "null" ]; then
    echo ""
    echo "❌ 项目创建失败或无法获取项目ID"
    exit 1
fi

echo ""
echo "✅ 项目创建成功，ID: $PROJECT_ID"

# 获取项目详情验证 expected_results
echo ""
echo "4. 获取项目详情验证 expected_results 字段..."

PROJECT_DETAIL=$(curl -s --noproxy localhost -X GET "http://localhost:8081/api/projects/$PROJECT_ID" \
  -H "Authorization: Bearer $JWT_TOKEN")

echo "项目详情响应:"
echo "$PROJECT_DETAIL" | jq '.'

# 检查阶段的 expected_results
echo ""
echo "5. 检查各阶段的 expected_results 字段:"

PHASES=$(echo "$PROJECT_DETAIL" | jq -r '.data.phases[]?')
if [ -z "$PHASES" ]; then
    echo "❌ 没有找到项目阶段数据"
else
    echo "$PROJECT_DETAIL" | jq -r '
        if .data.phases then
            .data.phases[] | 
            "阶段: \(.phaseName)\nexpected_results: \(.expectedResults // "null")\n---"
        else
            "❌ 响应中没有phases字段"
        end
    '
fi

echo ""
echo "6. 检查数据库中的实际数据..."
docker-compose exec mysql mysql -u root -prootpass123 qr_auth_dev \
  -e "SELECT id, phase_name, expected_results FROM project_phases WHERE project_id = $PROJECT_ID;" 2>/dev/null

echo ""
echo "=== 测试完成 ==="