# 周报系统重构实施计划

基于 `项目重构.md` 的详细实施方案，分为四个阶段系统性重构。

## 当前状态分析

### 已实现的功能
- ✅ 基础用户管理和认证系统
- ✅ 简化项目管理 (`simple_projects`)
- ✅ 项目阶段管理 (`project_phases`)
- ✅ 任务管理 (`tasks`)
- ✅ 基础周报功能 (`weekly_reports`)
- ✅ AI分析服务集成
- ✅ 三级审批工作流
- ✅ 中文周格式工具类 (`WeekFormatHelper`)

### 需要重构的问题
- ❌ 数据结构冗余（`template_id`, `priority`, `tags`, `attachments`, `view_count`）
- ❌ 缺乏结构化任务引用机制
- ❌ 日常任务表不存在
- ❌ 周报内容格式不够结构化
- ❌ 前端组件与新后端结构不匹配

## 阶段一：数据库重构（优先级：🔴 高）

### 1.1 新建数据库表

#### 创建日常任务表
```sql
-- V24__Create_Routine_Tasks_Table.sql
CREATE TABLE routine_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_name VARCHAR(255) NOT NULL COMMENT '任务名称',
    task_description TEXT COMMENT '任务描述',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_routine_tasks_created_by (created_by),
    INDEX idx_routine_tasks_status (status)
) COMMENT='日常性任务表';
```

#### 创建新版周报表
```sql
-- V25__Create_Weekly_Reports_V2_Table.sql
CREATE TABLE weekly_reports_v2 (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) NOT NULL COMMENT '周报标题',
    report_week VARCHAR(50) NOT NULL COMMENT '周报周次（中文格式）',
    content JSON NOT NULL COMMENT '结构化内容',
    additional_notes TEXT COMMENT '其他备注',
    development_opportunities TEXT COMMENT '可发展性清单',
    
    -- 状态控制
    status ENUM('DRAFT', 'SUBMITTED', 'PENDING_AI', 'PENDING_ADMIN', 'PENDING_SUPER_ADMIN', 'APPROVED', 'REJECTED') DEFAULT 'DRAFT',
    
    -- AI分析相关
    ai_analysis_passed BOOLEAN DEFAULT NULL COMMENT 'AI分析是否通过',
    ai_analysis_result TEXT COMMENT 'AI分析结果',
    
    -- 审批相关
    admin_reviewer_id BIGINT COMMENT '管理员审批人ID',
    super_admin_reviewer_id BIGINT COMMENT '超级管理员审批人ID',
    rejection_reason TEXT COMMENT '拒绝理由',
    
    -- 时间控制
    week_start DATE NOT NULL COMMENT '周开始日期',
    week_end DATE NOT NULL COMMENT '周结束日期',
    submitted_at TIMESTAMP NULL COMMENT '提交时间',
    reviewed_at TIMESTAMP NULL COMMENT '审核时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (admin_reviewer_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (super_admin_reviewer_id) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_weekly_reports_v2_user_id (user_id),
    INDEX idx_weekly_reports_v2_status (status),
    INDEX idx_weekly_reports_v2_week_start (week_start),
    INDEX idx_weekly_reports_v2_report_week (report_week),
    UNIQUE KEY uk_user_week (user_id, week_start)
) COMMENT='周报表V2';
```

### 1.2 修改现有表结构

#### 删除冗余字段
```sql
-- V26__Remove_Redundant_Fields.sql
ALTER TABLE weekly_reports 
DROP COLUMN template_id,
DROP COLUMN priority,
DROP COLUMN tags,
DROP COLUMN attachments,
DROP COLUMN view_count;

-- 删除相关索引
DROP INDEX idx_reports_template_id ON weekly_reports;
DROP INDEX idx_reports_priority ON weekly_reports;
```

#### 添加必需字段
```sql
-- V27__Add_Required_Fields.sql
ALTER TABLE weekly_reports 
ADD COLUMN report_week VARCHAR(50) COMMENT '中文周格式显示',
ADD COLUMN ai_analysis_passed BOOLEAN DEFAULT NULL COMMENT 'AI分析是否通过',
ADD COLUMN ai_analysis_result TEXT COMMENT 'AI分析结果';

-- 添加索引
CREATE INDEX idx_weekly_reports_report_week ON weekly_reports(report_week);
```

### 1.3 数据迁移策略

#### 数据迁移脚本
```sql
-- V28__Migrate_Existing_Data.sql
-- 填充report_week字段
UPDATE weekly_reports 
SET report_week = CONCAT(
    MONTH(week_start), '月第', 
    WEEK(week_start, 1) - WEEK(DATE_SUB(week_start, INTERVAL DAYOFMONTH(week_start) - 1 DAY), 1) + 1, 
    '周'
) 
WHERE report_week IS NULL;

-- 设置默认AI分析结果
UPDATE weekly_reports 
SET ai_analysis_passed = TRUE,
    ai_analysis_result = '历史数据，已通过审核'
WHERE status IN ('APPROVED', 'PUBLISHED');
```

### 1.4 清理不需要的表和数据

#### 删除废弃功能
```sql
-- V29__Remove_Deprecated_Tables.sql
-- 删除模板相关数据
DROP TABLE IF EXISTS templates;

-- 删除评论相关数据（如果存在）
DROP TABLE IF EXISTS comments;

-- 删除部门相关数据（简化架构）
-- 注意：需要先更新users表的department_id为NULL
UPDATE users SET department_id = NULL;
DROP TABLE IF EXISTS departments;
```

## 阶段二：后端重构（优先级：🟡 中）

### 2.1 新建实体类

#### RoutineTask.java
```java
@Entity
@Table(name = "routine_tasks")
public class RoutineTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "task_name", nullable = false)
    private String taskName;
    
    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TaskStatus status = TaskStatus.ACTIVE;
    
    // 标准时间戳
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum TaskStatus {
        ACTIVE, INACTIVE
    }
    
    // Getters and setters...
}
```

#### WeeklyReportV2.java
```java
@Entity
@Table(name = "weekly_reports_v2")
public class WeeklyReportV2 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "report_week", nullable = false)
    private String reportWeek;
    
    @Column(name = "content", columnDefinition = "JSON", nullable = false)
    private String content; // JSON格式的结构化内容
    
    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;
    
    @Column(name = "development_opportunities", columnDefinition = "TEXT")
    private String developmentOpportunities;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReportStatus status = ReportStatus.DRAFT;
    
    // AI分析字段
    @Column(name = "ai_analysis_passed")
    private Boolean aiAnalysisPassed;
    
    @Column(name = "ai_analysis_result", columnDefinition = "TEXT")
    private String aiAnalysisResult;
    
    // 审批字段
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_reviewer_id")
    private User adminReviewer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_admin_reviewer_id")
    private User superAdminReviewer;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    // 时间字段
    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;
    
    @Column(name = "week_end", nullable = false)
    private LocalDate weekEnd;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum ReportStatus {
        DRAFT, SUBMITTED, PENDING_AI, PENDING_ADMIN, 
        PENDING_SUPER_ADMIN, APPROVED, REJECTED
    }
    
    // Getters and setters...
}
```

### 2.2 新建DTO类

#### WeeklyReportContentV2.java
```java
// 结构化内容DTO
public class WeeklyReportContentV2 {
    private ThisWeekReport thisWeekReport;
    private NextWeekPlan nextWeekPlan;
    
    // Getters and setters...
}

public class ThisWeekReport {
    private List<RoutineTaskReference> routineTasks;
    private List<DevelopmentTaskReference> developmentTasks;
    
    // Getters and setters...
}

public class NextWeekPlan {
    private List<RoutineTaskReference> routineTasks;
    private List<DevelopmentTaskReference> developmentTasks;
    
    // Getters and setters...
}

public class RoutineTaskReference {
    private Long taskId;
    private String actualResult;           // 仅本周汇报使用
    private String analysisOfResultDifferences; // 仅本周汇报使用
    
    // Getters and setters...
}

public class DevelopmentTaskReference {
    private Long projectId;
    private Long phaseId;
    private String actualResult;           // 仅本周汇报使用
    private String analysisOfResultDifferences; // 仅本周汇报使用
    
    // Getters and setters...
}
```

#### WeeklyReportV2Request/Response.java
```java
@Valid
public class WeeklyReportV2CreateRequest {
    @NotBlank
    private String title;
    
    @NotNull
    private LocalDate weekStart;
    
    @NotNull
    private WeeklyReportContentV2 content;
    
    private String additionalNotes;
    private String developmentOpportunities;
    
    // Getters and setters...
}

public class WeeklyReportV2Response {
    private Long id;
    private String title;
    private String reportWeek;
    private WeeklyReportContentV2 content;
    private String additionalNotes;
    private String developmentOpportunities;
    private ReportStatus status;
    private Boolean aiAnalysisPassed;
    private String aiAnalysisResult;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters and setters...
}
```

### 2.3 新建Service类

#### RoutineTaskService.java
```java
@Service
@Transactional
public class RoutineTaskService {
    
    @Autowired
    private RoutineTaskRepository routineTaskRepository;
    
    public List<RoutineTask> getTasksByUser(Long userId) {
        return routineTaskRepository.findByCreatedByIdAndStatus(userId, TaskStatus.ACTIVE);
    }
    
    public RoutineTask createTask(RoutineTask task) {
        return routineTaskRepository.save(task);
    }
    
    public RoutineTask updateTask(Long id, RoutineTask taskDetails) {
        RoutineTask task = routineTaskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        
        task.setTaskName(taskDetails.getTaskName());
        task.setTaskDescription(taskDetails.getTaskDescription());
        task.setStatus(taskDetails.getStatus());
        
        return routineTaskRepository.save(task);
    }
    
    public void deleteTask(Long id) {
        RoutineTask task = routineTaskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        task.setStatus(TaskStatus.INACTIVE);
        routineTaskRepository.save(task);
    }
}
```

#### WeeklyReportServiceV2.java
```java
@Service
@Transactional
public class WeeklyReportServiceV2 {
    
    @Autowired
    private WeeklyReportV2Repository reportRepository;
    
    @Autowired
    private WeekFormatHelper weekFormatHelper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public WeeklyReportV2 createReport(WeeklyReportV2CreateRequest request, User user) {
        WeeklyReportV2 report = new WeeklyReportV2();
        report.setUser(user);
        report.setTitle(request.getTitle());
        report.setWeekStart(request.getWeekStart());
        report.setWeekEnd(WeekFormatHelper.getWeekEnd(request.getWeekStart()));
        report.setReportWeek(WeekFormatHelper.getReportWeekDisplay(request.getWeekStart()));
        
        // 序列化结构化内容
        try {
            String contentJson = objectMapper.writeValueAsString(request.getContent());
            report.setContent(contentJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize report content", e);
        }
        
        report.setAdditionalNotes(request.getAdditionalNotes());
        report.setDevelopmentOpportunities(request.getDevelopmentOpportunities());
        
        return reportRepository.save(report);
    }
    
    public WeeklyReportV2Response getReportResponse(Long id) {
        WeeklyReportV2 report = reportRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Report not found"));
        
        return convertToResponse(report);
    }
    
    private WeeklyReportV2Response convertToResponse(WeeklyReportV2 report) {
        WeeklyReportV2Response response = new WeeklyReportV2Response();
        // 标准字段映射
        response.setId(report.getId());
        response.setTitle(report.getTitle());
        response.setReportWeek(report.getReportWeek());
        response.setAdditionalNotes(report.getAdditionalNotes());
        response.setDevelopmentOpportunities(report.getDevelopmentOpportunities());
        response.setStatus(report.getStatus());
        response.setAiAnalysisPassed(report.getAiAnalysisPassed());
        response.setAiAnalysisResult(report.getAiAnalysisResult());
        response.setWeekStart(report.getWeekStart());
        response.setWeekEnd(report.getWeekEnd());
        response.setSubmittedAt(report.getSubmittedAt());
        response.setReviewedAt(report.getReviewedAt());
        response.setCreatedAt(report.getCreatedAt());
        response.setUpdatedAt(report.getUpdatedAt());
        
        // 反序列化结构化内容
        try {
            WeeklyReportContentV2 content = objectMapper.readValue(
                report.getContent(), WeeklyReportContentV2.class);
            response.setContent(content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize report content", e);
        }
        
        return response;
    }
    
    public void submitReport(Long id) {
        WeeklyReportV2 report = reportRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Report not found"));
        
        report.setStatus(ReportStatus.SUBMITTED);
        report.setSubmittedAt(LocalDateTime.now());
        reportRepository.save(report);
        
        // 触发AI分析
        triggerAIAnalysis(report);
    }
    
    private void triggerAIAnalysis(WeeklyReportV2 report) {
        report.setStatus(ReportStatus.PENDING_AI);
        reportRepository.save(report);
        // 调用AI服务...
    }
}
```

### 2.4 新建Controller

#### RoutineTaskController.java
```java
@RestController
@RequestMapping("/api/routine-tasks")
@Validated
public class RoutineTaskController extends BaseController {
    
    @Autowired
    private RoutineTaskService routineTaskService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoutineTask>>> getUserTasks() {
        Long userId = getCurrentUserId();
        List<RoutineTask> tasks = routineTaskService.getTasksByUser(userId);
        return ok(tasks, "获取日常任务成功");
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<RoutineTask>> createTask(@Valid @RequestBody RoutineTaskCreateRequest request) {
        User currentUser = getCurrentUser();
        RoutineTask task = new RoutineTask();
        task.setTaskName(request.getTaskName());
        task.setTaskDescription(request.getTaskDescription());
        task.setCreatedBy(currentUser);
        
        RoutineTask savedTask = routineTaskService.createTask(task);
        return ok(savedTask, "创建日常任务成功");
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoutineTask>> updateTask(
            @PathVariable Long id, 
            @Valid @RequestBody RoutineTaskUpdateRequest request) {
        RoutineTask taskDetails = new RoutineTask();
        taskDetails.setTaskName(request.getTaskName());
        taskDetails.setTaskDescription(request.getTaskDescription());
        taskDetails.setStatus(request.getStatus());
        
        RoutineTask updatedTask = routineTaskService.updateTask(id, taskDetails);
        return ok(updatedTask, "更新日常任务成功");
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable Long id) {
        routineTaskService.deleteTask(id);
        return ok("删除日常任务成功");
    }
}
```

#### WeeklyReportV2Controller.java
```java
@RestController
@RequestMapping("/api/v2/weekly-reports")
@Validated
public class WeeklyReportV2Controller extends BaseController {
    
    @Autowired
    private WeeklyReportServiceV2 reportService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<WeeklyReportV2Response>> createReport(
            @Valid @RequestBody WeeklyReportV2CreateRequest request) {
        User currentUser = getCurrentUser();
        WeeklyReportV2 report = reportService.createReport(request, currentUser);
        WeeklyReportV2Response response = reportService.getReportResponse(report.getId());
        return ok(response, "创建周报成功");
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WeeklyReportV2Response>> getReport(@PathVariable Long id) {
        WeeklyReportV2Response response = reportService.getReportResponse(id);
        return ok(response, "获取周报成功");
    }
    
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<String>> submitReport(@PathVariable Long id) {
        reportService.submitReport(id);
        return ok("提交周报成功");
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WeeklyReportV2Response>>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        // 实现分页查询逻辑
        return ok("获取周报列表成功");
    }
}
```

### 2.5 需要删除的文件

#### 删除模板相关类
- `src/main/java/com/weeklyreport/entity/Template.java`
- `src/main/java/com/weeklyreport/repository/TemplateRepository.java`
- `src/main/java/com/weeklyreport/service/TemplateService.java`
- `src/main/java/com/weeklyreport/controller/TemplateController.java`

#### 删除评论相关类
- `src/main/java/com/weeklyreport/entity/Comment.java`
- `src/main/java/com/weeklyreport/repository/CommentRepository.java`
- `src/main/java/com/weeklyreport/service/CommentService.java`
- `src/main/java/com/weeklyreport/controller/CommentController.java`

#### 删除部门相关类
- `src/main/java/com/weeklyreport/entity/Department.java`
- `src/main/java/com/weeklyreport/repository/DepartmentRepository.java`
- `src/main/java/com/weeklyreport/service/DepartmentService.java`
- `src/main/java/com/weeklyreport/controller/DepartmentController.java`

## 阶段三：前端重构（优先级：🟡 中）

### 3.1 新建组件

#### RoutineTaskManager.vue
```vue
<template>
  <div class="routine-task-manager">
    <div class="task-header">
      <h3>日常任务管理</h3>
      <button @click="showCreateDialog = true" class="btn-primary">添加任务</button>
    </div>
    
    <div class="task-list">
      <div v-for="task in tasks" :key="task.id" class="task-item">
        <div class="task-content">
          <h4>{{ task.taskName }}</h4>
          <p>{{ task.taskDescription }}</p>
        </div>
        <div class="task-actions">
          <button @click="editTask(task)" class="btn-secondary">编辑</button>
          <button @click="deleteTask(task.id)" class="btn-danger">删除</button>
        </div>
      </div>
    </div>
    
    <!-- 创建/编辑对话框 -->
    <TaskFormDialog 
      v-if="showCreateDialog || editingTask"
      :task="editingTask"
      @save="handleSaveTask"
      @cancel="handleCancelEdit"
    />
  </div>
</template>

<script>
import TaskFormDialog from './TaskFormDialog.vue';
import { routineTaskApi } from '@/services/api';

export default {
  name: 'RoutineTaskManager',
  components: {
    TaskFormDialog
  },
  data() {
    return {
      tasks: [],
      showCreateDialog: false,
      editingTask: null
    };
  },
  async mounted() {
    await this.loadTasks();
  },
  methods: {
    async loadTasks() {
      try {
        const response = await routineTaskApi.getUserTasks();
        this.tasks = response.data;
      } catch (error) {
        console.error('加载任务失败:', error);
      }
    },
    
    editTask(task) {
      this.editingTask = { ...task };
    },
    
    async handleSaveTask(taskData) {
      try {
        if (this.editingTask) {
          await routineTaskApi.updateTask(this.editingTask.id, taskData);
        } else {
          await routineTaskApi.createTask(taskData);
        }
        
        await this.loadTasks();
        this.handleCancelEdit();
      } catch (error) {
        console.error('保存任务失败:', error);
      }
    },
    
    handleCancelEdit() {
      this.showCreateDialog = false;
      this.editingTask = null;
    },
    
    async deleteTask(taskId) {
      if (confirm('确定要删除这个任务吗？')) {
        try {
          await routineTaskApi.deleteTask(taskId);
          await this.loadTasks();
        } catch (error) {
          console.error('删除任务失败:', error);
        }
      }
    }
  }
};
</script>
```

#### TaskReferenceSelector.vue
```vue
<template>
  <div class="task-reference-selector">
    <h4>{{ title }}</h4>
    
    <!-- 日常任务选择 -->
    <div class="routine-tasks-section">
      <h5>日常任务</h5>
      <div v-for="task in availableRoutineTasks" :key="task.id" class="task-option">
        <input 
          type="checkbox" 
          :id="`routine-${task.id}`"
          :value="task.id"
          v-model="selectedRoutineTasks"
        />
        <label :for="`routine-${task.id}`">{{ task.taskName }}</label>
        
        <!-- 如果是本周汇报，显示结果输入框 -->
        <div v-if="isThisWeekReport && isTaskSelected('routine', task.id)" class="result-inputs">
          <textarea 
            :placeholder="`${task.taskName} - 实际结果`"
            v-model="routineTaskResults[task.id].actualResult"
          ></textarea>
          <textarea 
            :placeholder="`${task.taskName} - 结果差异分析`"
            v-model="routineTaskResults[task.id].analysisOfResultDifferences"
          ></textarea>
        </div>
      </div>
    </div>
    
    <!-- 发展性任务选择（项目阶段） -->
    <div class="development-tasks-section">
      <h5>发展性任务</h5>
      <div v-for="project in availableProjects" :key="project.id" class="project-section">
        <h6>{{ project.projectName }}</h6>
        <div v-for="phase in project.phases" :key="phase.id" class="phase-option">
          <input 
            type="checkbox" 
            :id="`phase-${phase.id}`"
            :value="phase.id"
            v-model="selectedDevelopmentTasks"
          />
          <label :for="`phase-${phase.id}`">{{ phase.phaseName }}</label>
          
          <!-- 如果是本周汇报，显示结果输入框 -->
          <div v-if="isThisWeekReport && isPhaseSelected(phase.id)" class="result-inputs">
            <textarea 
              :placeholder="`${phase.phaseName} - 实际结果`"
              v-model="developmentTaskResults[phase.id].actualResult"
            ></textarea>
            <textarea 
              :placeholder="`${phase.phaseName} - 结果差异分析`"
              v-model="developmentTaskResults[phase.id].analysisOfResultDifferences"
            ></textarea>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TaskReferenceSelector',
  props: {
    title: String,
    isThisWeekReport: {
      type: Boolean,
      default: false
    },
    modelValue: {
      type: Object,
      default: () => ({
        routineTasks: [],
        developmentTasks: []
      })
    }
  },
  emits: ['update:modelValue'],
  data() {
    return {
      availableRoutineTasks: [],
      availableProjects: [],
      selectedRoutineTasks: [],
      selectedDevelopmentTasks: [],
      routineTaskResults: {},
      developmentTaskResults: {}
    };
  },
  async mounted() {
    await this.loadData();
  },
  watch: {
    selectedRoutineTasks: {
      handler() {
        this.updateValue();
      },
      deep: true
    },
    selectedDevelopmentTasks: {
      handler() {
        this.updateValue();
      },
      deep: true
    },
    routineTaskResults: {
      handler() {
        this.updateValue();
      },
      deep: true
    },
    developmentTaskResults: {
      handler() {
        this.updateValue();
      },
      deep: true
    }
  },
  methods: {
    async loadData() {
      // 加载可用的日常任务和项目阶段
      // 实现数据加载逻辑...
    },
    
    isTaskSelected(type, id) {
      return type === 'routine' 
        ? this.selectedRoutineTasks.includes(id)
        : this.selectedDevelopmentTasks.includes(id);
    },
    
    isPhaseSelected(phaseId) {
      return this.selectedDevelopmentTasks.includes(phaseId);
    },
    
    updateValue() {
      const value = {
        routineTasks: this.selectedRoutineTasks.map(taskId => ({
          taskId,
          ...(this.isThisWeekReport ? this.routineTaskResults[taskId] || {} : {})
        })),
        developmentTasks: this.selectedDevelopmentTasks.map(phaseId => {
          const project = this.findProjectByPhaseId(phaseId);
          return {
            projectId: project.id,
            phaseId,
            ...(this.isThisWeekReport ? this.developmentTaskResults[phaseId] || {} : {})
          };
        })
      };
      
      this.$emit('update:modelValue', value);
    },
    
    findProjectByPhaseId(phaseId) {
      for (const project of this.availableProjects) {
        const phase = project.phases.find(p => p.id === phaseId);
        if (phase) return project;
      }
      return null;
    }
  }
};
</script>
```

#### WeeklyReportFormV2.vue
```vue
<template>
  <div class="weekly-report-form-v2">
    <form @submit.prevent="handleSubmit">
      <!-- 基础信息 -->
      <div class="form-section">
        <h3>周报基础信息</h3>
        <div class="form-group">
          <label>标题</label>
          <input 
            type="text" 
            v-model="formData.title" 
            placeholder="请输入周报标题"
            required
          />
        </div>
        <div class="form-group">
          <label>周开始日期</label>
          <input 
            type="date" 
            v-model="formData.weekStart" 
            required
          />
        </div>
      </div>
      
      <!-- 本周汇报 -->
      <div class="form-section">
        <h3>本周汇报</h3>
        <TaskReferenceSelector
          title="选择本周完成的任务"
          :isThisWeekReport="true"
          v-model="formData.content.thisWeekReport"
        />
      </div>
      
      <!-- 下周规划 -->
      <div class="form-section">
        <h3>下周规划</h3>
        <TaskReferenceSelector
          title="选择下周计划的任务"
          :isThisWeekReport="false"
          v-model="formData.content.nextWeekPlan"
        />
      </div>
      
      <!-- 其他信息 -->
      <div class="form-section">
        <h3>其他信息</h3>
        <div class="form-group">
          <label>其他备注</label>
          <textarea 
            v-model="formData.additionalNotes"
            placeholder="请输入其他需要说明的内容"
          ></textarea>
        </div>
        <div class="form-group">
          <label>可发展性清单</label>
          <textarea 
            v-model="formData.developmentOpportunities"
            placeholder="请输入可发展性机会和建议"
          ></textarea>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="form-actions">
        <button type="button" @click="saveDraft" class="btn-secondary">保存草稿</button>
        <button type="submit" class="btn-primary">提交周报</button>
      </div>
    </form>
  </div>
</template>

<script>
import TaskReferenceSelector from './TaskReferenceSelector.vue';
import { weeklyReportV2Api } from '@/services/api';

export default {
  name: 'WeeklyReportFormV2',
  components: {
    TaskReferenceSelector
  },
  data() {
    return {
      formData: {
        title: '',
        weekStart: '',
        content: {
          thisWeekReport: {
            routineTasks: [],
            developmentTasks: []
          },
          nextWeekPlan: {
            routineTasks: [],
            developmentTasks: []
          }
        },
        additionalNotes: '',
        developmentOpportunities: ''
      }
    };
  },
  methods: {
    async saveDraft() {
      try {
        const response = await weeklyReportV2Api.createReport(this.formData);
        this.$router.push(`/reports/${response.data.id}`);
      } catch (error) {
        console.error('保存草稿失败:', error);
      }
    },
    
    async handleSubmit() {
      try {
        const response = await weeklyReportV2Api.createReport(this.formData);
        await weeklyReportV2Api.submitReport(response.data.id);
        this.$router.push('/reports');
      } catch (error) {
        console.error('提交周报失败:', error);
      }
    }
  }
};
</script>
```

### 3.2 修改现有组件

#### 更新ReportManager.vue
- 删除模板选择功能
- 集成新的TaskReferenceSelector组件
- 更新数据提交格式

#### 更新API服务 (api.ts)
```typescript
// 新增API服务
export const routineTaskApi = {
  getUserTasks: () => api.get('/api/routine-tasks'),
  createTask: (data: any) => api.post('/api/routine-tasks', data),
  updateTask: (id: number, data: any) => api.put(`/api/routine-tasks/${id}`, data),
  deleteTask: (id: number) => api.delete(`/api/routine-tasks/${id}`)
};

export const weeklyReportV2Api = {
  createReport: (data: any) => api.post('/api/v2/weekly-reports', data),
  getReport: (id: number) => api.get(`/api/v2/weekly-reports/${id}`),
  updateReport: (id: number, data: any) => api.put(`/api/v2/weekly-reports/${id}`, data),
  submitReport: (id: number) => api.post(`/api/v2/weekly-reports/${id}/submit`),
  getReports: (params: any) => api.get('/api/v2/weekly-reports', { params })
};

// 删除的API服务
// - templateApi相关方法
// - commentApi相关方法
// - departmentApi相关方法
```

### 3.3 更新路由配置

#### router/index.ts
```typescript
// 新增路由
{
  path: '/routine-tasks',
  name: 'RoutineTasks',
  component: () => import('@/views/RoutineTasksView.vue'),
  meta: { requiresAuth: true }
},
{
  path: '/reports/v2/create',
  name: 'CreateReportV2',
  component: () => import('@/views/CreateReportV2View.vue'),
  meta: { requiresAuth: true }
},

// 删除路由
// - /templates 相关路由
// - /departments 相关路由
// - /comments 相关路由
```

### 3.4 需要删除的前端文件

#### 删除模板相关组件
- `src/components/TemplateManager.vue`
- `src/views/TemplateView.vue`
- `src/utils/templateUtils.js`

#### 删除评论相关组件
- `src/components/CommentSection.vue`
- `src/utils/commentUtils.js`

#### 删除部门管理组件
- `src/components/DepartmentManagement.vue`
- `src/views/DepartmentView.vue`

## 阶段四：验证和清理（优先级：🟢 低）

### 4.1 数据完整性验证

#### 验证脚本
```sql
-- V30__Validation_And_Cleanup.sql

-- 验证数据迁移完整性
SELECT 
    COUNT(*) as total_old_reports,
    COUNT(CASE WHEN report_week IS NOT NULL THEN 1 END) as reports_with_week_format,
    COUNT(CASE WHEN ai_analysis_passed IS NOT NULL THEN 1 END) as reports_with_ai_status
FROM weekly_reports;

-- 验证用户数据完整性
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN department_id IS NULL THEN 1 END) as users_without_department
FROM users;

-- 验证日常任务数据
SELECT 
    COUNT(*) as total_routine_tasks,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_tasks
FROM routine_tasks;
```

### 4.2 性能优化

#### 索引优化
```sql
-- 添加性能索引
CREATE INDEX idx_weekly_reports_user_status ON weekly_reports(user_id, status);
CREATE INDEX idx_weekly_reports_week_status ON weekly_reports(week_start, status);
CREATE INDEX idx_routine_tasks_user_status ON routine_tasks(created_by, status);
CREATE INDEX idx_project_phases_project_status ON project_phases(project_id, status);
```

#### 查询优化
- 优化周报列表查询
- 优化任务关联查询
- 添加分页和缓存

### 4.3 代码清理

#### 删除废弃代码
```bash
# 删除废弃的Java文件
rm -rf src/main/java/com/weeklyreport/entity/Template.java
rm -rf src/main/java/com/weeklyreport/entity/Comment.java
rm -rf src/main/java/com/weeklyreport/entity/Department.java
rm -rf src/main/java/com/weeklyreport/repository/*Template*
rm -rf src/main/java/com/weeklyreport/repository/*Comment*
rm -rf src/main/java/com/weeklyreport/repository/*Department*
rm -rf src/main/java/com/weeklyreport/service/*Template*
rm -rf src/main/java/com/weeklyreport/service/*Comment*
rm -rf src/main/java/com/weeklyreport/service/*Department*
rm -rf src/main/java/com/weeklyreport/controller/*Template*
rm -rf src/main/java/com/weeklyreport/controller/*Comment*
rm -rf src/main/java/com/weeklyreport/controller/*Department*

# 删除废弃的前端文件
rm -rf frontend/src/components/TemplateManager.vue
rm -rf frontend/src/components/CommentSection.vue
rm -rf frontend/src/components/DepartmentManagement.vue
rm -rf frontend/src/views/TemplateView.vue
rm -rf frontend/src/views/DepartmentView.vue
rm -rf frontend/src/utils/templateUtils.js
rm -rf frontend/src/utils/commentUtils.js
```

#### 更新配置文件
- 删除模板相关配置
- 更新API文档
- 更新部署脚本

### 4.4 测试验证

#### 单元测试
- 新增RoutineTaskService测试
- 新增WeeklyReportServiceV2测试
- 更新现有测试以适配新结构

#### 集成测试
- API端点测试
- 数据库集成测试
- 前后端集成测试

#### 端到端测试
- 用户创建日常任务流程
- 周报创建和提交流程
- 审批工作流测试

## 风险评估与缓解策略

### 高风险区域
1. **数据迁移风险** - 现有周报数据可能丢失
   - 缓解：完整数据备份 + 回滚方案
   - 验证：迁移前后数据量对比

2. **API兼容性风险** - 前端调用失败
   - 缓解：保留旧API一段时间 + 渐进式迁移
   - 验证：API版本并行运行

3. **业务流程中断风险** - 审批流程异常
   - 缓解：灰度发布 + 快速回滚
   - 验证：关键路径测试

### 低风险区域
1. **UI组件重构** - 独立性强，影响范围有限
2. **工具类优化** - 已存在WeekFormatHelper，风险可控
3. **性能优化** - 渐进式优化，可逐步调整

## 实施时间安排

### 第1周：数据库重构
- 1-2天：创建新表和迁移脚本
- 2-3天：数据迁移和验证
- 1-2天：性能调优和索引优化

### 第2-3周：后端重构
- 3-4天：新增实体和DTO类
- 3-4天：实现Service层逻辑
- 2-3天：Controller和API实现
- 1-2天：单元测试和集成测试

### 第4周：前端重构
- 2-3天：核心组件开发
- 2-3天：页面重构和路由更新
- 2天：API集成和测试

### 第5周：验证和上线
- 2天：完整测试和bug修复
- 1天：生产环境部署
- 2天：监控和问题修复

## 成功标准

### 功能完整性
- ✅ 所有现有功能正常运行
- ✅ 新的结构化任务引用功能正常
- ✅ 中文周格式正确显示
- ✅ 审批工作流程正常

### 性能要求
- ✅ 页面加载时间 < 2秒
- ✅ API响应时间 < 500ms
- ✅ 数据库查询优化 > 50%

### 数据完整性
- ✅ 零数据丢失
- ✅ 历史数据完整迁移
- ✅ 新老系统数据一致

## 总结

本重构方案通过四个阶段系统性地简化系统架构、优化数据结构、提升用户体验。重点关注：

1. **数据结构优化** - 删除冗余字段，增加结构化任务引用
2. **业务流程简化** - 移除不必要功能，聚焦核心业务
3. **技术架构优化** - 提升代码质量和系统性能
4. **用户体验提升** - 中文化界面和结构化操作

通过渐进式实施和充分的测试验证，确保重构过程的安全性和系统的稳定性。