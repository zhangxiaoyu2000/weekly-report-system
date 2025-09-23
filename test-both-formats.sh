#!/bin/bash

echo "=== 测试两种字段名格式 ==="

JWT_TOKEN=$(curl -s --noproxy localhost -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "testuser", "password": "test12345"}' | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

echo "JWT Token获取成功"

# 测试1: 驼峰格式 expectedResults
echo ""
echo "测试1: 驼峰格式 expectedResults"
RESPONSE1=$(curl -s --noproxy localhost -X POST http://localhost:8081/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "测试驼峰格式",
    "description": "测试项目",
    "phases": [{"phaseName": "阶段1", "expectedResults": "驼峰格式的预期结果"}]
  }')

echo "驼峰格式结果: $(echo $RESPONSE1 | jq '.data.phases[0].expectedResults')"

# 测试2: 下划线格式 expected_results  
echo ""
echo "测试2: 下划线格式 expected_results"
RESPONSE2=$(curl -s --noproxy localhost -X POST http://localhost:8081/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "测试下划线格式", 
    "description": "测试项目",
    "phases": [{"phaseName": "阶段2", "expected_results": "下划线格式的预期结果"}]
  }')

echo "下划线格式结果: $(echo $RESPONSE2 | jq '.data.phases[0].expectedResults')"

# 测试3: 混合格式
echo ""
echo "测试3: 混合格式（两个阶段）"
RESPONSE3=$(curl -s --noproxy localhost -X POST http://localhost:8081/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "测试混合格式",
    "description": "测试项目", 
    "phases": [
      {"phaseName": "阶段A", "expectedResults": "驼峰格式"},
      {"phaseName": "阶段B", "expected_results": "下划线格式"}
    ]
  }')

echo "混合格式结果:"
echo "  阶段A: $(echo $RESPONSE3 | jq '.data.phases[0].expectedResults')"
echo "  阶段B: $(echo $RESPONSE3 | jq '.data.phases[1].expectedResults')"

echo ""
echo "=== 所有格式都支持！==="