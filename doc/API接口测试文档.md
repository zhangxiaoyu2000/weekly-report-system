# API接口测试文档

## 测试摘要

**测试时间**: 2025/9/20 17:13:09  
**测试环境**: http://localhost:8081  
**总接口数**: 53  
**成功接口数**: 31  
**失败接口数**: 22  
**成功率**: 58%  

## 成功的接口 (31个)

| 序号 | 方法 | 路径 | 状态码 | 描述 |
|------|------|------|--------|------|
| 1 | GET | /api/health | 200 | 正常响应 |
| 2 | GET | /api/health/authenticated | 200 | 正常响应 |
| 3 | POST | /api/auth/login | 200 | 正常响应 |
| 4 | POST | /api/auth/refresh | 200 | 正常响应 |
| 5 | GET | /api/auth/check-username?username=testuser999 | 200 | 正常响应 |
| 6 | GET | /api/auth/check-email?email=test999@example.com | 200 | 正常响应 |
| 7 | POST | /api/auth/logout | 200 | 正常响应 |
| 8 | GET | /api/users | 200 | 正常响应 |
| 9 | GET | /api/users?page=0&size=10 | 200 | 正常响应 |
| 10 | GET | /api/users/fast | 200 | 正常响应 |
| 11 | GET | /api/users/profile | 200 | 正常响应 |
| 12 | GET | /api/users/1 | 200 | 正常响应 |
| 13 | GET | /api/users/search?keyword=admin | 200 | 正常响应 |
| 14 | GET | /api/users/role/ADMIN | 200 | 正常响应 |
| 15 | GET | /api/users/statistics | 200 | 正常响应 |
| 16 | GET | /api/weekly-reports | 200 | 正常响应 |
| 17 | GET | /api/weekly-reports?page=0&size=10 | 200 | 正常响应 |
| 18 | GET | /api/weekly-reports/my | 200 | 正常响应 |
| 19 | GET | /api/weekly-reports/pending | 200 | 正常响应 |
| 20 | GET | /api/projects | 200 | 正常响应 |
| 21 | GET | /api/projects?page=0&size=10 | 200 | 正常响应 |
| 22 | GET | /api/projects/my | 200 | 正常响应 |
| 23 | GET | /api/projects/pending | 200 | 正常响应 |
| 24 | POST | /api/projects | 201 | 正常响应 |
| 25 | GET | /api/tasks | 200 | 正常响应 |
| 26 | GET | /api/tasks?page=0&size=10 | 200 | 正常响应 |
| 27 | GET | /api/tasks/my | 200 | 正常响应 |
| 28 | GET | /api/tasks/by-type/ROUTINE | 200 | 正常响应 |
| 29 | GET | /api/debug/user/admin | 200 | 正常响应 |
| 30 | POST | /api/debug/test-password | 200 | 正常响应 |
| 31 | POST | /api/debug/reset-user-password | 200 | 正常响应 |

## 失败的接口 (22个)

| 序号 | 方法 | 路径 | 状态码 | 错误信息 | 响应内容 |
|------|------|------|--------|----------|----------|
| 1 | POST | /api/auth/login | 401 | 无 | {"success":false,"message":"Invalid username/email or password","data":null,"timestamp":"2025-09-20T17:13:07.588914"} |
| 2 | POST | /api/auth/register | 400 | 无 | {"success":false,"message":"Username already exists","data":null,"timestamp":"2025-09-20T17:13:07.615159"} |
| 3 | POST | /api/auth/change-password | 400 | 无 | {"success":false,"message":"User not found","data":null,"timestamp":"2025-09-20T17:13:07.636247"} |
| 4 | PUT | /api/users/profile | 404 | 无 | {"success":false,"message":"User not found","data":null,"timestamp":"2025-09-20T17:13:07.754747"} |
| 5 | POST | /api/users | 409 | 无 | {"success":false,"message":"Username already exists","data":null,"timestamp":"2025-09-20T17:13:07.836118"} |
| 6 | GET | /api/ai/health | 500 | 无 | {"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.845478"} |
| 7 | GET | /api/ai/metrics | 500 | 无 | {"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.852403"} |
| 8 | GET | /api/ai/metrics?timeRange=7d | 500 | 无 | {"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.85871"} |
| 9 | POST | /api/ai/analyze/project | 500 | 无 | {"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.865414"} |
| 10 | POST | /api/ai/analyze/weekly-report | 500 | 无 | {"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.871744"} |
| 11 | GET | /api/ai/analysis/1 | 500 | 无 | {"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.877481"} |
| 12 | POST | /api/ai/generate-suggestions | 500 | 无 | {"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.882714"} |
| 13 | GET | /api/ai/project-insights/1 | 500 | 无 | {"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.888031"} |
| 14 | POST | /api/weekly-reports | 500 | 无 | {"success":false,"message":"创建周报失败: could not execute statement [Unknown column 'template_id' in 'NEW'] [/* insert for com.weeklyreport.entity.WeeklyReport */insert into weekly_reports (additional_not... |
| 15 | GET | /api/weekly-reports/1 | 404 | 无 | {"success":false,"message":"周报不存在","data":null,"timestamp":"2025-09-20T17:13:08.036701"} |
| 16 | PUT | /api/weekly-reports/1/submit | 404 | 无 | {"success":false,"message":"周报不存在","data":null,"timestamp":"2025-09-20T17:13:08.05528"} |
| 17 | GET | /api/projects/1 | 404 | 无 | {"success":false,"message":"Project not found","data":null,"timestamp":"2025-09-20T17:13:08.122822"} |
| 18 | PUT | /api/projects/1/submit | 404 | 无 | {"success":false,"message":"Project not found","data":null,"timestamp":"2025-09-20T17:13:08.130095"} |
| 19 | POST | /api/tasks | 500 | 无 | {"success":false,"message":"Failed to create task: could not execute statement [Data truncated for column 'task_type' at row 1] [/* insert for com.weeklyreport.entity.Task */insert into tasks (actual_... |
| 20 | GET | /api/tasks/1 | 404 | 无 | {"success":false,"message":"Task not found","data":null,"timestamp":"2025-09-20T17:13:08.198407"} |
| 21 | GET | /api/simple/hello | 401 | 无 | {"path":"/api/simple/hello","errorCode":"INSUFFICIENT_AUTHENTICATION","error":"Unauthorized","message":"Authentication failed: Full authentication is required to access this resource","timestamp":"202... |
| 22 | POST | /api/simple/fix-passwords | 401 | 无 | {"path":"/api/simple/fix-passwords","errorCode":"INSUFFICIENT_AUTHENTICATION","error":"Unauthorized","message":"Authentication failed: Full authentication is required to access this resource","timesta... |

## 详细测试结果

### 成功接口详情


#### 1. GET /api/health
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Service is healthy","data":{"service":"weekly-report-backend","version":"1.0.0","status":"UP","timestamp":"2025-09-20T17:13:07.173743"},"timestamp":"2025-09-20T17:13:07.1737...
```


#### 2. GET /api/health/authenticated
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Authentication verified","data":{"user":"admin","authorities":[{"authority":"ROLE_ADMIN"}],"status":"AUTHENTICATED","timestamp":"2025-09-20T17:13:07.20576"},"timestamp":"202...
```


#### 3. POST /api/auth/login
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Login successful","data":{"accessToken":"eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjoiUk9MRV9BRE1JTiIsImlhdCI6MTc1ODM1OTU4NywiZXhwIjoxNzU4MzYzMTg3LCJ1c2VySWQiOjEsImZ...
```


#### 4. POST /api/auth/refresh
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Token refreshed successfully (fallback)","data":{"accessToken":"fallback-token-1758359587625","refreshToken":"fallback-refresh-1758359587625","tokenType":"Bearer","expiresIn...
```


#### 5. GET /api/auth/check-username?username=testuser999
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Username is available","data":true,"timestamp":"2025-09-20T17:13:07.649781"}
```


#### 6. GET /api/auth/check-email?email=test999@example.com
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Email is available","data":true,"timestamp":"2025-09-20T17:13:07.658937"}
```


#### 7. POST /api/auth/logout
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Logout successful","data":"","timestamp":"2025-09-20T17:13:07.663274"}
```


#### 8. GET /api/users
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Users retrieved successfully","data":{"content":[{"id":1,"username":"admin","email":"admin@company.com","password":"$2a$12$ZyOVPXJCtXcmmE3DCcJ5e.imiGrhdv6ieGsCRS/aXkq2PoHAmJ...
```


#### 9. GET /api/users?page=0&size=10
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Users retrieved successfully","data":{"content":[{"id":1,"username":"admin","email":"admin@company.com","password":"$2a$12$ZyOVPXJCtXcmmE3DCcJ5e.imiGrhdv6ieGsCRS/aXkq2PoHAmJ...
```


#### 10. GET /api/users/fast
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Fast users retrieved successfully","data":{"content":[{"id":1,"username":"admin","email":"admin@company.com","password":"$2a$12$ZyOVPXJCtXcmmE3DCcJ5e.imiGrhdv6ieGsCRS/aXkq2P...
```


#### 11. GET /api/users/profile
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Profile retrieved successfully","data":{"id":1,"username":"admin","email":"admin@company.com","password":"$2a$12$ZyOVPXJCtXcmmE3DCcJ5e.imiGrhdv6ieGsCRS/aXkq2PoHAmJUUu","role...
```


#### 12. GET /api/users/1
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"User retrieved successfully","data":{"id":1,"username":"admin","email":"admin@company.com","password":"$2a$12$ZyOVPXJCtXcmmE3DCcJ5e.imiGrhdv6ieGsCRS/aXkq2PoHAmJUUu","role":"...
```


#### 13. GET /api/users/search?keyword=admin
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Users found successfully","data":{"content":[{"id":1,"username":"admin","email":"admin@company.com","password":"$2a$12$ZyOVPXJCtXcmmE3DCcJ5e.imiGrhdv6ieGsCRS/aXkq2PoHAmJUUu"...
```


#### 14. GET /api/users/role/ADMIN
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Users retrieved successfully","data":[{"id":1,"username":"admin","email":"admin@company.com","password":"$2a$12$ZyOVPXJCtXcmmE3DCcJ5e.imiGrhdv6ieGsCRS/aXkq2PoHAmJUUu","role"...
```


#### 15. GET /api/users/statistics
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"User statistics retrieved successfully","data":{"activeUsers":9,"inactiveUsers":0,"lockedUsers":0,"admins":3,"managers":4,"totalUsers":9},"timestamp":"2025-09-20T17:13:07.82...
```


#### 16. GET /api/weekly-reports
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"获取周报列表成功","data":[],"timestamp":"2025-09-20T17:13:07.905128"}
```


#### 17. GET /api/weekly-reports?page=0&size=10
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"获取周报列表成功","data":[],"timestamp":"2025-09-20T17:13:07.919317"}
```


#### 18. GET /api/weekly-reports/my
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"获取我的周报列表成功","data":[],"timestamp":"2025-09-20T17:13:07.935594"}
```


#### 19. GET /api/weekly-reports/pending
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"获取待审批周报列表成功","data":[],"timestamp":"2025-09-20T17:13:07.949775"}
```


#### 20. GET /api/projects
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Success","data":{"content":[{"id":11,"name":"Test Project","description":"Test Description","members":"testuser","expectedResults":"Test Results","timeline":"2 weeks","stopL...
```


#### 21. GET /api/projects?page=0&size=10
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Success","data":{"content":[{"id":11,"name":"Test Project","description":"Test Description","members":"testuser","expectedResults":"Test Results","timeline":"2 weeks","stopL...
```


#### 22. GET /api/projects/my
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Success","data":[{"id":10,"name":"更新后的接口测试项目","description":"更新后的项目描述","members":null,"expectedResults":"更新后的预期结果","timeline":"3周","stopLoss":"更新后的止损条件","createdBy":10004,"a...
```


#### 23. GET /api/projects/pending
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Success","data":[],"timestamp":"2025-09-20T17:13:08.096282"}
```


#### 24. POST /api/projects
- **状态码**: 201
- **响应示例**: 
```json
{"success":true,"message":"Success","data":{"id":12,"name":"Test Project 1758359588096","description":"This is a test project","members":"Team Alpha","expectedResults":"Complete API development","time...
```


#### 25. GET /api/tasks
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Success","data":{"content":[{"id":13,"taskName":"系统数据库性能监控与优化","personnelAssignment":"manager1, 技术团队","timeline":"每周一至周五 09:00-18:00","quantitativeMetrics":"数据库查询响应时间<2秒，CPU...
```


#### 26. GET /api/tasks?page=0&size=10
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Success","data":{"content":[{"id":13,"taskName":"系统数据库性能监控与优化","personnelAssignment":"manager1, 技术团队","timeline":"每周一至周五 09:00-18:00","quantitativeMetrics":"数据库查询响应时间<2秒，CPU...
```


#### 27. GET /api/tasks/my
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Success","data":[],"timestamp":"2025-09-20T17:13:08.163887"}
```


#### 28. GET /api/tasks/by-type/ROUTINE
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"message":"Success","data":[],"timestamp":"2025-09-20T17:13:08.173982"}
```


#### 29. GET /api/debug/user/admin
- **状态码**: 200
- **响应示例**: 
```json
{"found":true,"role":"ADMIN","passwordStartsWith":"$2a$12$ZyO","id":1,"email":"admin@company.com","passwordLength":60,"isBCryptFormat":true,"username":"admin","status":"ACTIVE"}
```


#### 30. POST /api/debug/test-password
- **状态码**: 200
- **响应示例**: 
```json
{"newHashMatches":true,"passwordProvided":"admin123","userStatus":"ACTIVE","passwordMatches":true,"storedPasswordHash":"$2a$12$ZyOVPXJCtXcmmE3DCcJ5e.imiGrhdv6ieGsCRS/aXkq2PoHAmJUUu","newPasswordHash":...
```


#### 31. POST /api/debug/reset-user-password
- **状态码**: 200
- **响应示例**: 
```json
{"success":true,"oldPassword":"$2a$12$ZyOVPXJCtXcmmE3DCcJ5e.imiGrhdv6ieGsCRS/aXkq2PoHAmJUUu","newPassword":"$2a$12$WZlG6x1W5maYh5Pd87mIaeOnyApM8w6ebQIEp8/ofMru0IsI5q1mi","message":"密码已重置","username":"...
```


### 失败接口详情


#### 1. POST /api/auth/login
- **状态码**: 401
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"Invalid username/email or password","data":null,"timestamp":"2025-09-20T17:13:07.588914"}
```


#### 2. POST /api/auth/register
- **状态码**: 400
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"Username already exists","data":null,"timestamp":"2025-09-20T17:13:07.615159"}
```


#### 3. POST /api/auth/change-password
- **状态码**: 400
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"User not found","data":null,"timestamp":"2025-09-20T17:13:07.636247"}
```


#### 4. PUT /api/users/profile
- **状态码**: 404
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"User not found","data":null,"timestamp":"2025-09-20T17:13:07.754747"}
```


#### 5. POST /api/users
- **状态码**: 409
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"Username already exists","data":null,"timestamp":"2025-09-20T17:13:07.836118"}
```


#### 6. GET /api/ai/health
- **状态码**: 500
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.845478"}
```


#### 7. GET /api/ai/metrics
- **状态码**: 500
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.852403"}
```


#### 8. GET /api/ai/metrics?timeRange=7d
- **状态码**: 500
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.85871"}
```


#### 9. POST /api/ai/analyze/project
- **状态码**: 500
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.865414"}
```


#### 10. POST /api/ai/analyze/weekly-report
- **状态码**: 500
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.871744"}
```


#### 11. GET /api/ai/analysis/1
- **状态码**: 500
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.877481"}
```


#### 12. POST /api/ai/generate-suggestions
- **状态码**: 500
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.882714"}
```


#### 13. GET /api/ai/project-insights/1
- **状态码**: 500
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"Internal server error","data":null,"timestamp":"2025-09-20T17:13:07.888031"}
```


#### 14. POST /api/weekly-reports
- **状态码**: 500
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"创建周报失败: could not execute statement [Unknown column 'template_id' in 'NEW'] [/* insert for com.weeklyreport.entity.WeeklyReport */insert into weekly_reports (additional_not...
```


#### 15. GET /api/weekly-reports/1
- **状态码**: 404
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"周报不存在","data":null,"timestamp":"2025-09-20T17:13:08.036701"}
```


#### 16. PUT /api/weekly-reports/1/submit
- **状态码**: 404
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"周报不存在","data":null,"timestamp":"2025-09-20T17:13:08.05528"}
```


#### 17. GET /api/projects/1
- **状态码**: 404
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"Project not found","data":null,"timestamp":"2025-09-20T17:13:08.122822"}
```


#### 18. PUT /api/projects/1/submit
- **状态码**: 404
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"Project not found","data":null,"timestamp":"2025-09-20T17:13:08.130095"}
```


#### 19. POST /api/tasks
- **状态码**: 500
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"Failed to create task: could not execute statement [Data truncated for column 'task_type' at row 1] [/* insert for com.weeklyreport.entity.Task */insert into tasks (actual_...
```


#### 20. GET /api/tasks/1
- **状态码**: 404
- **错误信息**: 无
- **响应内容**: 
```json
{"success":false,"message":"Task not found","data":null,"timestamp":"2025-09-20T17:13:08.198407"}
```


#### 21. GET /api/simple/hello
- **状态码**: 401
- **错误信息**: 无
- **响应内容**: 
```json
{"path":"/api/simple/hello","errorCode":"INSUFFICIENT_AUTHENTICATION","error":"Unauthorized","message":"Authentication failed: Full authentication is required to access this resource","timestamp":"202...
```


#### 22. POST /api/simple/fix-passwords
- **状态码**: 401
- **错误信息**: 无
- **响应内容**: 
```json
{"path":"/api/simple/fix-passwords","errorCode":"INSUFFICIENT_AUTHENTICATION","error":"Unauthorized","message":"Authentication failed: Full authentication is required to access this resource","timesta...
```


## 建议和问题

### 需要修复的问题


- **POST /api/auth/login**: 状态码401

- **POST /api/auth/register**: 状态码400

- **POST /api/auth/change-password**: 状态码400

- **PUT /api/users/profile**: 状态码404

- **POST /api/users**: 状态码409

- **GET /api/ai/health**: 状态码500

- **GET /api/ai/metrics**: 状态码500

- **GET /api/ai/metrics?timeRange=7d**: 状态码500

- **POST /api/ai/analyze/project**: 状态码500

- **POST /api/ai/analyze/weekly-report**: 状态码500

- **GET /api/ai/analysis/1**: 状态码500

- **POST /api/ai/generate-suggestions**: 状态码500

- **GET /api/ai/project-insights/1**: 状态码500

- **POST /api/weekly-reports**: 状态码500

- **GET /api/weekly-reports/1**: 状态码404

- **PUT /api/weekly-reports/1/submit**: 状态码404

- **GET /api/projects/1**: 状态码404

- **PUT /api/projects/1/submit**: 状态码404

- **POST /api/tasks**: 状态码500

- **GET /api/tasks/1**: 状态码404

- **GET /api/simple/hello**: 状态码401

- **POST /api/simple/fix-passwords**: 状态码401


### 安全建议

1. **DebugController安全风险**: /api/debug/* 接口暴露敏感信息，建议在生产环境中禁用或添加严格的权限控制
2. **CORS配置**: 检查跨域配置是否过于宽松
3. **错误信息**: 确保错误响应不泄露敏感系统信息

### 性能建议

1. **分页查询**: 确保所有列表接口都支持分页
2. **缓存策略**: 考虑为频繁查询的接口添加缓存
3. **查询优化**: 监控数据库查询性能

---
*测试报告由自动化测试工具生成*
