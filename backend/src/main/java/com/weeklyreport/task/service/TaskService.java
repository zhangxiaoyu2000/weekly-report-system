package com.weeklyreport.task.service;

import com.weeklyreport.task.entity.Task;
import com.weeklyreport.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Task operations
 */
@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Save a task
     */
    public Task saveTask(Task task) {
        // Update overdue status before saving
        task.updateOverdueStatus();
        return taskRepository.save(task);
    }

    /**
     * Find task by ID
     */
    @Transactional(readOnly = true)
    public Optional<Task> findTaskById(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * Find all tasks by user
     */
    @Transactional(readOnly = true)
    public List<Task> findTasksByUser(Long userId) {
        return taskRepository.findByCreatedByOrderByCreatedAtDesc(userId);
    }

    // taskType 字段已移除，相关方法已删除

    /**
     * Find tasks by user
     */
    @Transactional(readOnly = true)
    public Page<Task> findTasksByUserId(Long userId, Pageable pageable) {
        return taskRepository.findTasksByUserId(userId, pageable);
    }

    /**
     * Find tasks by user and expected results defined
     */
    @Transactional(readOnly = true)
    public List<Task> findTasksByUserIdAndExpectedResults(Long userId) {
        return taskRepository.findTasksByUserIdAndExpectedResults(userId);
    }

    /**
     * Find incomplete tasks
     */
    @Transactional(readOnly = true)
    public List<Task> findIncompleteTasks() {
        return taskRepository.findIncompleteTasks();
    }

    /**
     * Find defined tasks by date range
     */
    @Transactional(readOnly = true)
    public List<Task> findDefinedTasksByDateRange(LocalDate startDate, LocalDate endDate) {
        return taskRepository.findDefinedTasksByDateRange(startDate, endDate);
    }

    // findTasksDueSoon 方法已移除，因为不再基于 actualResults 字段

    /**
     * Update task
     */
    public Task updateTask(Task task) {
        if (!taskRepository.existsById(task.getId())) {
            throw new IllegalArgumentException("任务不存在，ID: " + task.getId());
        }
        
        // Update overdue status before saving
        task.updateOverdueStatus();
        return taskRepository.save(task);
    }

    /**
     * Update task progress
     */
    public Task updateTaskProgress(Long taskId, Integer progress) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("任务不存在，ID: " + taskId);
        }
        
        Task task = taskOpt.get();
        task.setProgress(progress);
        return taskRepository.save(task);
    }

    /**
     * Delete task
     */
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new IllegalArgumentException("任务不存在，ID: " + taskId);
        }
        taskRepository.deleteById(taskId);
    }

    /**
     * Create task - 不再需要 taskType 参数
     */
    public Task createTask(String taskName, Long createdBy) {
        Task task = new Task(taskName, createdBy);
        return saveTask(task);
    }

    /**
     * Task statistics inner class
     */
    public static class TaskStatistics {
        private final int completedCount;
        private final int pendingCount;
        private final int overdueCount;

        public TaskStatistics(int completedCount, int pendingCount, int overdueCount) {
            this.completedCount = completedCount;
            this.pendingCount = pendingCount;
            this.overdueCount = overdueCount;
        }

        public int getCompletedCount() {
            return completedCount;
        }

        public int getPendingCount() {
            return pendingCount;
        }

        public int getOverdueCount() {
            return overdueCount;
        }

        public int getTotalCount() {
            return completedCount + pendingCount + overdueCount;
        }

        public double getCompletionRate() {
            int total = getTotalCount();
            return total > 0 ? (double) completedCount / total * 100 : 0.0;
        }
    }
}