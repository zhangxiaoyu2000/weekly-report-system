package com.weeklyreport.controller;

import com.weeklyreport.dto.ApiResponse;
import com.weeklyreport.dto.task.TaskCreateRequest;
import com.weeklyreport.dto.task.TaskResponse;
import com.weeklyreport.entity.Task;
import com.weeklyreport.entity.User;
import com.weeklyreport.entity.TaskReport;
import com.weeklyreport.entity.DevTaskReport;
import com.weeklyreport.repository.TaskRepository;
import com.weeklyreport.repository.TaskReportRepository;
import com.weeklyreport.repository.DevTaskReportRepository;
import com.weeklyreport.service.UserService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 任务管理控制器 - 严格按照CLAUDE.md要求
 * 
 * 业务流程：
 * 1. 主管创建任务 → 添加到数据库
 * 2. 主管可以查看自己创建的任务
 * 3. 支持CRUD操作
 */
@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "*")
public class TaskController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskReportRepository taskReportRepository;

    @Autowired
    private DevTaskReportRepository devTaskReportRepository;

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("用户未认证");
        }
        String username = auth.getName();
        return userService.getUserProfile(username);
    }

    /**
     * 获取所有任务列表（分页）
     * GET /tasks
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<Task> taskPage;
            User currentUser = getCurrentUser();
            
            // 主管只能看到自己创建的任务，管理员和超级管理员可以看到所有任务
            if (currentUser.getRole() == User.Role.MANAGER) {
                taskPage = taskRepository.findByCreatedBy(currentUser.getId(), pageable);
            } else {
                taskPage = taskRepository.findAll(pageable);
            }
            
            Page<TaskResponse> responsePage = taskPage.map(TaskResponse::new);
            
            return ResponseEntity.ok(ApiResponse.success(responsePage));
        } catch (Exception e) {
            logger.error("Error getting tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get tasks: " + e.getMessage()));
        }
    }

    /**
     * 创建新任务（主管权限）
     * POST /tasks
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskCreateRequest request) {
        
        try {
            User currentUser = getCurrentUser();
            
            // 创建任务实体
            Task task = new Task();
            task.setTaskName(request.getTaskName());
            task.setPersonnelAssignment(request.getPersonnelAssignment());
            task.setTimeline(request.getTimeline());
            task.setExpectedResults(request.getExpectedResults());
            // TaskType字段已删除，不再设置
            task.setCreatedBy(currentUser.getId()); // 自动设置为当前用户
            
            Task savedTask = taskRepository.save(task);
            logger.info("Task created: {} by user: {}", savedTask.getId(), currentUser.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(new TaskResponse(savedTask)));
                
        } catch (Exception e) {
            logger.error("Error creating task", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create task: " + e.getMessage()));
        }
    }

    /**
     * 获取单个任务详情
     * GET /tasks/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(@PathVariable Long id) {
        
        try {
            Optional<Task> taskOpt = taskRepository.findById(id);
            if (taskOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Task not found"));
            }
            
            Task task = taskOpt.get();
            User currentUser = getCurrentUser();
            
            // 权限检查：管理员和超级管理员可以查看所有任务，主管只能查看自己创建的任务
            if (currentUser.getRole() == User.Role.MANAGER && 
                !task.getCreatedBy().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
            }
            
            return ResponseEntity.ok(ApiResponse.success(new TaskResponse(task)));
            
        } catch (Exception e) {
            logger.error("Error getting task: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get task: " + e.getMessage()));
        }
    }

    /**
     * 更新任务（仅创建者可更新）
     * PUT /tasks/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskCreateRequest request) {
        
        try {
            Optional<Task> taskOpt = taskRepository.findById(id);
            if (taskOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Task not found"));
            }
            
            Task task = taskOpt.get();
            User currentUser = getCurrentUser();
            
            // 只有创建者可以更新
            if (!task.getCreatedBy().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only task creator can update"));
            }
            
            // 更新字段
            task.setTaskName(request.getTaskName());
            task.setPersonnelAssignment(request.getPersonnelAssignment());
            task.setTimeline(request.getTimeline());
            task.setExpectedResults(request.getExpectedResults());
            // TaskType字段已删除，不再设置
            
            Task savedTask = taskRepository.save(task);
            logger.info("Task updated: {} by user: {}", savedTask.getId(), currentUser.getId());
            
            return ResponseEntity.ok(ApiResponse.success(new TaskResponse(savedTask)));
            
        } catch (Exception e) {
            logger.error("Error updating task: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update task: " + e.getMessage()));
        }
    }

    /**
     * 删除任务（仅创建者可删除）
     * DELETE /tasks/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        
        try {
            Optional<Task> taskOpt = taskRepository.findById(id);
            if (taskOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Task not found"));
            }
            
            Task task = taskOpt.get();
            User currentUser = getCurrentUser();
            
            // 只有创建者可以删除
            if (!task.getCreatedBy().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only task creator can delete"));
            }
            
            taskRepository.delete(task);
            logger.info("Task deleted: {} by user: {}", id, currentUser.getId());
            
            return ResponseEntity.ok(ApiResponse.success(null));
            
        } catch (Exception e) {
            logger.error("Error deleting task: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete task: " + e.getMessage()));
        }
    }

    /**
     * 获取用户创建的任务列表
     * GET /tasks/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getMyTasks() {
        
        try {
            User currentUser = getCurrentUser();
            List<Task> tasks = taskRepository.findByCreatedByOrderByCreatedAtDesc(currentUser.getId());
            List<TaskResponse> responses = tasks.stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(responses));
            
        } catch (Exception e) {
            logger.error("Error getting my tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get my tasks: " + e.getMessage()));
        }
    }

    /**
     * 根据任务类型获取任务列表
     * GET /tasks/by-type/{taskType}
     */
    @GetMapping("/by-type/{taskType}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByType(@PathVariable String taskType) {
        
        try {
            User currentUser = getCurrentUser();
            Task.TaskType type;
            
            try {
                type = Task.TaskType.valueOf(taskType.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid task type: " + taskType));
            }
            
            List<Task> tasks;
            if (currentUser.getRole() == User.Role.MANAGER) {
                tasks = taskRepository.findByCreatedBy(currentUser.getId());
            } else {
                tasks = taskRepository.findAll();
            }
            
            List<TaskResponse> responses = tasks.stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(responses));
            
        } catch (Exception e) {
            logger.error("Error getting tasks by type: {}", taskType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get tasks by type: " + e.getMessage()));
        }
    }

    /**
     * 获取任务统计信息
     * GET /tasks/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getTaskStatistics() {
        
        try {
            User currentUser = getCurrentUser();
            
            // 根据用户角色获取统计信息
            Long totalTasks;
            Long completedTasks;
            Long routineTasks;
            Long developmentTasks;
            
            if (currentUser.getRole() == User.Role.MANAGER) {
                // 主管只能看到自己创建的任务统计
                totalTasks = taskRepository.countByCreatedBy(currentUser.getId());
                completedTasks = taskRepository.countByCreatedBy(currentUser.getId());
                routineTasks = 0L; // TaskType已删除，无法统计类型
                developmentTasks = 0L; // TaskType已删除，无法统计类型
            } else {
                // 管理员和超级管理员可以看到所有任务统计
                totalTasks = taskRepository.count();
                completedTasks = taskRepository.count();
                routineTasks = 0L; // TaskType已删除，无法统计类型
                developmentTasks = 0L; // TaskType已删除，无法统计类型
            }
            
            // 构建统计响应
            java.util.Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("totalTasks", totalTasks);
            statistics.put("completedTasks", completedTasks);
            statistics.put("pendingTasks", totalTasks - completedTasks);
            statistics.put("routineTasks", routineTasks);
            statistics.put("developmentTasks", developmentTasks);
            statistics.put("completionRate", totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0.0);
            
            return ResponseEntity.ok(ApiResponse.success(statistics));
            
        } catch (Exception e) {
            logger.error("Error getting task statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get task statistics: " + e.getMessage()));
        }
    }

    /**
     * 根据周报ID获取关联的任务列表
     * GET /tasks/by-report/{reportId}
     */
    @GetMapping("/by-report/{reportId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByReport(@PathVariable Long reportId) {
        try {
            User currentUser = getCurrentUser();
            List<TaskResponse> allTasks = new java.util.ArrayList<>();
            
            // 获取日常性任务（TaskReport）
            List<TaskReport> taskReports = taskReportRepository.findByWeeklyReportId(reportId);
            for (TaskReport taskReport : taskReports) {
                Task task = taskReport.getTask();
                if (task != null) {
                    // 权限检查：主管只能看到自己创建的任务
                    if (currentUser.getRole() == User.Role.MANAGER && 
                        !task.getCreatedBy().equals(currentUser.getId())) {
                        continue;
                    }
                    
                    TaskResponse taskResponse = new TaskResponse(task);
                    // 添加周报相关的字段
                    taskResponse.setActualResults(taskReport.getActualResults());
                    taskResponse.setResultDifferenceAnalysis(taskReport.getResultDifferenceAnalysis());
                    taskResponse.setReportSection(taskReport.getIsWeek() ? "THIS_WEEK_REPORT" : "NEXT_WEEK_PLAN");
                    taskResponse.setTaskTypeString("ROUTINE");
                    allTasks.add(taskResponse);
                }
            }
            
            // 获取发展性任务（DevTaskReport）
            List<DevTaskReport> devTaskReports = devTaskReportRepository.findByWeeklyReportId(reportId);
            for (DevTaskReport devTaskReport : devTaskReports) {
                // 创建一个TaskResponse来表示发展性任务
                TaskResponse taskResponse = new TaskResponse();
                taskResponse.setId(devTaskReport.getId());
                taskResponse.setTaskName("发展性任务");
                taskResponse.setActualResults(devTaskReport.getActualResults());
                taskResponse.setResultDifferenceAnalysis(devTaskReport.getResultDifferenceAnalysis());
                taskResponse.setReportSection(devTaskReport.getIsWeek() ? "THIS_WEEK_REPORT" : "NEXT_WEEK_PLAN");
                taskResponse.setTaskTypeString("DEVELOPMENT");
                taskResponse.setProjectId(devTaskReport.getProjectId());
                taskResponse.setProjectPhaseId(devTaskReport.getPhasesId());
                allTasks.add(taskResponse);
            }
            
            logger.info("Found {} tasks for report {}", allTasks.size(), reportId);
            return ResponseEntity.ok(ApiResponse.success(allTasks));
            
        } catch (Exception e) {
            logger.error("Error getting tasks for report: {}", reportId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get tasks for report: " + e.getMessage()));
        }
    }
}