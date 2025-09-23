#!/bin/bash

echo "=== 测试下划线字段名 expected_results ==="

# 获取JWT Token
JWT_TOKEN=$(curl -s --noproxy localhost -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "testuser", "password": "test12345"}' | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

echo "JWT Token: ${JWT_TOKEN:0:50}..."

# 测试使用下划线格式的字段名
echo ""
echo "测试: 使用 expected_results (下划线格式)"
RESPONSE=$(curl -s --noproxy localhost -X POST http://localhost:8081/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "测试下划线字段名",
    "description": "测试项目",
    "phases": [
      {
        "phaseName": "测试阶段",
        "description": "阶段描述",
        "expected_results": "这是使用下划线字段名的预期结果"
      }
    ]
  }')

PROJECT_ID=$(echo $RESPONSE | jq -r '.data.id // empty')
echo "项目ID: $PROJECT_ID"
echo "创建响应中的expectedResults: $(echo $RESPONSE | jq '.data.phases[0].expectedResults')"

if [ ! -z "$PROJECT_ID" ]; then
    echo ""
    echo "验证: 获取项目详情"
    DETAIL_RESPONSE=$(curl -s --noproxy localhost -X GET "http://localhost:8081/api/projects/$PROJECT_ID" \
      -H "Authorization: Bearer $JWT_TOKEN")
    
    echo "详情响应中的expectedResults: $(echo $DETAIL_RESPONSE | jq '.data.phases[0].expectedResults')"
    
    echo ""
    echo "数据库中的实际值:"
    docker-compose exec mysql mysql -u root -prootpass123 weekly_report_system \
      -e "SELECT id, phase_name, expected_results FROM project_phases WHERE project_id = $PROJECT_ID;" 2>/dev/null
fi

echo ""
echo "=== 测试完成 ==="