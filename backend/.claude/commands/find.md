---
description: 查找项目中的某个元素，包括变量，文件，其中这个命令有三个参数,第一个参数是查找对象,第二个参数是查询到做什么，可以省略，默认是返回位置 第三个参数是查找路径，对于项目的相对路径，第三个参数可以省略，若省略就是从项目的根目录进行查询
argument-hint: <查找对象> [操作] [查找路径]
allowed-tools: Grep,Glob,Read,Bash
---

# 查找命令

**查找对象**: $1 (必需)
**操作**: $2 (可选，默认: 返回位置)
**查找路径**: $3 (可选，默认: 项目根目录)

## 执行步骤

### 1. 解析参数

**查找对象** (`$1`):
- 必填参数
- 可以是变量名、类名、方法名、文件名或其他代码元素

**操作** (`$2`):
- 可选参数，默认为 "返回位置"
- 可选值:
  - `location` / `位置` / 留空: 返回所有匹配位置
  - `read` / `读取`: 显示匹配项的完整内容
  - `count` / `统计`: 统计出现次数
  - `references` / `引用`: 查找所有引用位置
  - `definition` / `定义`: 查找定义位置

**查找路径** (`$3`):
- 可选参数，默认为项目根目录
- 相对于项目根目录的路径
- 示例: `src/main/java`, `src/test`, `.`

### 2. 确定搜索策略

根据查找对象的特征选择合适的工具:

**文件名查找** (包含路径分隔符或文件扩展名):
```
使用 Glob 工具:
- 模式: **/*$1* 或精确文件名
- 快速定位文件
```

**代码元素查找** (变量、类、方法等):
```
使用 Grep 工具:
- 模式: 智能构建正则表达式
- Java: class $1, interface $1, void $1(, $1 =
- 搜索范围: 基于 $3 参数或默认全项目
- 排除: target/, node_modules/, .git/
```

### 3. 执行搜索

#### 默认操作: 返回位置
```
使用 Grep 的 files_with_matches 模式:
- 仅返回包含匹配的文件路径
- 使用 -n 显示行号
- 使用 -C 显示上下文 (如果需要)
```

#### 读取操作
```
1. 先使用 Grep 找到所有匹配位置
2. 对每个匹配文件使用 Read 工具
3. 显示匹配行的上下文 (前后5行)
```

#### 统计操作
```
使用 Grep 的 count 模式:
- 统计每个文件中的匹配次数
- 汇总总计数
```

#### 引用查找
```
1. 搜索所有提及 $1 的位置
2. 排除定义本身
3. 按文件分组显示
```

#### 定义查找
```
针对不同语言构建精确的定义模式:
- Java: public/private/protected class/interface/enum $1
- Java方法: .* $1\s*\(
- 变量定义: (var|let|const|Type) $1\s*[=;]
```

### 4. 格式化输出

#### 位置输出格式
```
📁 查找结果: $1

📍 src/main/java/com/example/Service.java:45
📍 src/test/java/com/example/ServiceTest.java:23

总计: 2 个位置
```

#### 读取输出格式
```
📁 查找结果: $1 (含内容)

📄 src/main/java/com/example/Service.java:45
───────────────────────────────────────
[显示上下文代码]
───────────────────────────────────────

📄 src/test/java/com/example/ServiceTest.java:23
───────────────────────────────────────
[显示上下文代码]
───────────────────────────────────────
```

#### 统计输出格式
```
📊 统计结果: $1

📄 src/main/java/com/example/Service.java: 15 次
📄 src/test/java/com/example/ServiceTest.java: 8 次

总计: 23 次
```

### 5. 智能优化

**自动识别搜索类型**:
- 包含 `.java`, `.xml`, `.yml` 等扩展名 → 文件搜索
- 大写开头 (CamelCase) → 类名搜索
- 小写开头 → 变量/方法名搜索
- 包含路径分隔符 `/` → 路径搜索

**搜索范围优化**:
- 默认排除: `target/`, `node_modules/`, `.git/`, `logs/`, `*.class`
- Java项目优先搜索: `src/main/java/`, `src/test/java/`
- 配置文件优先搜索: `src/main/resources/`

**性能优化**:
- 文件数量 > 1000 → 提示缩小搜索范围
- 使用 Glob 预过滤文件类型
- 大文件 (>1MB) 提示可能需要更长时间

## 示例用法

### 示例 1: 查找类定义位置
```bash
/find UserService
# 在整个项目中查找 UserService 类
```

### 示例 2: 查找变量并读取上下文
```bash
/find jdbcTemplate read
# 查找 jdbcTemplate 变量的所有使用位置并显示代码
```

### 示例 3: 在指定目录查找
```bash
/find @Autowired 位置 src/main/java/com/weeklyreport/service
# 在 service 目录中查找 @Autowired 注解
```

### 示例 4: 统计方法调用次数
```bash
/find saveWeeklyReport 统计
# 统计 saveWeeklyReport 方法被调用的次数
```

### 示例 5: 查找文件
```bash
/find application.yml
# 查找 application.yml 配置文件
```

### 示例 6: 查找引用
```bash
/find WeeklyReport 引用 src/main/java
# 查找 WeeklyReport 类的所有引用位置
```

## 注意事项

1. **性能考虑**: 在大型项目中，建议指定搜索路径以提高速度
2. **精确匹配**: 使用引号包裹多词查询，如: `/find "public static void"`
3. **正则支持**: 查找对象支持正则表达式模式
4. **大小写**: 默认大小写敏感，可使用 `-i` 标志忽略大小写
5. **排除文件**: 自动排除 target, node_modules, .git 等目录

## 实现逻辑

现在开始执行查找操作:

1. 检查 $1 是否提供，否则报错
2. 设置默认值: 操作=$2 或 "location", 路径=$3 或 "."
3. 智能识别查找类型 (文件/代码元素)
4. 选择合适的搜索工具和策略
5. 执行搜索
6. 格式化并输出结果
7. 提供优化建议 (如果结果过多或过少)
