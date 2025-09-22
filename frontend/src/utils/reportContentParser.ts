/**
 * 周报内容解析工具
 * 将基于任务生成的Markdown内容解析为传统的工作总结字段
 */

export interface ParsedReportContent {
  workSummary: string        // 工作总结
  achievements: string       // 主要成就  
  challenges: string         // 面临挑战
  nextWeekPlan: string       // 下周计划
  reflection: string         // 个人反思
  improvements: string       // 改进建议
}

/**
 * 解析基于任务的周报内容
 */
export function parseReportContent(content: string): ParsedReportContent {
  if (!content || content.trim() === '') {
    return {
      workSummary: '无',
      achievements: '无',
      challenges: '无', 
      nextWeekPlan: '无',
      reflection: '无',
      improvements: '无'
    }
  }

  try {
    // 初始化结果
    const result: ParsedReportContent = {
      workSummary: '',
      achievements: '',
      challenges: '',
      nextWeekPlan: '',
      reflection: '',
      improvements: ''
    }

    // 按行分割内容
    const lines = content.split('\n')
    let currentSection = ''
    let currentTaskType = ''
    let currentContent: string[] = []

    // 用于收集不同类型的任务
    const routineTasks: string[] = []
    const developmentTasks: string[] = []
    const nextWeekRoutineTasks: string[] = []
    const nextWeekDevTasks: string[] = []

    for (const line of lines) {
      const trimmedLine = line.trim()

      // 识别主要章节
      if (trimmedLine === '## 本周已完成工作') {
        currentSection = 'thisWeek'
        continue
      } else if (trimmedLine === '## 下周工作规划') {
        currentSection = 'nextWeek'
        continue
      }

      // 识别任务类型
      if (trimmedLine === '### 日常性任务') {
        currentTaskType = currentSection === 'thisWeek' ? 'routine' : 'nextWeekRoutine'
        continue
      } else if (trimmedLine === '### 发展性任务') {
        currentTaskType = currentSection === 'thisWeek' ? 'development' : 'nextWeekDevelopment'
        continue
      }

      // 收集任务内容
      if (trimmedLine.match(/^\d+\.\s\*\*(.*)\*\*$/)) {
        // 任务标题行
        const taskName = trimmedLine.replace(/^\d+\.\s\*\*(.*)\*\*$/, '$1')
        currentContent = [taskName]
      } else if (trimmedLine.match(/^\s*-\s*(.*):(.*)$/)) {
        // 任务详情行
        const detail = trimmedLine.replace(/^\s*-\s*/, '').trim()
        currentContent.push(detail)
      } else if (currentContent.length > 0 && (
        trimmedLine.startsWith('###') || 
        trimmedLine.startsWith('##') ||
        lines.indexOf(line) === lines.length - 1
      )) {
        // 结束当前任务的收集
        const taskText = currentContent.join('\n')
        
        switch (currentTaskType) {
          case 'routine':
            routineTasks.push(taskText)
            break
          case 'development':
            developmentTasks.push(taskText)
            break
          case 'nextWeekRoutine':
            nextWeekRoutineTasks.push(taskText)
            break
          case 'nextWeekDevelopment':
            nextWeekDevTasks.push(taskText)
            break
        }
        currentContent = []
      }
    }

    // 生成工作总结
    if (routineTasks.length > 0 || developmentTasks.length > 0) {
      result.workSummary = `本周完成了${routineTasks.length}项日常性任务和${developmentTasks.length}项发展性任务。`
      
      if (routineTasks.length > 0) {
        result.workSummary += '\n\n**日常性工作：**\n' + routineTasks.map((task, i) => `${i + 1}. ${task.split('\n')[0]}`).join('\n')
      }
    } else {
      result.workSummary = '本周工作内容暂无详细记录'
    }

    // 生成主要成就
    if (developmentTasks.length > 0) {
      result.achievements = '**主要发展性成果：**\n' + 
        developmentTasks.map((task, i) => {
          const lines = task.split('\n')
          const taskName = lines[0]
          const actualResult = lines.find(l => l.includes('实际结果:'))?.replace(/.*实际结果:\s*/, '') || ''
          return `${i + 1}. ${taskName}${actualResult ? '\n   成果：' + actualResult : ''}`
        }).join('\n')
    } else if (routineTasks.length > 0) {
      result.achievements = `成功完成${routineTasks.length}项日常性任务，保持了工作的连续性和稳定性。`
    } else {
      result.achievements = '本周成果暂无详细记录'
    }

    // 生成面临挑战（从任务的结果差异分析中提取）
    const challenges: string[] = []
    const allTasks = routineTasks.concat(developmentTasks)
    allTasks.forEach(task => {
      const lines = task.split('\n')
      const diffLine = lines.find(l => l.includes('结果差异分析:'))
      const diffAnalysis = diffLine ? diffLine.replace(/.*结果差异分析:\s*/, '') : ''
      if (diffAnalysis && diffAnalysis.trim() !== '' && diffAnalysis !== '无') {
        challenges.push(diffAnalysis)
      }
    })
    
    result.challenges = challenges.length > 0 ? 
      '**主要挑战：**\n' + challenges.map((c, i) => `${i + 1}. ${c}`).join('\n') : 
      '本周工作进展顺利，未遇到重大挑战'

    // 生成下周计划
    if (nextWeekRoutineTasks.length > 0 || nextWeekDevTasks.length > 0) {
      result.nextWeekPlan = `下周计划开展${nextWeekRoutineTasks.length}项日常性任务和${nextWeekDevTasks.length}项发展性任务。`
      
      if (nextWeekDevTasks.length > 0) {
        result.nextWeekPlan += '\n\n**重点发展性工作：**\n' + 
          nextWeekDevTasks.map((task, i) => {
            const taskName = task.split('\n')[0]
            const expectedResult = task.split('\n').find(l => l.includes('预期结果:'))?.replace(/.*预期结果:\s*/, '')
            return `${i + 1}. ${taskName}${expectedResult ? '\n   目标：' + expectedResult : ''}`
          }).join('\n')
      }
      
      if (nextWeekRoutineTasks.length > 0) {
        result.nextWeekPlan += '\n\n**日常性工作：**\n' + 
          nextWeekRoutineTasks.map((task, i) => `${i + 1}. ${task.split('\n')[0]}`).join('\n')
      }
    } else {
      result.nextWeekPlan = '下周工作计划暂无详细安排'
    }

    // 生成个人反思和改进建议（基于任务完成情况）
    const totalTasks = routineTasks.length + developmentTasks.length
    const allTasksForAnalysis = routineTasks.concat(developmentTasks)
    const completedTasks = allTasksForAnalysis.filter(task => 
      task.includes('实际结果:') && !task.includes('实际结果: 无')
    ).length

    if (totalTasks > 0) {
      const completionRate = Math.round((completedTasks / totalTasks) * 100)
      result.reflection = `本周任务完成率${completionRate}%（${completedTasks}/${totalTasks}项）。${
        completionRate >= 80 ? '工作进展良好，按计划推进。' : 
        completionRate >= 60 ? '工作进展正常，部分任务需要加强。' : 
        '工作进展需要改善，建议优化时间管理。'
      }`
      
      result.improvements = completionRate < 80 ? 
        '建议：1. 优化时间分配\n2. 提前识别任务风险\n3. 加强任务优先级管理' :
        '继续保持当前工作节奏，注意任务质量的持续提升。'
    } else {
      result.reflection = '本周工作记录较少，建议完善任务规划和记录。'
      result.improvements = '建议建立更详细的任务管理和记录机制。'
    }

    return result
  } catch (error) {
    console.error('解析周报内容失败:', error)
    return {
      workSummary: content.substring(0, 200) + '...',
      achievements: '内容解析中出现问题，请查看原始内容',
      challenges: '无法解析',
      nextWeekPlan: '无法解析', 
      reflection: '无法解析',
      improvements: '无法解析'
    }
  }
}

/**
 * 简化版本的内容解析，直接基于原始content进行分段
 */
export function parseSimpleContent(content: string): ParsedReportContent {
  if (!content) {
    return {
      workSummary: '暂无内容',
      achievements: '暂无记录',
      challenges: '暂无记录',
      nextWeekPlan: '暂无规划',
      reflection: '暂无反思',
      improvements: '暂无建议'
    }
  }

  // 基于Markdown结构进行分段
  const sections = content.split('##').filter(s => s.trim())
  
  let workSummary = ''
  let nextWeekPlan = ''

  sections.forEach(section => {
    const sectionContent = section.trim()
    if (sectionContent.startsWith('本周已完成工作')) {
      workSummary = sectionContent.replace('本周已完成工作', '').trim()
    } else if (sectionContent.startsWith('下周工作规划')) {
      nextWeekPlan = sectionContent.replace('下周工作规划', '').trim()
    }
  })

  return {
    workSummary: workSummary || content.substring(0, 200) + (content.length > 200 ? '...' : ''),
    achievements: workSummary ? extractAchievements(workSummary) : '从本周完成的工作中提取',
    challenges: '基于任务执行情况分析，详见工作总结',
    nextWeekPlan: nextWeekPlan || '详见原始内容',
    reflection: '基于本周任务完成情况的综合分析',
    improvements: '建议参考任务执行结果进行持续改进'
  }
}

/**
 * 从工作内容中提取成就
 */
function extractAchievements(workContent: string): string {
  // 提取发展性任务作为主要成就
  const devTaskMatches = workContent.match(/### 发展性任务([\s\S]*?)(?=###|$)/g)
  if (devTaskMatches && devTaskMatches.length > 0) {
    return devTaskMatches[0].replace('### 发展性任务', '').trim()
  }
  
  // 如果没有发展性任务，提取所有完成的任务
  const taskMatches = workContent.match(/\d+\.\s\*\*(.*?)\*\*/g)
  if (taskMatches && taskMatches.length > 0) {
    return '本周完成的主要工作：\n' + taskMatches.map(task => 
      '• ' + task.replace(/\d+\.\s\*\*(.*?)\*\*/, '$1')
    ).join('\n')
  }
  
  return '请查看详细工作内容'
}