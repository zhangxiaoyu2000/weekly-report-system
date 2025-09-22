package com.weeklyreport.controller;

import com.weeklyreport.dto.ApiResponse;
import com.weeklyreport.entity.AIAnalysisResult;
import com.weeklyreport.entity.Project;
import com.weeklyreport.repository.AIAnalysisResultRepository;
import com.weeklyreport.repository.ProjectRepository;
import com.weeklyreport.service.ai.AIAnalysisService;
import com.weeklyreport.service.ai.dto.AIAnalysisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AI分析功能测试控制器
 */
@RestController
@RequestMapping("/ai-test")
@CrossOrigin(origins = "*")
public class AITestController {

    private static final Logger logger = LoggerFactory.getLogger(AITestController.class);

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AIAnalysisResultRepository aiAnalysisResultRepository;

    /**
     * 测试AI分析服务基本功能
     * GET /api/ai-test/basic
     */
    @GetMapping("/basic")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testBasicFunctionality() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 检查AI服务是否可用
            boolean aiAvailable = aiAnalysisService.isAIAvailable();
            result.put("aiServiceAvailable", aiAvailable);
            
            // 检查提供商状态
            Map<String, Object> providerStatus = aiAnalysisService.getProvidersStatus();
            result.put("providerStatus", providerStatus);
            
            logger.info("AI服务基本功能测试完成: {}", result);
            return ResponseEntity.ok(ApiResponse.success("AI服务基本功能测试完成", result));
            
        } catch (Exception e) {
            logger.error("AI服务基本功能测试失败", e);
            return ResponseEntity.ok(ApiResponse.error("AI服务基本功能测试失败: " + e.getMessage()));
        }
    }

    /**
     * 测试AI分析回调函数 - 无需认证的测试接口
     * POST /api/ai-test/callback/{projectId}
     */
    @PostMapping("/callback/{projectId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testCallback(@PathVariable Long projectId) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 检查项目是否存在
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("项目不存在: " + projectId));
            }
            
            Project project = projectOpt.get();
            result.put("originalStatus", project.getApprovalStatus().toString());
            result.put("originalAiAnalysisId", project.getAiAnalysisId());
            
            // 记录分析结果初始数量
            List<AIAnalysisResult> initialResults = aiAnalysisResultRepository.findAll();
            int initialCount = initialResults.size();
            result.put("initialAnalysisResultCount", initialCount);
            
            // 创建模拟AI分析响应
            AIAnalysisResponse mockResponse = new AIAnalysisResponse();
            mockResponse.setAnalysisId("test-callback-" + System.currentTimeMillis());
            mockResponse.setResult("{\n" +
                "  \"isPass\": true,\n" +
                "  \"proposal\": \"项目可行性良好，建议继续推进。测试回调函数功能。\",\n" +
                "  \"feasibilityScore\": 0.85,\n" +
                "  \"riskLevel\": \"LOW\",\n" +
                "  \"keyIssues\": [],\n" +
                "  \"recommendations\": [\"继续按计划执行\", \"定期监控进度\"]\n" +
                "}");
            mockResponse.setProviderUsed("test-provider");
            
            logger.info("开始测试回调函数，项目ID: {}", projectId);
            
            // 使用测试方法调用回调函数
            aiAnalysisService.testCallbackExecution(projectId);
            
            logger.info("回调函数调用完成，检查结果");
            
            // 检查结果
            Project updatedProject = projectRepository.findById(projectId).orElse(null);
            if (updatedProject != null) {
                result.put("updatedStatus", updatedProject.getApprovalStatus().toString());
                result.put("updatedAiAnalysisId", updatedProject.getAiAnalysisId());
                result.put("rejectionReason", updatedProject.getRejectionReason());
            }
            
            // 检查分析结果数量变化
            List<AIAnalysisResult> finalResults = aiAnalysisResultRepository.findAll();
            int finalCount = finalResults.size();
            result.put("finalAnalysisResultCount", finalCount);
            result.put("analysisResultAdded", finalCount > initialCount);
            
            // 如果有AI分析ID，检查分析结果详情
            if (updatedProject != null && updatedProject.getAiAnalysisId() != null) {
                Optional<AIAnalysisResult> analysisResultOpt = aiAnalysisResultRepository.findById(updatedProject.getAiAnalysisId());
                if (analysisResultOpt.isPresent()) {
                    AIAnalysisResult analysisResult = analysisResultOpt.get();
                    result.put("analysisResultExists", true);
                    result.put("analysisResultProjectId", analysisResult.getReportId());
                    result.put("analysisResultType", analysisResult.getAnalysisType().toString());
                    result.put("analysisResultStatus", analysisResult.getStatus().toString());
                } else {
                    result.put("analysisResultExists", false);
                }
            }
            
            result.put("callbackTestSuccess", true);
            logger.info("回调函数测试完成: {}", result);
            
            return ResponseEntity.ok(ApiResponse.success("回调函数测试完成", result));
            
        } catch (Exception e) {
            logger.error("回调函数测试失败，项目ID: {}", projectId, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("callbackTestSuccess", false);
            errorResult.put("error", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("回调函数测试失败: " + e.getMessage()));
        }
    }

    /**
     * 测试AI分析完整流程
     * POST /api/ai-test/full-flow/{projectId}
     */
    @PostMapping("/full-flow/{projectId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testFullFlow(@PathVariable Long projectId) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 检查项目是否存在
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("项目不存在: " + projectId));
            }
            
            Project project = projectOpt.get();
            logger.info("开始测试完整AI分析流程，项目ID: {}, 当前状态: {}", projectId, project.getApprovalStatus());
            
            // 重置项目状态为AI_ANALYZING
            project.setApprovalStatus(Project.ApprovalStatus.AI_ANALYZING);
            project.setAiAnalysisId(null);
            project.setRejectionReason(null);
            projectRepository.save(project);
            
            result.put("projectReset", true);
            result.put("resetStatus", project.getApprovalStatus().toString());
            
            // 记录分析结果初始数量
            List<AIAnalysisResult> initialResults = aiAnalysisResultRepository.findAll();
            int initialCount = initialResults.size();
            result.put("initialAnalysisResultCount", initialCount);
            
            // 触发AI分析
            logger.info("触发AI分析...");
            aiAnalysisService.analyzeProject(project);
            result.put("aiAnalysisTriggered", true);
            
            // 等待几秒让分析完成
            Thread.sleep(5000);
            
            // 检查最终结果
            Project finalProject = projectRepository.findById(projectId).orElse(null);
            if (finalProject != null) {
                result.put("finalStatus", finalProject.getApprovalStatus().toString());
                result.put("finalAiAnalysisId", finalProject.getAiAnalysisId());
                result.put("statusChanged", !finalProject.getApprovalStatus().equals(Project.ApprovalStatus.AI_ANALYZING));
            }
            
            // 检查分析结果数量变化
            List<AIAnalysisResult> finalResults = aiAnalysisResultRepository.findAll();
            int finalCount = finalResults.size();
            result.put("finalAnalysisResultCount", finalCount);
            result.put("newAnalysisResultAdded", finalCount > initialCount);
            
            logger.info("完整AI分析流程测试完成: {}", result);
            
            return ResponseEntity.ok(ApiResponse.success("完整AI分析流程测试完成", result));
            
        } catch (Exception e) {
            logger.error("完整AI分析流程测试失败，项目ID: {}", projectId, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("fullFlowTestSuccess", false);
            errorResult.put("error", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("完整AI分析流程测试失败: " + e.getMessage()));
        }
    }

    /**
     * 检查AI分析结果数据
     * GET /api/ai-test/analysis-results
     */
    @GetMapping("/analysis-results")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkAnalysisResults() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 获取所有AI分析结果
            List<AIAnalysisResult> allResults = aiAnalysisResultRepository.findAll();
            result.put("totalAnalysisResults", allResults.size());
            
            // 获取最近的几个结果
            List<AIAnalysisResult> recentResults = allResults.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .toList();
            
            result.put("recentResults", recentResults.stream().map(r -> {
                Map<String, Object> resultInfo = new HashMap<>();
                resultInfo.put("id", r.getId());
                resultInfo.put("reportId", r.getReportId());
                resultInfo.put("analysisType", r.getAnalysisType().toString());
                resultInfo.put("status", r.getStatus().toString());
                resultInfo.put("createdAt", r.getCreatedAt().toString());
                return resultInfo;
            }).toList());
            
            return ResponseEntity.ok(ApiResponse.success("AI分析结果检查完成", result));
            
        } catch (Exception e) {
            logger.error("检查AI分析结果失败", e);
            return ResponseEntity.ok(ApiResponse.error("检查AI分析结果失败: " + e.getMessage()));
        }
    }
}