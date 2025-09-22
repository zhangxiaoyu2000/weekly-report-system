package com.weeklyreport.service;

import com.weeklyreport.entity.RoutineTask;
import com.weeklyreport.entity.User;
import com.weeklyreport.repository.RoutineTaskRepository;
import com.weeklyreport.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 日常性任务服务类
 */
@Service
@Transactional
public class RoutineTaskService {

    private static final Logger logger = LoggerFactory.getLogger(RoutineTaskService.class);

    @Autowired
    private RoutineTaskRepository routineTaskRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 创建日常任务
     */
    public RoutineTask createTask(String taskName, String taskDescription, 
                                 RoutineTask.TaskType taskType, Long createdById, Long assignedToId) {
        logger.info("Creating routine task: {} for user: {}", taskName, assignedToId);

        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new IllegalArgumentException("创建者不存在"));

        User assignedTo = assignedToId != null ? 
                userRepository.findById(assignedToId)
                        .orElseThrow(() -> new IllegalArgumentException("分配用户不存在")) : 
                createdBy;

        // 检查是否已存在同名任务
        if (routineTaskRepository.existsByAssignedToAndTaskNameIgnoreCase(assignedTo, taskName)) {
            throw new IllegalArgumentException("该用户已存在同名任务");
        }

        RoutineTask task = new RoutineTask(taskName, taskDescription, taskType, createdBy);
        task.setAssignedTo(assignedTo);

        RoutineTask savedTask = routineTaskRepository.save(task);
        logger.info("Routine task created successfully with ID: {}", savedTask.getId());
        
        return savedTask;
    }

    /**
     * 更新任务
     */
    public RoutineTask updateTask(Long taskId, String taskName, String taskDescription,
                                 RoutineTask.TaskType taskType, RoutineTask.Priority priority, 
                                 Long userId) {
        logger.info("Updating routine task: {} by user: {}", taskId, userId);

        RoutineTask task = routineTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 权限验证：只有创建者或分配对象可以修改
        if (!task.getCreatedBy().getId().equals(userId) && 
            !task.getAssignedTo().getId().equals(userId)) {
            throw new IllegalArgumentException("无权限修改此任务");
        }

        if (taskName != null) {
            task.setTaskName(taskName);
        }
        if (taskDescription != null) {
            task.setTaskDescription(taskDescription);
        }
        if (taskType != null) {
            task.setTaskType(taskType);
        }
        if (priority != null) {
            task.setPriority(priority);
        }

        RoutineTask updatedTask = routineTaskRepository.save(task);
        logger.info("Routine task updated successfully: {}", taskId);
        
        return updatedTask;
    }

    /**
     * 删除任务
     */
    public void deleteTask(Long taskId, Long userId) {
        logger.info("Deleting routine task: {} by user: {}", taskId, userId);

        RoutineTask task = routineTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        // 权限验证：只有创建者可以删除
        if (!task.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("无权限删除此任务");
        }

        routineTaskRepository.delete(task);
        logger.info("Routine task deleted successfully: {}", taskId);
    }

    /**
     * 完成任务
     */
    public RoutineTask completeTask(Long taskId, Long userId) {
        logger.info("Completing routine task: {} by user: {}", taskId, userId);

        RoutineTask task = routineTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        // 权限验证：只有分配对象可以完成任务
        if (!task.getAssignedTo().getId().equals(userId)) {
            throw new IllegalArgumentException("无权限完成此任务");
        }

        task.markAsCompleted();
        RoutineTask completedTask = routineTaskRepository.save(task);
        
        logger.info("Routine task completed successfully: {}", taskId);
        return completedTask;
    }

    /**
     * 激活任务
     */
    public RoutineTask activateTask(Long taskId, Long userId) {
        logger.info("Activating routine task: {} by user: {}", taskId, userId);

        RoutineTask task = routineTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        // 权限验证
        if (!task.getCreatedBy().getId().equals(userId) && 
            !task.getAssignedTo().getId().equals(userId)) {
            throw new IllegalArgumentException("无权限激活此任务");
        }

        task.setStatus(RoutineTask.TaskStatus.ACTIVE);
        task.setCompletedAt(null); // 重新激活时清除完成时间
        
        RoutineTask activatedTask = routineTaskRepository.save(task);
        logger.info("Routine task activated successfully: {}", taskId);
        
        return activatedTask;
    }

    /**
     * 获取用户的活跃任务
     */
    @Transactional(readOnly = true)
    public List<RoutineTask> getActiveTasksByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        return routineTaskRepository.findActiveTasksByUserOrderedByPriority(user);
    }

    /**
     * 获取用户的所有任务（分页）
     */
    @Transactional(readOnly = true)
    public Page<RoutineTask> getTasksByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        return routineTaskRepository.findByAssignedTo(user, pageable);
    }

    /**
     * 获取用户创建的任务（分页）
     */
    @Transactional(readOnly = true)
    public Page<RoutineTask> getTasksCreatedByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        return routineTaskRepository.findByCreatedBy(user, pageable);
    }

    /**
     * 根据ID获取任务
     */
    @Transactional(readOnly = true)
    public Optional<RoutineTask> getTaskById(Long taskId) {
        return routineTaskRepository.findById(taskId);
    }

    /**
     * 根据ID和用户获取任务（权限验证）
     */
    @Transactional(readOnly = true)
    public Optional<RoutineTask> getTaskByIdAndUser(Long taskId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        return routineTaskRepository.findByIdAndAssignedTo(taskId, user);
    }

    /**
     * 搜索任务
     */
    @Transactional(readOnly = true)
    public List<RoutineTask> searchTasks(String keyword) {
        return routineTaskRepository.findByTaskNameOrDescriptionContaining(keyword);
    }

    /**
     * 获取用户任务统计
     */
    @Transactional(readOnly = true)
    public TaskStatistics getUserTaskStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        Object[] stats = routineTaskRepository.getTaskStatisticsByUser(user);
        
        return new TaskStatistics(
                ((Number) stats[0]).longValue(), // activeTasks
                ((Number) stats[1]).longValue(), // completedTasks
                ((Number) stats[2]).longValue(), // inactiveTasks
                ((Number) stats[3]).longValue()  // totalTasks
        );
    }

    /**
     * 获取可用于周报的任务
     */
    @Transactional(readOnly = true)
    public List<RoutineTask> getTasksForWeeklyReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        return routineTaskRepository.findTasksForWeeklyReport(user);
    }

    /**
     * 查找即将到期的任务
     */
    @Transactional(readOnly = true)
    public List<RoutineTask> getOverdueOrNearDueTasks() {
        return routineTaskRepository.findOverdueOrNearDueTasks();
    }

    /**
     * 任务统计信息DTO
     */
    public static class TaskStatistics {
        private final long activeTasks;
        private final long completedTasks;
        private final long inactiveTasks;
        private final long totalTasks;

        public TaskStatistics(long activeTasks, long completedTasks, long inactiveTasks, long totalTasks) {
            this.activeTasks = activeTasks;
            this.completedTasks = completedTasks;
            this.inactiveTasks = inactiveTasks;
            this.totalTasks = totalTasks;
        }

        // Getters
        public long getActiveTasks() { return activeTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public long getInactiveTasks() { return inactiveTasks; }
        public long getTotalTasks() { return totalTasks; }
        
        public double getCompletionRate() {
            return totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        }
    }
}