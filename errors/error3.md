
实际周报请求数据结构：
{"title":"22",
"reportWeek":"2025-09-15",  
"content":"# 本周工作汇报\n\n## 本周已完成工作\n\n### 日常性任务\n\n1. **AI智能分析模块功能升级**\n   - 关键指标: 模型准确率提升15%，响应速度提升30%，用户满意度>85%\n   - 实际结果: 222\n   - 结果差异分析: 222\n\n### 发展性任务\n\n1. **需求分析与系统设计阶段**\n   - 实际结果: 222\n   - 结果差异分析: 222",
"workSummary":"",
"achievements":"",
challenges":"",
"nextWeekPlan":"",
"additionalNotes":"22222",
developmentOpportunities":"22222",
"priority":2,
"projectId":null,
"templateId":null
}

错误的地方有
"reportWeek":"2025-09-15",  前端应该是硬编码了，这里应该是当天

"workSummary":"",
"achievements":"",
challenges":"",
"nextWeekPlan":"",   这些字段都是多余的


"priority":2,  优先级字段也是多余的


我期望的传输的结构是：

{
 "userid":""   # 提交周报的用户id    
"title":"22",      # 这是周报标题
"reportWeek":"几月第几周（周几）",  # 周报日期
content:{               # 本周汇报内容 
  Routine_tasks:[    #  日常性任务
    {
      task_id:"",        # 对应任务表中的日常性任务的id 外键
      actual_result:""，       # 实际结果
      AnalysisofResultDifferences:""。   # 结果差异分析
    }
  ],
  Developmental_tasks:[        #  发展性任务
    {
      project_id:"",      # 对应项目表中的项目id。外键
      phase_id:"",        #   对应该项目的某个阶段的id  外键
      actual_result:""，   #  实际结果
      AnalysisofResultDifferences:""    # 结果差异分析
    }
  ]
}
"nextWeekPlan":{      # 下周规划
   Routine_tasks:[    #  日常性任务
    {
      task_id:"",     # 对应任务表中的日常性任务的id 外键
    }
  ],
  Developmental_tasks:[      #  发展性任务
    {
      project_id:"",    # 对应项目表中的项目id  外键
      phase_id:"",      #   对应该项目的某个阶段的id   外键
    }
  ]
},
"additionalNotes":"22222",    # 其他备注
"developmentOpportunities":"22222",   # 可发展性清单
}

 


执行步骤：

需要去除的表有：
departments 表
flyway_schema_history 表
simple_weekly_reports 表
task_templates 表
templates 表
comments 表

simple_projects 表更名为。projects


最终就保留一下几张表：

ai_analysis_results   
  AI分析结果表
projects
  项目表，需要包括项目的基础信息，除此之外需要有一个 AI分析结果id（外键对应ai_analysis_results的主键）  管理员审批人ID  超级管理员审批人ID 拒绝理由 审批状态
project_phases
  项目阶段表
tasks
  任务表（包括日常性任务和发展项目中的阶段性任务） 因为两者之间有很多相同的字段，还需要添加外键一个字段来关联项目
users
  用户表
weekly_reports
  周报表，需要保留上述周报的数据,除此之外需要有一个 AI分析结果id（外键对应ai_analysis_results的主键）  管理员审批人ID  拒绝理由 审批状态



数据库结构：
# AI分析结果表
ai_analysis_results:{
          # 字段保持原样
}
# 存储项目
projects:{
          #项目ID
          #项目名称 
          #项目内容
          #项目成员
          #预期结果
          #时间线
          #止损点
          #阶段任务id
}
# 存储项目阶段性任务
project_phases:{         
          #任务ID
          #任务名称
          #阶段描述
          #负责成员
          #时间安排
          #预期结果
          #关联项目ID
          #实际结果
          #结果差异分析
}
# 存储日常性任务
tasks:{
          #任务ID
          #任务名称
          #人员分配
          #时间线
          #量化指标
          #预期结果
          #实际结果
          #结果差异分析
}
# 用户表
users:{   
          #用户ID
          #用户名
          #邮箱
          #密码
          #角色
          #状态

}

# 日常性任务与周报的关联表
task_reports:{
          #周报ID 主键
          #任务ID 主键
}

# 发展性项目的阶段性任务与周报关联表：
dev_task_reports:{
          #项目ID
          #任务ID
          #周报ID
}

# 周报表
weekly_reports:{
          # 周报ID
          # 提交周报的用户ID
          # 周报标题
          # 其他备注
          # 可发展性清单
}



后端实现工作流：

项目管理模块：
  管理员通过http://localhost:3005/app/projects/create 页面填写基本的