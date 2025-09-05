/**
 * 日期工具函数
 */

/**
 * 格式化日期
 * @param {Date|string} date 日期
 * @param {string} format 格式化字符串
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, format = 'YYYY-MM-DD') {
  if (!date) return ''
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute)
    .replace('ss', second)
}

/**
 * 获取周的开始和结束日期
 * @param {Date|string} date 日期
 * @returns {Object} {start: Date, end: Date}
 */
export function getWeekRange(date = new Date()) {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1) // 调整到周一
  
  const start = new Date(d.setDate(diff))
  const end = new Date(d.setDate(diff + 6))
  
  return {
    start: new Date(start.getFullYear(), start.getMonth(), start.getDate()),
    end: new Date(end.getFullYear(), end.getMonth(), end.getDate())
  }
}

/**
 * 获取周数
 * @param {Date|string} date 日期
 * @returns {number} 第几周
 */
export function getWeekNumber(date = new Date()) {
  const d = new Date(date)
  const firstDay = new Date(d.getFullYear(), 0, 1)
  const pastDaysOfYear = (d - firstDay) / 86400000
  return Math.ceil((pastDaysOfYear + firstDay.getDay() + 1) / 7)
}

/**
 * 格式化相对时间
 * @param {Date|string} date 日期
 * @returns {string} 相对时间字符串
 */
export function formatRelativeTime(date) {
  if (!date) return ''
  
  const d = new Date(date)
  const now = new Date()
  const diff = now - d
  
  const second = 1000
  const minute = second * 60
  const hour = minute * 60
  const day = hour * 24
  const week = day * 7
  const month = day * 30
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前'
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前'
  } else if (diff < week) {
    return Math.floor(diff / day) + '天前'
  } else if (diff < month) {
    return Math.floor(diff / week) + '周前'
  } else {
    return formatDate(d, 'YYYY-MM-DD')
  }
}