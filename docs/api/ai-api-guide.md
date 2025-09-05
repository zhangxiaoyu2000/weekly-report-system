# AI API 使用指南

本文档详细介绍了周报系统AI功能API的使用方法，包括分析、建议生成和项目洞察等功能。

## 概览

AI API 提供以下核心功能：
- 周报内容智能分析
- 智能建议生成  
- 项目洞察分析
- AI服务健康监控

**基础URL：** `/api/ai`
**认证方式：** Bearer JWT Token
**响应格式：** JSON

## 通用响应格式

所有API响应都遵循统一格式：

```json
{
  "success": true,
  "message": "操作成功",
  "data": { ... },
  "timestamp": "2025-09-05T12:00:00Z"
}
```

错误响应：
```json
{
  "success": false,
  "message": "错误描述",
  "error": "详细错误信息",
  "timestamp": "2025-09-05T12:00:00Z"
}
```

## API 端点详情

### 1. 启动周报AI分析

启动对指定周报的AI分析任务。

**端点：** `POST /api/ai/analyze-report/{reportId}`  
**权限：** USER  

#### 请求参数

**路径参数：**
- `reportId` (Long, 必需): 周报ID

**请求体：**
```json
{
  "reportId": 1,
  "analysisTypes": ["summary", "sentiment", "keywords", "risks"],
  "analysisLanguage": "zh-CN",
  "includeDetails": true
}
```

#### 请求体字段说明

| 字段 | 类型 | 必需 | 描述 |
|------|------|------|------|
| `reportId` | Long | 是 | 周报ID（从路径参数自动设置） |
| `analysisTypes` | Array[String] | 否 | 分析类型列表，可选值：summary, sentiment, keywords, risks |
| `analysisLanguage` | String | 否 | 分析语言，默认 zh-CN |
| `includeDetails` | Boolean | 否 | 是否包含详细分析，默认 false |

#### 响应示例

```json
{
  "success": true,
  "message": "AI analysis started successfully",
  "data": {
    "analysisId": 1693987200000,
    "reportId": 1,
    "status": "PROCESSING",
    "summary": "AI分析正在处理中...",
    "sentiment": "NEUTRAL",
    "sentimentScore": 0.0,
    "confidenceScore": 85,
    "analysisStartTime": "2025-09-05 12:00:00"
  }
}
```

#### 示例代码

```javascript
// JavaScript/Fetch
const response = await fetch('/api/ai/analyze-report/1', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_JWT_TOKEN'
  },
  body: JSON.stringify({
    analysisTypes: ['summary', 'sentiment', 'keywords'],
    analysisLanguage: 'zh-CN',
    includeDetails: true
  })
});
const result = await response.json();
```

```python
# Python/Requests
import requests

headers = {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_JWT_TOKEN'
}

data = {
    'analysisTypes': ['summary', 'sentiment', 'keywords'],
    'analysisLanguage': 'zh-CN',
    'includeDetails': True
}

response = requests.post('/api/ai/analyze-report/1', 
                        headers=headers, json=data)
result = response.json()
```

```bash
# cURL
curl -X POST '/api/ai/analyze-report/1' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -d '{
    "analysisTypes": ["summary", "sentiment", "keywords"],
    "analysisLanguage": "zh-CN",
    "includeDetails": true
  }'
```

### 2. 获取AI分析结果

获取指定周报的AI分析结果。

**端点：** `GET /api/ai/analysis/{reportId}`  
**权限：** USER

#### 请求参数

**路径参数：**
- `reportId` (Long, 必需): 周报ID

#### 响应示例

```json
{
  "success": true,
  "message": "Analysis result retrieved successfully",
  "data": {
    "analysisId": 1693987200000,
    "reportId": 1,
    "status": "COMPLETED",
    "summary": "本周工作进展良好，团队协作效率高，项目按计划推进。主要完成了用户认证模块和API接口开发。",
    "sentiment": "POSITIVE",
    "sentimentScore": 0.75,
    "keywords": ["项目进展", "团队协作", "用户认证", "API开发"],
    "risks": ["时间压力可能导致质量下降", "新技术学习曲线较陡"],
    "suggestions": [
      "建议增加代码审查频次以确保质量",
      "可以考虑引入自动化测试工具提高效率"
    ],
    "insights": {
      "workloadDistribution": {
        "development": 60,
        "testing": 25,
        "documentation": 10,
        "meetings": 5
      },
      "communicationScore": 88,
      "innovationIndex": 75
    },
    "confidenceScore": 85,
    "analysisStartTime": "2025-09-05 12:00:00",
    "analysisCompleteTime": "2025-09-05 12:05:00",
    "processingTimeMs": 300000
  }
}
```

### 3. 生成智能建议

根据用户输入和上下文生成个性化的智能建议。

**端点：** `POST /api/ai/generate-suggestions`  
**权限：** USER

#### 请求体

```json
{
  "userId": 1,
  "projectId": 2,
  "reportId": 3,
  "context": "report_improvement",
  "userInput": "如何提高周报质量？",
  "focusAreas": ["productivity", "communication"],
  "maxSuggestions": 5,
  "additionalContext": {
    "teamSize": 5,
    "projectPhase": "development"
  }
}
```

#### 请求体字段说明

| 字段 | 类型 | 必需 | 描述 |
|------|------|------|------|
| `userId` | Long | 是 | 用户ID（由系统自动设置） |
| `projectId` | Long | 否 | 项目ID，用于项目相关建议 |
| `reportId` | Long | 否 | 周报ID，用于报告相关建议 |
| `context` | String | 是 | 建议上下文，可选值：report_improvement, project_planning, team_management |
| `userInput` | String | 否 | 用户输入的具体问题或需求 |
| `focusAreas` | Array[String] | 否 | 关注领域列表 |
| `maxSuggestions` | Integer | 否 | 最大建议数量，默认5 |
| `additionalContext` | Object | 否 | 额外的上下文信息 |

#### 响应示例

```json
{
  "success": true,
  "message": "Suggestions generated successfully",
  "data": {
    "suggestionId": "mock-1693987200000",
    "context": "report_improvement",
    "suggestions": [
      {
        "title": "增加具体数据支撑",
        "description": "在周报中加入具体的性能指标、完成度百分比等量化数据，使报告更具说服力",
        "category": "improvement",
        "priority": "HIGH",
        "confidenceScore": 92,
        "tags": ["数据化", "量化指标", "可视化"],
        "actionType": "immediate"
      },
      {
        "title": "优化问题描述格式",
        "description": "采用结构化的问题描述模板，包括问题背景、影响范围、解决方案和时间计划",
        "category": "optimization",
        "priority": "MEDIUM",
        "confidenceScore": 85,
        "tags": ["格式化", "结构化", "模板"],
        "actionType": "short_term"
      }
    ],
    "totalSuggestions": 2,
    "confidence": "HIGH",
    "generatedAt": "2025-09-05 12:00:00"
  }
}
```

### 4. 获取项目AI洞察

分析项目的进展、团队表现、风险和趋势等洞察信息。

**端点：** `GET /api/ai/project-insights/{projectId}`  
**权限：** USER

#### 请求参数

**路径参数：**
- `projectId` (Long, 必需): 项目ID

**查询参数：**
- `startDate` (String, 可选): 分析开始日期 (YYYY-MM-DD)
- `endDate` (String, 可选): 分析结束日期 (YYYY-MM-DD)
- `includeComparisons` (Boolean, 可选): 是否包含历史对比，默认 false
- `includePredictions` (Boolean, 可选): 是否包含未来预测，默认 false

#### 响应示例

```json
{
  "success": true,
  "message": "Project insights generated successfully",
  "data": {
    "projectId": 1,
    "projectName": "周报系统开发",
    "analysisStartDate": "2025-08-01",
    "analysisEndDate": "2025-09-05",
    "progressInsight": {
      "completionPercentage": 68.5,
      "progressStatus": "on_track",
      "tasksCompleted": 15,
      "totalTasks": 22,
      "progressSummary": "项目整体进展良好，关键里程碑按时完成",
      "keyAchievements": [
        "用户认证模块开发完成",
        "数据库设计和迁移完成",
        "基础API接口实现完成"
      ],
      "blockers": [
        "第三方API集成等待审批",
        "UI设计评审需要更多时间"
      ]
    },
    "teamInsight": {
      "averageProductivity": 78.5,
      "teamMorale": "high",
      "activeMembers": 5,
      "memberContributions": {
        "张三": 85.0,
        "李四": 92.0,
        "王五": 76.0,
        "赵六": 88.0,
        "钱七": 79.0
      },
      "collaborationPatterns": [
        "代码审查参与度高",
        "知识分享活跃",
        "问题解决响应及时"
      ],
      "improvementAreas": [
        "可以增加跨团队技术交流",
        "建议优化会议效率"
      ]
    },
    "risks": [
      {
        "riskType": "timeline",
        "severity": "medium",
        "description": "项目时间线存在一定压力，需要关注关键路径上的任务进展",
        "probability": 0.45,
        "mitigation": ["制定应对预案", "增加监控频率", "提前准备备选方案"],
        "impact": "中等影响"
      }
    ],
    "trends": [
      {
        "trendType": "productivity",
        "direction": "increasing",
        "description": "团队生产效率呈上升趋势",
        "significance": "medium",
        "data": {
          "currentValue": 78.5,
          "previousValue": 72.1,
          "changeRate": 8.9
        }
      }
    ],
    "generatedAt": "2025-09-05 12:00:00"
  }
}
```

### 5. AI服务健康检查

检查AI服务的健康状态和可用性。

**端点：** `GET /api/ai/health`  
**权限：** USER

#### 响应示例

```json
{
  "success": true,
  "message": "AI service is healthy",
  "data": {
    "status": "healthy",
    "ai_service": "operational",
    "response_time": "250ms",
    "last_check": "2025-09-05T12:00:00"
  }
}
```

### 6. 获取AI服务性能指标

获取AI服务的详细性能指标和统计信息（管理员专用）。

**端点：** `GET /api/ai/metrics`  
**权限：** ADMIN

#### 请求参数

**查询参数：**
- `timeRange` (String, 可选): 时间范围，可选值：24h, 7d, 30d，默认 24h

#### 响应示例

```json
{
  "success": true,
  "message": "AI metrics retrieved successfully",
  "data": {
    "serviceStatus": "healthy",
    "totalRequests": 1250,
    "successfulRequests": 1189,
    "failedRequests": 61,
    "successRate": 95.12,
    "averageResponseTime": 1850.5,
    "p95ResponseTime": 3200.0,
    "totalAnalysesCompleted": 892,
    "averageAnalysisAccuracy": 87.3,
    "errorCounts": {
      "network_error": 23,
      "api_limit_exceeded": 15,
      "invalid_request": 12,
      "timeout": 8,
      "service_unavailable": 3
    },
    "providerPerformance": {
      "openai": 95.0,
      "azure": 92.0,
      "local": 88.0
    },
    "lastHealthCheck": "2025-09-05 12:00:00",
    "timeRange": "24h",
    "activeAnalyses": 5,
    "queuedAnalyses": 12,
    "systemLoad": 0.17,
    "resourceUsage": {
      "cpu_usage": 45.2,
      "memory_usage": 67.8,
      "disk_usage": 23.1,
      "network_io": 156.7
    },
    "metricsCollectedAt": "2025-09-05 12:00:00"
  }
}
```

## 错误处理

### 常见错误码

| HTTP状态码 | 错误类型 | 描述 |
|------------|----------|------|
| 400 | Bad Request | 请求参数无效 |
| 401 | Unauthorized | 未认证或token无效 |
| 403 | Forbidden | 权限不足 |
| 404 | Not Found | 资源不存在 |
| 429 | Too Many Requests | 请求频率超限 |
| 500 | Internal Server Error | 服务器内部错误 |
| 503 | Service Unavailable | AI服务暂时不可用 |

### 错误响应示例

```json
{
  "success": false,
  "message": "Report not found",
  "error": "The requested report with ID 999 does not exist",
  "timestamp": "2025-09-05T12:00:00Z"
}
```

## 最佳实践

### 1. 认证和授权
- 始终在请求头中包含有效的JWT token
- 确保token未过期，及时刷新
- 不同的端点需要不同的权限级别

### 2. 请求频率控制
- 避免过于频繁的API调用
- 实施客户端缓存策略
- 对于分析类请求，建议轮询间隔不少于5秒

### 3. 错误处理
- 实施重试机制，但要注意指数退避
- 记录详细的错误日志便于调试
- 为用户提供友好的错误提示

### 4. 性能优化
- 合理设置请求超时时间
- 使用分页查询大量数据
- 适当使用缓存减少重复请求

### 5. 数据隐私
- 不在日志中记录敏感信息
- 遵循数据最小化原则
- 定期清理不需要的分析结果

## 限制说明

### 请求限制
- API调用频率：每用户每分钟最多100次请求
- 分析任务：每用户同时最多5个并发分析任务
- 请求体大小：最大2MB
- 响应超时：分析请求最长30秒

### 数据限制
- 周报内容：最大50KB文本
- 分析历史：保留最近6个月的分析结果
- 建议数量：单次最多返回20条建议

### 功能限制
- 支持的语言：中文（zh-CN）、英文（en-US）
- AI模型：基于GPT-3.5/4.0架构
- 分析类型：文本分析、情感分析、关键词提取、风险评估

## 更新日志

### v1.0.0 (2025-09-05)
- 初始API发布
- 支持基础分析功能
- 实现健康检查和监控
- 添加智能建议生成
- 项目洞察分析功能

---

如有问题或建议，请联系开发团队或查看更多文档。