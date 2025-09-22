# 构建和环境配置说明

## 环境变量配置

本项目支持三种环境配置，每种环境都有独立的配置文件和构建脚本。

### 环境文件

| 文件 | 环境 | 说明 |
|------|------|------|
| `.env.local` | 本地开发环境 | 使用127.0.0.1:8080，启用调试模式 |
| `.env.test` | 测试环境 | 使用test-api.weeklyreport.com，适中调试 |
| `.env.production` | 生产环境 | 使用api.weeklyreport.com，关闭调试 |

### 主要环境变量

| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| `VITE_API_BASE_URL` | API服务器地址 | `http://127.0.0.1:8080/api` |
| `VITE_APP_ENV` | 应用环境标识 | `development/test/production` |
| `VITE_APP_TITLE` | 应用标题 | `WeeklyReport 开发环境` |
| `VITE_DEBUG` | 调试模式 | `true/false` |
| `VITE_LOG_LEVEL` | 日志级别 | `debug/info/warn/error` |
| `VITE_API_TIMEOUT` | API超时时间 | `10000` (毫秒) |

## 构建脚本

### 开发环境脚本
```bash
npm run dev           # 默认开发模式
npm run dev:local     # 本地开发环境
npm run dev:test      # 测试环境开发
npm run dev:prod      # 生产环境开发
```

### 构建脚本
```bash
npm run build:local   # 本地环境构建（无TypeScript检查）
npm run build:test    # 测试环境构建（无TypeScript检查）
npm run build:prod    # 生产环境构建（无TypeScript检查）
npm run build:prod:strict  # 生产环境严格构建（含TypeScript检查）
```

### 预览脚本
```bash
npm run serve:local   # 预览本地环境构建
npm run serve:test    # 预览测试环境构建  
npm run serve:prod    # 预览生产环境构建
```

### 其他脚本
```bash
npm run lint          # 代码检查
npm run format        # 代码格式化
npm run type-check    # TypeScript类型检查
npm run clean         # 清理缓存
npm run clean:build   # 清理构建产物
npm run analyze       # 构建产物分析
```

## 环境切换

### 开发时环境切换
在开发过程中，可以通过运行不同的脚本来模拟不同环境：

```bash
# 开发本地环境
npm run dev:local

# 开发时连接测试服务器
npm run dev:test

# 开发时连接生产服务器（用于调试）
npm run dev:prod
```

### 部署时环境构建
部署时使用对应的构建脚本：

```bash
# 部署到测试服务器
npm run build:test

# 部署到生产服务器
npm run build:prod
```

## 注意事项

1. **TypeScript检查**: 为了快速构建，默认构建脚本跳过了TypeScript检查
2. **严格检查**: 如需进行严格的类型检查，使用 `build:prod:strict`
3. **环境隔离**: 每个环境的配置完全独立，避免交叉污染
4. **安全性**: 生产环境配置关闭了调试功能，启用了安全特性

## 环境变量优先级

Vite的环境变量加载优先级（从高到低）：
1. `.env.[mode].local`
2. `.env.local`
3. `.env.[mode]`
4. `.env`

其中 `[mode]` 对应构建时的 `--mode` 参数值。