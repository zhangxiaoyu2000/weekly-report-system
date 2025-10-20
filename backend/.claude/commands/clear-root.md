---
description: 清理项目根目录的所有临时文件
allowed-tools: Glob,Bash
---

清理项目根目录中的所有临时文件，包括测试脚本、日志文件、文本文件等。

执行以下步骤:

1. 使用Glob工具查找项目根目录中的以下文件类型:
   - `*.js` (JavaScript测试脚本)
   - `*.sh` (Shell测试脚本)
   - `*.py` (Python测试脚本)
   - `*.log` (日志文件)
   - `*.txt` (文本文件，如token文件、结果文件等)
   - `*.md` (Markdown文件，**排除README.md**)
   - `*.sql` (SQL脚本文件)
   - `*.json` (JSON数据文件，**排除package.json、package-lock.json等配置文件**)

2. 过滤规则:
   - 仅删除根目录文件，不影响src/、tests/、scripts/等子目录
   - **保留重要配置文件**: package.json, package-lock.json, pom.xml, README.md
   - **保留应用配置**: application.yml, application.properties
   - **保留构建文件**: Dockerfile, docker-compose.yml, Jenkinsfile

3. 列出找到的临时文件清单

4. 使用Bash工具批量删除这些文件

5. 确认删除操作完成

注意:
- 该命令用于清理开发过程中产生的临时文件和测试脚本
- 删除前会显示文件清单供确认
- 不会删除任何配置文件或项目结构文件
