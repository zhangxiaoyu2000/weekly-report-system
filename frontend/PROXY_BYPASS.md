# 代理绕过配置说明

## 问题描述

在本地开发环境中，如果系统开启了代理（如Privoxy、Shadowsocks等），可能会导致前端请求本地后端服务时出现以下问题：

- `ERR_CONNECTION_REFUSED` 错误
- `TypeError: Failed to fetch` 错误
- 请求超时或响应缓慢
- 代理服务器错误页面

## 解决方案

本项目已经内置了代理绕过配置，通过 `.env.local` 文件中的环境变量来优化本地开发体验。

### 代理绕过环境变量

在 `.env.local` 文件中已配置以下变量：

```bash
# 代理配置 (本地开发专用)
VITE_BYPASS_PROXY=true                  # 启用代理绕过优化
VITE_USE_IP_INSTEAD_LOCALHOST=true      # 使用IP地址而不是localhost
VITE_DIRECT_CONNECTION=true             # 启用直连模式
VITE_DISABLE_CACHE=true                 # 禁用请求缓存避免代理缓存
VITE_FORCE_LOCALHOST_BYPASS=true        # 强制绕过localhost代理

# 本地服务器配置
VITE_LOCAL_API_HOST=127.0.0.1          # 本地API服务器IP
VITE_LOCAL_API_PORT=8080                # 本地API服务器端口
```

### 工作原理

当这些配置启用后，前端会：

1. **使用IP地址**: 用`127.0.0.1`替代`localhost`，绕过某些代理的localhost处理
2. **添加防缓存头部**: 添加`Cache-Control`、`Pragma`、`Expires`头部
3. **禁用请求缓存**: 设置`cache: 'no-store'`避免浏览器和代理缓存
4. **优化超时时间**: 使用环境变量控制的超时时间

### 如何使用

#### 方法1: 使用默认配置（推荐）
直接使用项目，默认的`.env.local`配置已经优化了代理绕过：

```bash
npm run dev:local
```

#### 方法2: 自定义配置
如果默认配置不能解决你的代理问题，可以修改`.env.local`中的配置：

```bash
# 如果你的后端运行在不同端口
VITE_LOCAL_API_PORT=8081

# 如果你需要使用不同的IP
VITE_LOCAL_API_HOST=192.168.1.100

# 如果你想完全禁用代理绕过
VITE_BYPASS_PROXY=false
```

#### 方法3: 运行时测试
你可以在浏览器控制台检查当前使用的API地址：

```javascript
console.log('当前API基础URL:', import.meta.env.VITE_API_BASE_URL)
console.log('代理绕过状态:', import.meta.env.VITE_BYPASS_PROXY)
```

### 常见问题排查

#### 问题1: 仍然出现连接错误
```bash
# 确保后端服务正在运行
curl http://127.0.0.1:8080/api/health

# 检查代理设置
echo $http_proxy $https_proxy
```

#### 问题2: 请求仍然很慢
检查`.env.local`中的配置：
```bash
VITE_API_TIMEOUT=5000  # 减少超时时间
VITE_DISABLE_CACHE=true
```

#### 问题3: 某些请求正常，某些请求失败
这可能是因为某些组件还在使用硬编码的URL，我们已经更新了主要的API配置文件。

### 环境切换

当你需要在不同环境之间切换时：

```bash
# 本地开发（启用代理绕过）
npm run dev:local

# 测试环境（使用远程API）
npm run dev:test

# 生产环境预览
npm run dev:prod
```

### 调试技巧

如果仍有问题，可以在浏览器控制台查看详细的网络日志：

1. 打开浏览器开发者工具
2. 查看Network标签页
3. 观察失败的请求详情
4. 检查是否使用了正确的URL和头部

通过这套配置，你可以在保持代理开启的情况下正常进行本地开发。