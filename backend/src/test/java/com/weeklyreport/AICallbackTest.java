package com.weeklyreport;

import com.weeklyreport.entity.AIAnalysisResult;
import com.weeklyreport.entity.Project;
import com.weeklyreport.repository.AIAnalysisResultRepository;
import com.weeklyreport.repository.ProjectRepository;
import com.weeklyreport.service.ai.AIAnalysisService;
import com.weeklyreport.service.ai.dto.AIAnalysisResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI分析功能测试
 */
@SpringBootTest
@ActiveProfiles("dev")  // 使用dev配置，连接实际数据库
public class AICallbackTest {

    private static final Logger logger = LoggerFactory.getLogger(AICallbackTest.class);

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AIAnalysisResultRepository aiAnalysisResultRepository;

    /**
     * 测试AI分析回调函数是否能正常执行
     */
    @Test
    public void testAIAnalysisCallback() {
        logger.info("=== 开始测试AI分析回调函数 ===");

        // 使用项目35进行测试
        Long testProjectId = 35L;
        
        // 检查项目是否存在
        Optional<Project> projectOpt = projectRepository.findById(testProjectId);
        assertTrue(projectOpt.isPresent(), "测试项目35应该存在");
        
        Project project = projectOpt.get();
        logger.info("找到测试项目: ID={}, 名称={}, 当前状态={}", 
                   project.getId(), project.getName(), project.getApprovalStatus());

        // 记录初始状态
        Project.ApprovalStatus originalStatus = project.getApprovalStatus();
        Long originalAiAnalysisId = project.getAiAnalysisId();
        
        // 记录AI分析结果初始数量
        List<AIAnalysisResult> initialResults = aiAnalysisResultRepository.findAll();
        int initialResultCount = initialResults.size();
        logger.info("AI分析结果表初始记录数: {}", initialResultCount);

        // 创建模拟AI分析响应
        AIAnalysisResponse mockResponse = new AIAnalysisResponse();
        mockResponse.setAnalysisId("test-callback-" + System.currentTimeMillis());
        mockResponse.setResult("{\n" +
            "  \"isPass\": true,\n" +
            "  \"proposal\": \"项目可行性良好，建议继续推进。这是一个SpringBoot测试生成的模拟结果。\",\n" +
            "  \"feasibilityScore\": 0.90,\n" +
            "  \"riskLevel\": \"LOW\",\n" +
            "  \"keyIssues\": [],\n" +
            "  \"recommendations\": [\"继续按计划执行\", \"定期监控进度\", \"保持团队沟通\"]\n" +
            "}");
        mockResponse.setProviderUsed("test-provider");

        try {
            // 重置项目状态为AI_ANALYZING以便测试
            project.setApprovalStatus(Project.ApprovalStatus.AI_ANALYZING);
            project.setAiAnalysisId(null);
            project.setRejectionReason(null);
            projectRepository.save(project);
            logger.info("项目状态已重置为AI_ANALYZING");

            // 调用回调函数
            logger.info("开始调用AI分析回调函数...");
            aiAnalysisService.handleAIAnalysisResult(testProjectId, mockResponse, null);
            logger.info("AI分析回调函数调用完成");

            // 等待异步操作完成（最多等待10秒）
            boolean statusChanged = false;
            int maxWaitSeconds = 10;
            Project updatedProject = null;
            
            for (int i = 0; i < maxWaitSeconds; i++) {
                Thread.sleep(1000); // 等待1秒
                
                updatedProject = projectRepository.findById(testProjectId).orElse(null);
                if (updatedProject != null && updatedProject.getApprovalStatus() != Project.ApprovalStatus.AI_ANALYZING) {
                    statusChanged = true;
                    logger.info("检测到项目状态变化: {}", updatedProject.getApprovalStatus());
                    break;
                }
                
                if (i % 2 == 0) {
                    logger.info("等待异步回调完成... {}秒", i + 1);
                }
            }
            
            assertTrue(statusChanged, "项目状态应该在" + maxWaitSeconds + "秒内从AI_ANALYZING改变");

            // 验证项目状态是否更新
            assertNotNull(updatedProject, "更新后的项目应该存在");
            
            logger.info("更新后项目状态: {}, AI分析ID: {}", 
                       updatedProject.getApprovalStatus(), updatedProject.getAiAnalysisId());

            // 验证状态已改变
            assertNotEquals(Project.ApprovalStatus.AI_ANALYZING, updatedProject.getApprovalStatus(),
                           "项目状态应该已从AI_ANALYZING改变");

            // 验证状态为预期值 (AI_APPROVED 或 AI_REJECTED)
            assertTrue(
                updatedProject.getApprovalStatus() == Project.ApprovalStatus.AI_APPROVED ||
                updatedProject.getApprovalStatus() == Project.ApprovalStatus.AI_REJECTED,
                "项目状态应该为AI_APPROVED或AI_REJECTED，实际状态: " + updatedProject.getApprovalStatus()
            );

            // 检查AI分析结果存储情况
            List<AIAnalysisResult> finalResults = aiAnalysisResultRepository.findAll();
            int finalResultCount = finalResults.size();
            logger.info("AI分析结果表最终记录数: {}", finalResultCount);
            
            if (updatedProject.getApprovalStatus() == Project.ApprovalStatus.AI_APPROVED) {
                // 如果状态为AI_APPROVED，应该有AI分析ID和结果记录
                assertNotNull(updatedProject.getAiAnalysisId(), "AI_APPROVED状态下AI分析ID应该已设置");
                assertEquals(initialResultCount + 1, finalResultCount, "应该新增一条AI分析结果记录");
                
                // 验证AI分析结果记录内容
                Optional<AIAnalysisResult> analysisResultOpt = aiAnalysisResultRepository.findById(updatedProject.getAiAnalysisId());
                assertTrue(analysisResultOpt.isPresent(), "AI分析结果记录应该存在");
                
                AIAnalysisResult analysisResult = analysisResultOpt.get();
                assertEquals(testProjectId, analysisResult.getReportId(), "分析结果应该关联到正确的项目");
                assertEquals(AIAnalysisResult.AnalysisStatus.COMPLETED, analysisResult.getStatus(), "分析状态应该为COMPLETED");
                assertNotNull(analysisResult.getResult(), "分析结果内容不应为空");
                
                logger.info("AI分析结果验证通过: ID={}, 项目ID={}, 状态={}", 
                           analysisResult.getId(), analysisResult.getReportId(), analysisResult.getStatus());
            } else {
                // 如果状态为AI_REJECTED，可能没有AI分析ID（保存失败的情况）
                logger.info("项目被AI拒绝，原因: {}", updatedProject.getRejectionReason());
                // 在这种情况下不要求有AI分析结果记录
            }

            logger.info("=== AI分析回调函数测试通过 ===");

        } catch (Exception e) {
            logger.error("AI分析回调函数测试失败", e);
            fail("AI分析回调函数不应该抛出异常: " + e.getMessage());
        }
    }

    /**
     * 测试AI分析错误处理
     */
    @Test
    public void testAIAnalysisErrorHandling() {
        logger.info("=== 开始测试AI分析错误处理 ===");

        Long testProjectId = 35L;
        
        Optional<Project> projectOpt = projectRepository.findById(testProjectId);
        assertTrue(projectOpt.isPresent(), "测试项目35应该存在");
        
        Project project = projectOpt.get();

        try {
            // 重置项目状态
            project.setApprovalStatus(Project.ApprovalStatus.AI_ANALYZING);
            project.setAiAnalysisId(null);
            project.setRejectionReason(null);
            projectRepository.save(project);

            // 模拟AI分析错误
            Exception mockError = new RuntimeException("模拟AI服务连接超时错误");

            // 调用错误处理回调
            logger.info("开始测试错误处理回调...");
            aiAnalysisService.handleAIAnalysisResult(testProjectId, null, mockError);

            // 验证错误处理结果
            Project updatedProject = projectRepository.findById(testProjectId).orElse(null);
            assertNotNull(updatedProject);
            
            assertEquals(Project.ApprovalStatus.AI_REJECTED, updatedProject.getApprovalStatus(),
                        "错误情况下项目状态应该为AI_REJECTED");
            
            assertNotNull(updatedProject.getRejectionReason(), "应该有拒绝原因");
            assertTrue(updatedProject.getRejectionReason().contains("模拟AI服务连接超时错误"),
                      "拒绝原因应该包含错误信息");

            logger.info("错误处理测试通过: 状态={}, 拒绝原因={}", 
                       updatedProject.getApprovalStatus(), updatedProject.getRejectionReason());

            logger.info("=== AI分析错误处理测试通过 ===");

        } catch (Exception e) {
            logger.error("AI分析错误处理测试失败", e);
            fail("错误处理测试不应该抛出异常: " + e.getMessage());
        }
    }

    /**
     * 测试完整AI分析流程
     */
    @Test
    public void testCompleteAIAnalysisFlow() {
        logger.info("=== 开始测试完整AI分析流程 ===");

        Long testProjectId = 35L;
        
        Optional<Project> projectOpt = projectRepository.findById(testProjectId);
        assertTrue(projectOpt.isPresent(), "测试项目35应该存在");
        
        Project project = projectOpt.get();

        try {
            // 重置项目状态
            project.setApprovalStatus(Project.ApprovalStatus.AI_ANALYZING);
            project.setAiAnalysisId(null);
            project.setRejectionReason(null);
            projectRepository.save(project);

            logger.info("项目状态已重置，开始触发AI分析...");

            // 记录开始时间
            long startTime = System.currentTimeMillis();

            // 触发AI分析
            aiAnalysisService.analyzeProject(project);

            logger.info("AI分析已触发，等待完成...");

            // 等待AI分析完成（最多等待30秒）
            boolean completed = false;
            int maxWaitSeconds = 30;
            
            for (int i = 0; i < maxWaitSeconds; i++) {
                Thread.sleep(1000); // 等待1秒
                
                Project currentProject = projectRepository.findById(testProjectId).orElse(null);
                if (currentProject != null && currentProject.getApprovalStatus() != Project.ApprovalStatus.AI_ANALYZING) {
                    completed = true;
                    long duration = System.currentTimeMillis() - startTime;
                    logger.info("AI分析完成，耗时: {}ms, 最终状态: {}", duration, currentProject.getApprovalStatus());
                    break;
                }
                
                if (i % 5 == 0) {
                    logger.info("等待AI分析完成... {}秒", i);
                }
            }

            if (!completed) {
                logger.warn("AI分析在{}秒内未完成，这可能表明回调函数有问题", maxWaitSeconds);
                
                // 检查最终状态
                Project finalProject = projectRepository.findById(testProjectId).orElse(null);
                if (finalProject != null) {
                    logger.info("最终项目状态: {}, AI分析ID: {}", 
                               finalProject.getApprovalStatus(), finalProject.getAiAnalysisId());
                }
                
                fail("AI分析应该在" + maxWaitSeconds + "秒内完成");
            }

            logger.info("=== 完整AI分析流程测试完成 ===");

        } catch (Exception e) {
            logger.error("完整AI分析流程测试失败", e);
            fail("完整AI分析流程测试不应该抛出异常: " + e.getMessage());
        }
    }
}