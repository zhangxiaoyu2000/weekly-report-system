package com.weeklyreport.ai;

import com.weeklyreport.entity.AIAnalysisResult;
import com.weeklyreport.entity.Project;
import com.weeklyreport.repository.AIAnalysisResultRepository;
import com.weeklyreport.repository.ProjectRepository;
import com.weeklyreport.service.ai.AIAnalysisService;
import com.weeklyreport.service.ai.exception.AIServiceException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI分析服务完整功能测试
 * 测试真实的AI分析流程，包括项目创建、AI分析触发、结果存储等
 */
@SpringBootTest
@ActiveProfiles("dev")
public class AIAnalysisIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisIntegrationTest.class);

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AIAnalysisResultRepository aiAnalysisResultRepository;

    /**
     * 测试AI服务基本功能
     */
    @Test
    public void testAIServiceBasicFunctionality() {
        logger.info("=== 开始测试AI服务基本功能 ===");

        // 检查AI服务是否可用
        boolean aiAvailable = aiAnalysisService.isAIAvailable();
        logger.info("AI服务是否可用: {}", aiAvailable);
        assertTrue(aiAvailable, "AI服务应该可用");

        // 检查提供商状态
        var providerStatus = aiAnalysisService.getProvidersStatus();
        logger.info("AI提供商状态: {}", providerStatus);
        assertNotNull(providerStatus, "提供商状态不应为空");
        assertFalse(providerStatus.isEmpty(), "应该有至少一个AI提供商");

        logger.info("=== AI服务基本功能测试通过 ===");
    }

    /**
     * 测试完整的项目AI分析流程
     */
    @Test
    public void testCompleteProjectAIAnalysisFlow() throws Exception {
        logger.info("=== 开始测试完整项目AI分析流程 ===");

        // 1. 创建测试项目
        Project testProject = createTestProject();
        Project savedProject = projectRepository.save(testProject);
        logger.info("创建测试项目: ID={}, 名称={}", savedProject.getId(), savedProject.getName());

        // 2. 记录初始状态
        Project.ApprovalStatus initialStatus = savedProject.getApprovalStatus();
        assertEquals(Project.ApprovalStatus.AI_ANALYZING, initialStatus, "初始状态应为AI_ANALYZING");

        // 3. 记录AI分析结果初始数量
        List<AIAnalysisResult> initialResults = aiAnalysisResultRepository.findAll();
        int initialResultCount = initialResults.size();
        logger.info("AI分析结果初始数量: {}", initialResultCount);

        // 4. 触发AI分析
        logger.info("触发AI分析...");
        long startTime = System.currentTimeMillis();
        
        try {
            aiAnalysisService.analyzeProject(savedProject);
            logger.info("AI分析触发成功");
        } catch (AIServiceException e) {
            logger.error("AI分析触发失败", e);
            fail("AI分析不应该失败: " + e.getMessage());
        }

        // 5. 等待AI分析完成
        boolean analysisCompleted = waitForAnalysisCompletion(savedProject.getId(), 60);
        assertTrue(analysisCompleted, "AI分析应在60秒内完成");

        long duration = System.currentTimeMillis() - startTime;
        logger.info("AI分析完成，总耗时: {}ms", duration);

        // 6. 验证分析结果
        Project completedProject = projectRepository.findById(savedProject.getId()).orElse(null);
        assertNotNull(completedProject, "项目应该存在");

        logger.info("最终项目状态: {}", completedProject.getApprovalStatus());
        logger.info("AI分析ID: {}", completedProject.getAiAnalysisId());
        logger.info("拒绝原因: {}", completedProject.getRejectionReason());

        // 验证状态已改变
        assertNotEquals(Project.ApprovalStatus.AI_ANALYZING, completedProject.getApprovalStatus(), 
                       "项目状态应该已从AI_ANALYZING改变");

        // 验证状态为预期值
        assertTrue(
            completedProject.getApprovalStatus() == Project.ApprovalStatus.AI_APPROVED ||
            completedProject.getApprovalStatus() == Project.ApprovalStatus.AI_REJECTED,
            "项目状态应该为AI_APPROVED或AI_REJECTED"
        );

        // 7. 验证AI分析结果存储
        List<AIAnalysisResult> finalResults = aiAnalysisResultRepository.findAll();
        int finalResultCount = finalResults.size();
        logger.info("AI分析结果最终数量: {}", finalResultCount);

        if (completedProject.getApprovalStatus() == Project.ApprovalStatus.AI_APPROVED) {
            // 如果通过，应该有分析结果
            assertNotNull(completedProject.getAiAnalysisId(), "AI通过时应该有分析结果ID");
            assertEquals(initialResultCount + 1, finalResultCount, "应该新增一条AI分析结果记录");

            // 验证分析结果详情
            AIAnalysisResult analysisResult = aiAnalysisResultRepository
                .findById(completedProject.getAiAnalysisId()).orElse(null);
            assertNotNull(analysisResult, "AI分析结果应该存在");
            assertEquals(savedProject.getId(), analysisResult.getReportId(), "分析结果应该关联到正确的项目");
            assertEquals(AIAnalysisResult.AnalysisStatus.COMPLETED, analysisResult.getStatus(), 
                        "分析状态应该为COMPLETED");
            assertNotNull(analysisResult.getResult(), "分析结果内容不应为空");

            logger.info("AI分析结果验证通过: 结果ID={}, 内容长度={}", 
                       analysisResult.getId(), analysisResult.getResult().length());
        } else {
            // 如果被拒绝，应该有拒绝原因
            assertNotNull(completedProject.getRejectionReason(), "AI拒绝时应该有拒绝原因");
            logger.info("项目被AI拒绝，原因: {}", completedProject.getRejectionReason());
        }

        logger.info("=== 完整项目AI分析流程测试通过 ===");
    }

    /**
     * 测试AI分析回调函数直接调用
     */
    @Test
    public void testAIAnalysisCallbackFunction() throws Exception {
        logger.info("=== 开始测试AI分析回调函数 ===");

        // 1. 创建测试项目
        Project testProject = createTestProject();
        Project savedProject = projectRepository.save(testProject);

        // 2. 使用内置的测试回调功能
        logger.info("调用AI分析回调测试功能...");
        long startTime = System.currentTimeMillis();

        aiAnalysisService.testCallbackExecution(savedProject.getId());

        // 3. 等待回调完成
        boolean callbackCompleted = waitForAnalysisCompletion(savedProject.getId(), 30);
        assertTrue(callbackCompleted, "回调应在30秒内完成");

        long duration = System.currentTimeMillis() - startTime;
        logger.info("回调测试完成，耗时: {}ms", duration);

        // 4. 验证回调结果
        Project updatedProject = projectRepository.findById(savedProject.getId()).orElse(null);
        assertNotNull(updatedProject);

        assertNotEquals(Project.ApprovalStatus.AI_ANALYZING, updatedProject.getApprovalStatus(),
                       "回调后项目状态应该改变");

        logger.info("回调测试结果: 状态={}, AI分析ID={}, 拒绝原因={}", 
                   updatedProject.getApprovalStatus(), updatedProject.getAiAnalysisId(), 
                   updatedProject.getRejectionReason());

        logger.info("=== AI分析回调函数测试通过 ===");
    }

    /**
     * 测试多个项目并发AI分析
     */
    @Test
    public void testConcurrentAIAnalysis() throws Exception {
        logger.info("=== 开始测试并发AI分析 ===");

        int projectCount = 3;
        Project[] projects = new Project[projectCount];
        
        // 1. 创建多个测试项目
        for (int i = 0; i < projectCount; i++) {
            Project project = createTestProject();
            project.setName("并发测试项目-" + (i + 1) + "-" + System.currentTimeMillis());
            projects[i] = projectRepository.save(project);
            logger.info("创建项目 {}: ID={}", i + 1, projects[i].getId());
        }

        // 2. 并发触发AI分析
        logger.info("并发触发AI分析...");
        long startTime = System.currentTimeMillis();

        for (Project project : projects) {
            try {
                aiAnalysisService.analyzeProject(project);
                logger.info("项目 {} AI分析已触发", project.getId());
            } catch (Exception e) {
                logger.error("项目 {} AI分析触发失败", project.getId(), e);
            }
        }

        // 3. 等待所有分析完成
        boolean allCompleted = true;
        for (Project project : projects) {
            boolean completed = waitForAnalysisCompletion(project.getId(), 90);
            if (!completed) {
                allCompleted = false;
                logger.warn("项目 {} AI分析未在时限内完成", project.getId());
            }
        }

        long totalDuration = System.currentTimeMillis() - startTime;
        logger.info("并发AI分析总耗时: {}ms", totalDuration);

        // 4. 验证所有项目状态
        int successCount = 0;
        int failureCount = 0;

        for (Project project : projects) {
            Project updatedProject = projectRepository.findById(project.getId()).orElse(null);
            if (updatedProject != null) {
                Project.ApprovalStatus status = updatedProject.getApprovalStatus();
                logger.info("项目 {} 最终状态: {}", project.getId(), status);

                if (status != Project.ApprovalStatus.AI_ANALYZING) {
                    if (status == Project.ApprovalStatus.AI_APPROVED) {
                        successCount++;
                    } else if (status == Project.ApprovalStatus.AI_REJECTED) {
                        failureCount++;
                    }
                }
            }
        }

        logger.info("并发测试结果统计: 成功={}, 失败={}, 总数={}", successCount, failureCount, projectCount);
        assertTrue(allCompleted, "所有AI分析应该完成");
        assertEquals(projectCount, successCount + failureCount, "所有项目状态都应该更新");

        logger.info("=== 并发AI分析测试通过 ===");
    }

    /**
     * 创建测试项目
     */
    private Project createTestProject() {
        Project project = new Project();
        project.setName("AI分析测试项目-" + System.currentTimeMillis());
        project.setDescription("这是一个用于测试AI分析功能的优秀项目。项目目标明确，资源配置合理，时间规划现实，具有很高的可行性。");
        project.setMembers("张三（项目经理）,李四（技术负责人）,王五（开发工程师）");
        project.setExpectedResults("1. 完成功能开发并通过测试；2. 系统性能指标达到要求；3. 用户满意度超过90%");
        project.setTimeline("第一阶段（1-2周）：需求分析；第二阶段（3-6周）：开发实现；第三阶段（7-8周）：测试部署");
        project.setStopLoss("如果在第二阶段末期发现技术难度超出预期，或用户需求发生重大变化，则考虑项目暂停或重新规划");
        project.setCreatedBy(10004L); // manager1的用户ID
        project.setApprovalStatus(Project.ApprovalStatus.AI_ANALYZING);
        return project;
    }

    /**
     * 等待AI分析完成
     */
    private boolean waitForAnalysisCompletion(Long projectId, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeout = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeout) {
            try {
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project != null && project.getApprovalStatus() != Project.ApprovalStatus.AI_ANALYZING) {
                    logger.info("项目 {} AI分析完成，状态: {}", projectId, project.getApprovalStatus());
                    return true;
                }
                
                Thread.sleep(1000); // 等待1秒再检查
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("等待过程被中断", e);
                return false;
            }
        }
        
        logger.warn("项目 {} AI分析在{}秒内未完成", projectId, timeoutSeconds);
        return false;
    }
}