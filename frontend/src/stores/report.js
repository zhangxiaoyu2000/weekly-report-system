import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useReportStore = defineStore('report', () => {
  // State
  const reports = ref([])
  const currentReport = ref(null)
  const loading = ref(false)
  const total = ref(0)

  // Getters
  const reportCount = computed(() => reports.value.length)
  const weeklyReports = computed(() => 
    reports.value.filter(report => report.type === 'weekly')
  )
  const monthlyReports = computed(() => 
    reports.value.filter(report => report.type === 'monthly')
  )

  // Actions
  function setReports(newReports) {
    reports.value = newReports
  }

  function addReport(report) {
    reports.value.unshift(report)
    total.value += 1
  }

  function updateReport(updatedReport) {
    const index = reports.value.findIndex(r => r.id === updatedReport.id)
    if (index !== -1) {
      reports.value[index] = updatedReport
    }
  }

  function removeReport(reportId) {
    const index = reports.value.findIndex(r => r.id === reportId)
    if (index !== -1) {
      reports.value.splice(index, 1)
      total.value -= 1
    }
  }

  function setCurrentReport(report) {
    currentReport.value = report
  }

  function setLoading(status) {
    loading.value = status
  }

  function setTotal(count) {
    total.value = count
  }

  function clearReports() {
    reports.value = []
    currentReport.value = null
    total.value = 0
  }

  return {
    // State
    reports,
    currentReport,
    loading,
    total,
    // Getters
    reportCount,
    weeklyReports,
    monthlyReports,
    // Actions
    setReports,
    addReport,
    updateReport,
    removeReport,
    setCurrentReport,
    setLoading,
    setTotal,
    clearReports
  }
})