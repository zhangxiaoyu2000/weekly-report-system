package com.weeklyreport.ai;

import com.weeklyreport.entity.AIAnalysisResult;
import com.weeklyreport.entity.Project;
import com.weeklyreport.repository.AIAnalysisResultRepository;
import com.weeklyreport.repository.ProjectRepository;
import com.weeklyreport.service.ai.AIAnalysisService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI分析服务集成测试类
 * 专门测试AI分析功能的完整流程，包括回调函数执行和数据存储
 */
@SpringBootTest
@ActiveProfiles("test")
public class AIAnalysisServiceIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisServiceIntegrationTest.class);

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AIAnalysisResultRepository aiAnalysisResultRepository;

    /**
     * 测试AI分析完整流程：创建项目 -> 触发AI分析 -> 验证回调执行 -> 验证数据存储
     */
    @Test
    @Transactional
    public void testCompleteAIAnalysisFlow() throws Exception {
        logger.info("=== 开始测试AI分析完整流程 ===");

        // 1. 创建测试项目
        Project testProject = createTestProject();
        Project savedProject = projectRepository.save(testProject);
        logger.info("创建测试项目完成，ID: {}", savedProject.getId());

        // 2. 记录初始状态
        assertEquals(Project.ApprovalStatus.AI_ANALYZING, savedProject.getApprovalStatus());
        assertNull(savedProject.getAiAnalysisId());
        
        // 3. 记录AI分析结果初始数量
        List<AIAnalysisResult> initialResults = aiAnalysisResultRepository.findAll();
        int initialResultCount = initialResults.size();
        logger.info("AI分析结果初始数量: {}", initialResultCount);

        // 4. 触发AI分析
        logger.info("触发AI分析...");
        aiAnalysisService.analyzeProject(savedProject);

        // 5. 等待AI分析完成（最多等待30秒）
        boolean analysisCompleted = waitForAnalysisCompletion(savedProject.getId(), 30);
        assertTrue(analysisCompleted, "AI分析应在30秒内完成");

        // 6. 验证项目状态更新
        Project updatedProject = projectRepository.findById(savedProject.getId()).orElse(null);
        assertNotNull(updatedProject);
        logger.info("项目状态: {}, AI分析ID: {}", updatedProject.getApprovalStatus(), updatedProject.getAiAnalysisId());

        // 验证项目状态已从AI_ANALYZING改变
        assertNotEquals(Project.ApprovalStatus.AI_ANALYZING, updatedProject.getApprovalStatus());
        
        // 验证状态为AI_APPROVED或AI_REJECTED
        assertTrue(
            updatedProject.getApprovalStatus() == Project.ApprovalStatus.AI_APPROVED ||
            updatedProject.getApprovalStatus() == Project.ApprovalStatus.AI_REJECTED,
            "项目状态应为AI_APPROVED或AI_REJECTED"
        );

        // 7. 验证AI分析结果存储
        List<AIAnalysisResult> finalResults = aiAnalysisResultRepository.findAll();
        int finalResultCount = finalResults.size();
        logger.info("AI分析结果最终数量: {}", finalResultCount);
        
        assertEquals(initialResultCount + 1, finalResultCount, "应该新增一条AI分析结果记录");

        // 8. 验证AI分析ID关联
        if (updatedProject.getApprovalStatus() == Project.ApprovalStatus.AI_APPROVED) {
            assertNotNull(updatedProject.getAiAnalysisId(), "AI分析通过时应该有分析结果ID");
            
            // 验证分析结果记录存在
            AIAnalysisResult analysisResult = aiAnalysisResultRepository.findById(updatedProject.getAiAnalysisId()).orElse(null);
            assertNotNull(analysisResult, "AI分析结果记录应存在");
            assertEquals(savedProject.getId(), analysisResult.getReportId(), "分析结果应关联到正确的项目");
            logger.info("AI分析结果验证通过，结果ID: {}", analysisResult.getId());
        }

        logger.info("=== AI分析完整流程测试完成 ===");
    }

    /**
     * 测试AI分析异步执行
     */
    @Test
    public void testAsyncAIAnalysis() throws Exception {
        logger.info("=== 开始测试AI分析异步执行 ===");

        Project testProject = createTestProject();
        Project savedProject = projectRepository.save(testProject);

        // 触发异步AI分析
        CompletableFuture<Void> analysisFuture = CompletableFuture.runAsync(() -> {
            try {
                aiAnalysisService.analyzeProject(savedProject);
                logger.info("异步AI分析触发完成");
            } catch (Exception e) {
                logger.error("异步AI分析触发失败", e);
                throw new RuntimeException(e);
            }
        });

        // 等待异步操作完成
        analysisFuture.get(10, TimeUnit.SECONDS);

        // 等待分析结果
        boolean completed = waitForAnalysisCompletion(savedProject.getId(), 20);
        assertTrue(completed, "异步AI分析应该完成");

        logger.info("=== AI分析异步执行测试完成 ===");
    }

    /**
     * 测试AI分析回调函数直接调用
     */
    @Test
    @Transactional
    public void testAIAnalysisCallbackDirect() {
        logger.info("=== 开始测试AI分析回调函数直接调用 ===");

        Project testProject = createTestProject();
        Project savedProject = projectRepository.save(testProject);

        // 模拟AI分析响应
        com.weeklyreport.service.ai.dto.AIAnalysisResponse mockResponse = 
            new com.weeklyreport.service.ai.dto.AIAnalysisResponse();
        mockResponse.setAnalysisId("test-analysis-id-" + System.currentTimeMillis());
        mockResponse.setResult("{\n" +
            "  \"isPass\": true,\n" +
            "  \"proposal\": \"项目可行性良好，建议继续推进\",\n" +
            "  \"feasibilityScore\": 0.85,\n" +
            "  \"riskLevel\": \"LOW\"\n" +
            "}");
        mockResponse.setProviderUsed("test-provider");

        // 直接调用回调函数
        try {
            aiAnalysisService.handleAIAnalysisResult(savedProject.getId(), mockResponse, null);
            logger.info("回调函数调用完成");

            // 验证结果
            Project updatedProject = projectRepository.findById(savedProject.getId()).orElse(null);
            assertNotNull(updatedProject);
            assertEquals(Project.ApprovalStatus.AI_APPROVED, updatedProject.getApprovalStatus());
            assertNotNull(updatedProject.getAiAnalysisId());

            // 验证分析结果存储
            AIAnalysisResult result = aiAnalysisResultRepository.findById(updatedProject.getAiAnalysisId()).orElse(null);
            assertNotNull(result);
            assertEquals(savedProject.getId(), result.getReportId());

            logger.info("回调函数直接调用测试通过");

        } catch (Exception e) {
            logger.error("回调函数调用失败", e);
            fail("回调函数调用不应该失败: " + e.getMessage());
        }

        logger.info("=== AI分析回调函数直接调用测试完成 ===");
    }

    /**
     * 测试AI分析错误处理
     */
    @Test
    @Transactional
    public void testAIAnalysisErrorHandling() {
        logger.info("=== 开始测试AI分析错误处理 ===");

        Project testProject = createTestProject();
        Project savedProject = projectRepository.save(testProject);

        // 模拟错误情况
        Exception mockError = new RuntimeException("模拟AI服务错误");

        // 直接调用回调函数处理错误
        try {
            aiAnalysisService.handleAIAnalysisResult(savedProject.getId(), null, mockError);
            logger.info("错误处理回调完成");

            // 验证错误处理结果
            Project updatedProject = projectRepository.findById(savedProject.getId()).orElse(null);
            assertNotNull(updatedProject);
            assertEquals(Project.ApprovalStatus.AI_REJECTED, updatedProject.getApprovalStatus());
            assertNotNull(updatedProject.getRejectionReason());
            assertTrue(updatedProject.getRejectionReason().contains("模拟AI服务错误"));

            logger.info("错误处理测试通过");

        } catch (Exception e) {
            logger.error("错误处理测试失败", e);
            fail("错误处理不应该抛出异常: " + e.getMessage());
        }

        logger.info("=== AI分析错误处理测试完成 ===");
    }

    /**
     * 创建测试项目
     */
    private Project createTestProject() {
        Project project = new Project();
        project.setName("AI分析测试项目-" + System.currentTimeMillis());
        project.setDescription("这是一个用于测试AI分析功能的项目");
        project.setMembers("测试开发者,测试工程师");
        project.setExpectedResults("验证AI分析功能正常工作");
        project.setTimeline("2小时内完成测试");
        project.setStopLoss("如果测试失败，回滚代码");
        project.setCreatedBy(10004L); // manager1的用户ID
        project.setApprovalStatus(Project.ApprovalStatus.AI_ANALYZING);
        return project;
    }

    /**
     * 等待AI分析完成
     * @param projectId 项目ID
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否在指定时间内完成
     */
    private boolean waitForAnalysisCompletion(Long projectId, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeout = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeout) {
            try {
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project != null && project.getApprovalStatus() != Project.ApprovalStatus.AI_ANALYZING) {
                    logger.info("AI分析完成，最终状态: {}", project.getApprovalStatus());
                    return true;
                }
                
                Thread.sleep(1000); // 等待1秒再检查
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("等待过程被中断", e);
                return false;
            }
        }
        
        logger.warn("AI分析在{}秒内未完成", timeoutSeconds);
        return false;
    }
}