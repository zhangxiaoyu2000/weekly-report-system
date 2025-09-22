## 部署流程
你先通过ssh了解当前测试服务器上面的接口情况
然后使用空余端口部署本项目

本地查看deploy.md -》 按照文档中过往经验修改 -》 git push到 gitea -》 通过Jenkinsfile  部署到 测试服务器 -》所有服务健康    |
             ｜
             ｜                                                      ｜
             ｜                                                       ｜
             -----------------------------------  使用printwrigt mcp去查看Jenkins控制台日志,错误原因，解决 方法写到deploy.md文档中

## 需要的资料

测试服务器：
    23.95.193.155
    root  To1YHvWPvyX157jf38
jenkins:
    http://23.95.193.155:12088/
    用户名：zhangxiaoyu  密码:2049251148
gitea:  
    http://23.95.193.155:12300/zhangxiaoyu/WeeklyReport.git
    用户名：zhangxiaoyu  密码:2049251148abcZY

## 重点
不要回退版本，就用这个版本，不要想去拉取gitea中之前的版本

## 需要部署的服务有
mysql+

## 部署回滚测试
部署过程中使用playwright mcp去查看Jenkins控制台，来查看构建部署情况，若是失败就查看构建日志，然后回滚修改提交
若是构建成功但是镜像容器启动失败可以使用ssh登录到服务器，使用docker logs命令去查看日志，并反馈回滚
并且在回滚纠错和修复的过程补充已知问题和修复

## 已知问题和修复


## 部署最佳实践和经验总结