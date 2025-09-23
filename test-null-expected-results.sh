#!/bin/bash

echo "=== 测试导致 expectedResults 为空的各种情况 ==="

# 获取JWT Token
JWT_TOKEN=$(curl -s --noproxy localhost -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "testuser", "password": "test12345"}' | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

echo "JWT Token: ${JWT_TOKEN:0:50}..."

# 测试1: expectedResults 字段缺失
echo ""
echo "测试1: expectedResults 字段缺失"
RESPONSE1=$(curl -s --noproxy localhost -X POST http://localhost:8081/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "测试缺失字段",
    "description": "测试项目", 
    "phases": [
      {
        "phaseName": "阶段1",
        "description": "阶段描述"
      }
    ]
  }')

PROJECT_ID1=$(echo $RESPONSE1 | jq -r '.data.id // empty')
echo "项目ID: $PROJECT_ID1"
echo "响应中的expectedResults: $(echo $RESPONSE1 | jq '.data.phases[0].expectedResults')"

# 测试2: expectedResults 为空字符串
echo ""
echo "测试2: expectedResults 为空字符串"
RESPONSE2=$(curl -s --noproxy localhost -X POST http://localhost:8081/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "测试空字符串",
    "description": "测试项目",
    "phases": [
      {
        "phaseName": "阶段2", 
        "description": "阶段描述",
        "expectedResults": ""
      }
    ]
  }')

PROJECT_ID2=$(echo $RESPONSE2 | jq -r '.data.id // empty')
echo "项目ID: $PROJECT_ID2"
echo "响应中的expectedResults: $(echo $RESPONSE2 | jq '.data.phases[0].expectedResults')"

# 测试3: expectedResults 为null
echo ""
echo "测试3: expectedResults 为null"
RESPONSE3=$(curl -s --noproxy localhost -X POST http://localhost:8081/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "测试null值",
    "description": "测试项目",
    "phases": [
      {
        "phaseName": "阶段3",
        "description": "阶段描述", 
        "expectedResults": null
      }
    ]
  }')

PROJECT_ID3=$(echo $RESPONSE3 | jq -r '.data.id // empty')
echo "项目ID: $PROJECT_ID3"
echo "响应中的expectedResults: $(echo $RESPONSE3 | jq '.data.phases[0].expectedResults')"

# 检查数据库中的实际值
echo ""
echo "数据库中的实际值:"
if [ ! -z "$PROJECT_ID1" ]; then
    echo "项目$PROJECT_ID1 (缺失字段):"
    docker-compose exec mysql mysql -u root -prootpass123 weekly_report_system \
      -e "SELECT id, phase_name, expected_results IS NULL as is_null, expected_results = '' as is_empty, expected_results FROM project_phases WHERE project_id = $PROJECT_ID1;" 2>/dev/null
fi

if [ ! -z "$PROJECT_ID2" ]; then
    echo "项目$PROJECT_ID2 (空字符串):"
    docker-compose exec mysql mysql -u root -prootpass123 weekly_report_system \
      -e "SELECT id, phase_name, expected_results IS NULL as is_null, expected_results = '' as is_empty, expected_results FROM project_phases WHERE project_id = $PROJECT_ID2;" 2>/dev/null
fi

if [ ! -z "$PROJECT_ID3" ]; then
    echo "项目$PROJECT_ID3 (null值):"
    docker-compose exec mysql mysql -u root -prootpass123 weekly_report_system \
      -e "SELECT id, phase_name, expected_results IS NULL as is_null, expected_results = '' as is_empty, expected_results FROM project_phases WHERE project_id = $PROJECT_ID3;" 2>/dev/null
fi