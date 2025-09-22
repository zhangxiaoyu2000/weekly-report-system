package com.weeklyreport.service.ai;

import com.weeklyreport.config.AIConfig;
import com.weeklyreport.entity.AIAnalysisResult;
import com.weeklyreport.entity.SimpleProject;
import com.weeklyreport.entity.WeeklyReport;
import com.weeklyreport.repository.AIAnalysisResultRepository;
import com.weeklyreport.repository.ProjectRepository;
import com.weeklyreport.repository.WeeklyReportRepository;
import com.weeklyreport.service.ai.dto.AIAnalysisRequest;
import com.weeklyreport.service.ai.dto.AIAnalysisResponse;
import com.weeklyreport.service.ai.exception.AIServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Main AI analysis service that coordinates different AI providers
 */
@Service
public class AIAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisService.class);
    
    private final AIServiceFactory aiServiceFactory;
    private final AIConfig aiConfig;
    private final RetryTemplate retryTemplate;
    private final WeeklyReportRepository weeklyReportRepository;
    private final AIAnalysisResultRepository aiAnalysisResultRepository;
    private final ProjectRepository projectRepository;
    private final AICallbackTransactionService callbackTransactionService;
    
    @Autowired
    public AIAnalysisService(AIServiceFactory aiServiceFactory, 
                           AIConfig aiConfig,
                           @Qualifier("aiRetryTemplate") RetryTemplate retryTemplate,
                           WeeklyReportRepository weeklyReportRepository,
                           AIAnalysisResultRepository aiAnalysisResultRepository,
                           ProjectRepository projectRepository,
                           AICallbackTransactionService callbackTransactionService) {
        this.aiServiceFactory = aiServiceFactory;
        this.aiConfig = aiConfig;
        this.retryTemplate = retryTemplate;
        this.weeklyReportRepository = weeklyReportRepository;
        this.aiAnalysisResultRepository = aiAnalysisResultRepository;
        this.projectRepository = projectRepository;
        this.callbackTransactionService = callbackTransactionService;
    }
    
    /**
     * Perform AI analysis with automatic provider selection and retry logic
     */
    @Retryable(
        value = {AIServiceException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public AIAnalysisResponse analyzeContent(AIAnalysisRequest request) throws AIServiceException {
        return analyzeContent(request, null);
    }
    
    /**
     * Perform AI analysis with specific provider
     */
    @Retryable(
        value = {AIServiceException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public AIAnalysisResponse analyzeContent(AIAnalysisRequest request, String providerCode) 
            throws AIServiceException {
        
        if (!aiConfig.isEnabled()) {
            throw new AIServiceException("AI services are disabled");
        }
        
        logger.info("Starting AI analysis - Type: {}, Provider: {}", 
                   request.getAnalysisType(), providerCode != null ? providerCode : "default");
        
        return retryTemplate.execute(context -> {
            try {
                AIServiceProvider provider = providerCode != null 
                    ? aiServiceFactory.getProvider(providerCode)
                    : aiServiceFactory.getDefaultProvider();
                
                AIAnalysisResponse response = provider.analyze(request);
                
                logger.info("AI analysis completed successfully - ID: {}, Provider: {}", 
                           response.getAnalysisId(), response.getProviderUsed());
                
                return response;
                
            } catch (AIServiceException e) {
                logger.error("AI analysis attempt {} failed: {}", 
                           context.getRetryCount() + 1, e.getMessage());
                throw e;
            }
        });
    }
    
    /**
     * Analyze a Project entity (new unified project structure)
     */
    public void analyzeProject(com.weeklyreport.entity.Project project) throws AIServiceException {
        logger.info("=== 开始AI分析项目 ===\n项目ID: {}\n项目名称: {}\n项目描述: {}\n项目成员: {}\n预期结果: {}\n时间线: {}\n止损点: {}", 
                   project.getId(), project.getName(), project.getDescription(), 
                   project.getMembers(), project.getExpectedResults(), 
                   project.getTimeline(), project.getStopLoss());
        
        try {
            // Create structured analysis prompt for project feasibility
            String analysisPrompt = String.format("""
                你是一位资深的项目管理专家，请分析以下项目的可行性：
                
                项目基本信息：
                - 项目名称：%s
                - 项目内容：%s
                - 项目成员：%s
                - 预期结果：%s
                - 时间线：%s
                - 止损点：%s
                
                请从以下维度进行分析并给出建议：
                1. 项目可行性评估（技术、资源、时间）
                2. 风险评估和建议
                3. 项目优化建议
                4. 总体评价（通过/不通过）
                
                请以结构化的形式返回分析结果。
                """, 
                project.getName() != null ? project.getName() : "未指定",
                project.getDescription() != null ? project.getDescription() : "未指定",
                project.getMembers() != null ? project.getMembers() : "未指定",
                project.getExpectedResults() != null ? project.getExpectedResults() : "未指定", 
                project.getTimeline() != null ? project.getTimeline() : "未指定",
                project.getStopLoss() != null ? project.getStopLoss() : "未指定");
            
            logger.info("=== AI分析提示词 ===\n{}", analysisPrompt);
            
            AIAnalysisRequest request = new AIAnalysisRequest(
                analysisPrompt,
                AIAnalysisRequest.AnalysisType.PROJECT_EVALUATION
            );
            request.setContext(project.getId().toString());
            
            logger.info("=== 发送AI分析请求 ===\n项目ID: {}\n分析类型: {}", project.getId(), request.getAnalysisType());
            
            // 异步执行AI分析
            CompletableFuture<AIAnalysisResponse> analysisResult = analyzeContentAsync(request);
            
            logger.info("=== CompletableFuture已创建 ===\n项目ID: {}", project.getId());
            
            // 设置回调来处理AI分析结果
            analysisResult.whenComplete((response, throwable) -> {
                logger.info("=== AI分析回调被触发 ===\n项目ID: {}", project.getId());
                try {
                    if (throwable != null) {
                        logger.error("=== AI分析失败 ===\n项目ID: {}\n错误: {}", project.getId(), throwable.getMessage());
                    } else {
                        logger.info("=== AI分析成功 ===\n项目ID: {}\n分析ID: {}\n提供商: {}\n结果长度: {} 字符", 
                                   project.getId(), response.getAnalysisId(), response.getProviderUsed(), 
                                   response.getResult() != null ? response.getResult().length() : 0);
                        logger.info("=== AI分析结果内容 ===\n{}", response.getResult());
                    }
                    handleAIAnalysisResult(project.getId(), response, throwable);
                } catch (Exception e) {
                    logger.error("=== AI分析回调处理失败 ===\n项目ID: {}\n异常: {}", project.getId(), e.getMessage(), e);
                }
            });
            
            logger.info("=== 回调已注册，AI分析已启动 ===\n项目ID: {}", project.getId());
            
        } catch (Exception e) {
            logger.error("Error analyzing unified project: {}", project.getId(), e);
            throw new AIServiceException("Failed to analyze project: " + e.getMessage(), e);
        }
    }

    /**
     * Perform AI analysis asynchronously
     */
    public CompletableFuture<AIAnalysisResponse> analyzeContentAsync(AIAnalysisRequest request) {
        return analyzeContentAsync(request, null);
    }
    
    /**
     * Perform AI analysis asynchronously with specific provider
     */
    public CompletableFuture<AIAnalysisResponse> analyzeContentAsync(AIAnalysisRequest request, 
                                                                    String providerCode) {
        if (!aiConfig.isEnabled()) {
            CompletableFuture<AIAnalysisResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new AIServiceException("AI services are disabled"));
            return future;
        }
        
        logger.info("Starting async AI analysis - Type: {}, Provider: {}", 
                   request.getAnalysisType(), providerCode != null ? providerCode : "default");
        
        try {
            AIServiceProvider provider = providerCode != null 
                ? aiServiceFactory.getProvider(providerCode)
                : aiServiceFactory.getDefaultProvider();
            
            return provider.analyzeAsync(request)
                .whenComplete((response, throwable) -> {
                    if (throwable != null) {
                        logger.error("Async AI analysis failed", throwable);
                    } else {
                        logger.info("Async AI analysis completed - ID: {}, Provider: {}", 
                                   response.getAnalysisId(), response.getProviderUsed());
                    }
                });
                
        } catch (Exception e) {
            logger.error("Failed to start async AI analysis", e);
            CompletableFuture<AIAnalysisResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new AIServiceException("Failed to start async analysis", e));
            return future;
        }
    }
    
    /**
     * Get available AI providers and their status
     */
    public Map<String, Object> getProvidersStatus() {
        return aiServiceFactory.getProviderStatus();
    }
    
    /**
     * Check if AI services are available
     */
    public boolean isAIAvailable() {
        return aiConfig.isEnabled() && aiServiceFactory.isAIEnabled();
    }
    
    /**
     * Get cost estimate for analysis
     */
    public double getCostEstimate(String content, String providerCode) {
        try {
            AIServiceProvider provider = providerCode != null 
                ? aiServiceFactory.getProvider(providerCode)
                : aiServiceFactory.getDefaultProvider();
            
            return provider.getCostEstimate(content);
        } catch (Exception e) {
            logger.warn("Failed to get cost estimate: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Validate analysis request
     */
    public void validateRequest(AIAnalysisRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Analysis request cannot be null");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        if (request.getAnalysisType() == null) {
            throw new IllegalArgumentException("Analysis type cannot be null");
        }
    }
    
    /**
     * Analyze weekly report asynchronously
     */
    public CompletableFuture<List<AIAnalysisResult>> analyzeWeeklyReportAsync(Long reportId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                WeeklyReport report = weeklyReportRepository.findById(reportId)
                    .orElseThrow(() -> new IllegalArgumentException("Weekly report not found: " + reportId));
                
                // Create analysis request from report
                AIAnalysisRequest request = new AIAnalysisRequest();
                request.setContent(report.getContent());
                request.setAnalysisType(com.weeklyreport.service.ai.dto.AIAnalysisRequest.AnalysisType.SUMMARY);
                
                // Perform analysis
                AIAnalysisResponse response = analyzeContent(request);
                
                // Convert to analysis results
                return List.of(); // Return empty list for now
                
            } catch (Exception e) {
                logger.error("Failed to analyze weekly report {}", reportId, e);
                throw new RuntimeException("Analysis failed", e);
            }
        });
    }
    
    /**
     * Get analysis results for a weekly report
     */
    public List<AIAnalysisResult> getAnalysisResults(Long reportId) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Weekly report not found: " + reportId));
        
        // Return empty list since analysis results are not stored in the simplified entity
        return List.of();
    }

    /**
     * Analyze a project and return analysis result
     */
    public String analyzeProject(SimpleProject project) throws AIServiceException {
        logger.info("Starting project analysis for project ID: {}", project.getId());
        
        try {
            // Create structured analysis prompt for project feasibility
            String analysisPrompt = String.format("""
                你是一位资深的项目管理专家，请分析以下项目的可行性：
                
                项目基本信息：
                - 项目名称：%s
                - 项目内容：%s
                - 项目成员：%s
                - 预期结果：%s
                - 时间计划：%s
                - 止损点：%s
                
                请从以下维度进行分析：
                1. 项目目标的明确性和可实现性
                2. 资源配置的合理性
                3. 时间规划的现实性
                4. 风险控制的充分性
                5. 预期结果的可衡量性
                
                请以JSON格式返回分析结果，并根据你的分析给出置信度评分：
                {
                    "confidence": 0.0-1.0,
                    "proposal": "详细的分析意见",
                    "feasibilityScore": 0.0-1.0,
                    "riskLevel": "LOW/MEDIUM/HIGH",
                    "keyIssues": ["问题1", "问题2"],
                    "recommendations": ["建议1", "建议2"]
                }
                
                置信度说明：
                - 0.0-0.7: 项目不合适/不通过，存在问题需要改进
                - 0.7-1.0: 项目合适/通过，可以进入下一阶段
                - 具体评分应基于项目可行性、资源匹配度、风险控制等综合评估
                """,
                project.getProjectName(),
                project.getProjectContent(),
                project.getProjectMembers(),
                project.getExpectedResults(),
                project.getTimeline(),
                project.getStopLoss()
            );
            
            // Create analysis request for project
            AIAnalysisRequest request = new AIAnalysisRequest();
            request.setContent(analysisPrompt);
            request.setAnalysisType(AIAnalysisRequest.AnalysisType.PROJECT_EVALUATION);
            
            // Perform analysis
            AIAnalysisResponse response = analyzeContent(request);
            
            logger.info("Project analysis completed for project ID: {}", project.getId());
            return response.getResult();
            
        } catch (Exception e) {
            logger.error("Failed to analyze project {}", project.getId(), e);
            throw new AIServiceException("项目AI分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * Analyze a weekly report asynchronously - NEW ASYNC VERSION
     */
    @org.springframework.scheduling.annotation.Async
    public CompletableFuture<AIAnalysisResult> analyzeWeeklyReportAsync(WeeklyReport report) {
        logger.info("🤖 =============异步AI周报分析开始=============");
        logger.info("🤖 周报ID: {}", report.getId());
        logger.info("🤖 周报标题: {}", report.getTitle());
        logger.info("🤖 周报周次: {}", report.getReportWeek());
        logger.info("🤖 用户ID: {}", report.getUserId());
        logger.info("🤖 当前状态: {}", report.getApprovalStatus());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Call the synchronous analysis method
                AIAnalysisResult result = analyzeWeeklyReportSync(report);
                logger.info("🤖 =============异步AI周报分析成功完成=============");
                return result;
            } catch (Exception e) {
                logger.error("🤖 ❌ 异步AI周报分析失败，周报ID: {}", report.getId(), e);
                
                // Update weekly report status to AI_REJECTED on failure
                try {
                    report.setApprovalStatus(WeeklyReport.ApprovalStatus.AI_REJECTED);
                    weeklyReportRepository.save(report);
                    logger.info("🤖 周报状态已更新为AI_REJECTED due to async analysis failure");
                } catch (Exception statusUpdateException) {
                    logger.error("🤖 ❌ 更新周报状态失败", statusUpdateException);
                }
                
                throw new RuntimeException("异步周报AI分析失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Analyze a weekly report and return analysis result (SYNCHRONOUS VERSION)
     */
    public AIAnalysisResult analyzeWeeklyReportSync(WeeklyReport report) throws AIServiceException {
        logger.info("🤖 =============AI周报分析开始=============");
        logger.info("🤖 周报ID: {}", report.getId());
        logger.info("🤖 周报标题: {}", report.getTitle());
        logger.info("🤖 周报周次: {}", report.getReportWeek());
        logger.info("🤖 用户ID: {}", report.getUserId());
        logger.info("🤖 当前状态: {}", report.getApprovalStatus());
        
        try {
            // Create structured analysis prompt for weekly report quality
            String analysisPrompt = String.format("""
                你是一位经验丰富的工作汇报审核专家，请评估以下周报的质量：
                
                周报信息：
                - 标题：%s
                - 报告周次：%s
                - 内容：%s
                - 额外说明：%s
                - 发展机会：%s
                
                请从以下维度进行全面评估：
                1. 工作内容的完整性和详细程度
                2. 工作成果的具体性和可衡量性
                3. 问题识别的准确性和深度
                4. 下周规划的合理性和可行性
                5. 整体表达的专业性和清晰度
                
                请以结构化的JSON格式返回评估结果，并根据周报质量给出置信度评分：
                {
                    "overallScore": 85,
                    "confidence": 0.85,
                    "proposal": "详细的评估意见和改进建议，包含具体的改进方向",
                    "qualityScore": 0.85,
                    "riskLevel": "LOW",
                    "suggestions": ["建议在日常任务执行中加强细节记录", "发展性任务的进度跟踪可以更加量化"],
                    "improvementAreas": ["任务执行效率", "结果量化表述"],
                    "positiveAspects": ["任务完成度较高", "工作态度积极"],
                    "riskAssessment": "低风险，整体表现稳定",
                    "detailedFeedback": {
                        "routine_tasks": {"score": 80, "feedback": "日常任务完成质量良好"},
                        "developmental_tasks": {"score": 90, "feedback": "发展性任务展现出良好的项目推进能力"},
                        "planning_quality": {"score": 85, "feedback": "下周规划合理，目标明确"}
                    }
                }
                
                置信度说明：
                - 0.0-0.7: 周报质量不合格，需要重新编写
                - 0.7-1.0: 周报质量合格，可以通过审核
                - 具体评分应基于内容完整性、工作成果、专业性等综合评估
                """,
                report.getTitle() != null ? report.getTitle() : "无标题",
                report.getReportWeek() != null ? report.getReportWeek() : "未指定",
                report.getContent() != null ? report.getContent() : "无内容",
                report.getAdditionalNotes() != null ? report.getAdditionalNotes() : "无",
                report.getDevelopmentOpportunities() != null ? report.getDevelopmentOpportunities() : "无"
            );
            
            logger.info("🤖 AI分析提示词已生成，长度: {} 字符", analysisPrompt.length());
            
            // Create analysis request for weekly report
            AIAnalysisRequest request = new AIAnalysisRequest();
            request.setContent(analysisPrompt);
            request.setAnalysisType(AIAnalysisRequest.AnalysisType.SUMMARY);
            
            logger.info("🤖 发送AI分析请求，分析类型: {}", request.getAnalysisType());
            
            // Perform analysis
            AIAnalysisResponse response = analyzeContent(request);
            
            logger.info("🤖 AI分析响应接收成功");
            logger.info("🤖 分析ID: {}", response.getAnalysisId());
            logger.info("🤖 使用的AI提供商: {}", response.getProviderUsed());
            logger.info("🤖 分析结果长度: {} 字符", response.getResult() != null ? response.getResult().length() : 0);
            logger.info("🤖 置信度: {}", response.getConfidence());
            
            // 详细输出AI分析结果
            logger.info("🤖 ===============AI分析结果详情===============");
            logger.info("🤖 完整AI分析结果:\n{}", response.getResult());
            logger.info("🤖 ==========================================");
            
            // 尝试解析JSON结果来提取具体信息
            if (response.getResult() != null && response.getResult().contains("{")) {
                try {
                    // 简单的JSON信息提取（不使用JSON库，避免依赖问题）
                    String result = response.getResult();
                    
                    // 提取总分
                    if (result.contains("overallScore")) {
                        String scoreStr = extractJsonValue(result, "overallScore");
                        logger.info("🤖 总体评分: {}/100", scoreStr);
                    }
                    
                    // 提取是否通过
                    if (result.contains("isPass")) {
                        String passStr = extractJsonValue(result, "isPass");
                        logger.info("🤖 是否通过: {}", passStr);
                    }
                    
                    // 提取风险等级
                    if (result.contains("riskLevel")) {
                        String riskStr = extractJsonValue(result, "riskLevel");
                        logger.info("🤖 风险等级: {}", riskStr);
                    }
                    
                    // 提取建议
                    if (result.contains("suggestions")) {
                        logger.info("🤖 AI建议:");
                        // 简单提取建议内容
                        if (result.contains("建议")) {
                            String[] lines = result.split("\n");
                            for (String line : lines) {
                                if (line.contains("建议") && !line.trim().isEmpty()) {
                                    logger.info("🤖   - {}", line.trim());
                                }
                            }
                        }
                    }
                    
                } catch (Exception e) {
                    logger.warn("🤖 解析AI分析结果JSON时出现错误，但不影响整体流程: {}", e.getMessage());
                }
            }
            
            // Extract confidence from AI response or use default
            double confidence = 0.5; // Default to threshold value if extraction fails
            try {
                if (response.getResult() != null && response.getResult().contains("confidence")) {
                    String confidenceStr = extractJsonValue(response.getResult(), "confidence");
                    confidence = Double.parseDouble(confidenceStr);
                    logger.info("🤖 从AI结果中提取到置信度: {}", confidence);
                }
            } catch (Exception e) {
                logger.warn("🤖 提取置信度失败，使用默认值: {} - 错误: {}", confidence, e.getMessage());
            }
            
            // AI分析结果清理已由WeeklyReportService.updateWeeklyReport()处理
            // 这里只负责创建新的AI分析结果，避免重复清理导致的竞争条件
            logger.info("🤖 开始创建新的AI分析结果（旧结果清理已在更新时完成）");
            
            // Create and save new analysis result
            AIAnalysisResult analysisResult = new AIAnalysisResult();
            analysisResult.setReportId(report.getId());
            analysisResult.setEntityType(AIAnalysisResult.EntityType.WEEKLY_REPORT);
            analysisResult.setAnalysisType(AIAnalysisResult.AnalysisType.SUMMARY);
            analysisResult.setResult(response.getResult());
            analysisResult.setConfidence(confidence);
            analysisResult.setModelVersion(response.getProviderUsed());
            analysisResult.setStatus(AIAnalysisResult.AnalysisStatus.COMPLETED);
            
            // Save analysis result to database
            AIAnalysisResult savedResult = aiAnalysisResultRepository.save(analysisResult);
            logger.info("🤖 新的AI分析结果已保存到数据库，ID: {}", savedResult.getId());
            
            // Update weekly report status based on confidence
            boolean isApproved = confidence >= 0.7;
            WeeklyReport.ApprovalStatus newStatus = isApproved ? 
                WeeklyReport.ApprovalStatus.ADMIN_REVIEWING : 
                WeeklyReport.ApprovalStatus.AI_REJECTED;
            
            logger.info("🤖 ===============AI分析结果判断===============");
            logger.info("🤖 置信度: {}", confidence);
            logger.info("🤖 通过阈值: 0.7");
            logger.info("🤖 是否通过: {}", isApproved ? "是" : "否");
            logger.info("🤖 原状态: {}", report.getApprovalStatus());
            logger.info("🤖 新状态: {}", newStatus);
            logger.info("🤖 ============================================");
            
            // Update weekly report with analysis result and new status
            report.setAiAnalysisId(savedResult.getId());
            report.setApprovalStatus(newStatus);
            weeklyReportRepository.save(report);
            
            logger.info("🤖 周报状态已更新");
            logger.info("🤖 AI分析结果ID: {}", savedResult.getId());
            logger.info("🤖 关联周报ID: {}", savedResult.getReportId());
            logger.info("🤖 模型版本: {}", savedResult.getModelVersion());
            logger.info("🤖 置信度: {}", savedResult.getConfidence());
            logger.info("🤖 最终状态: {}", report.getApprovalStatus());
            
            logger.info("🤖 =============AI周报分析完成=============");
            return savedResult;
            
        } catch (Exception e) {
            logger.error("🤖 ❌ AI周报分析失败，周报ID: {}", report.getId(), e);
            
            // Update weekly report status to AI_REJECTED on failure
            try {
                report.setApprovalStatus(WeeklyReport.ApprovalStatus.AI_REJECTED);
                weeklyReportRepository.save(report);
                logger.info("🤖 周报状态已更新为AI_REJECTED due to analysis failure");
            } catch (Exception statusUpdateException) {
                logger.error("🤖 ❌ 更新周报状态失败", statusUpdateException);
            }
            
            throw new AIServiceException("周报AI分析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 简单的JSON值提取方法
     */
    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\"";
            int keyIndex = json.indexOf(searchKey);
            if (keyIndex == -1) return "未找到";
            
            int colonIndex = json.indexOf(":", keyIndex);
            if (colonIndex == -1) return "格式错误";
            
            int startIndex = colonIndex + 1;
            // 跳过空格
            while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
                startIndex++;
            }
            
            if (startIndex >= json.length()) return "值为空";
            
            // 确定值的结束位置
            int endIndex;
            char firstChar = json.charAt(startIndex);
            
            if (firstChar == '"') {
                // 字符串值
                startIndex++; // 跳过开始的引号
                endIndex = json.indexOf('"', startIndex);
                if (endIndex == -1) return "字符串未闭合";
                return json.substring(startIndex, endIndex);
            } else {
                // 数字或布尔值
                endIndex = startIndex;
                while (endIndex < json.length() && 
                       json.charAt(endIndex) != ',' && 
                       json.charAt(endIndex) != '}' && 
                       json.charAt(endIndex) != ']' &&
                       !Character.isWhitespace(json.charAt(endIndex))) {
                    endIndex++;
                }
                return json.substring(startIndex, endIndex).trim();
            }
        } catch (Exception e) {
            return "提取失败: " + e.getMessage();
        }
    }
    
    /**
     * 处理AI分析结果的回调方法
     * 注意: 这个方法不应该是异步的，因为它需要确保事务正确执行
     */
    public void handleAIAnalysisResult(Long projectId, AIAnalysisResponse response, Throwable throwable) {
        try {
            logger.info("开始处理AI分析结果，项目ID: {}", projectId);
            
            if (throwable != null) {
                logger.error("AI分析失败，项目ID: {}", projectId, throwable);
                callbackTransactionService.processAIAnalysisFailure(projectId, throwable.getMessage());
            } else {
                logger.info("AI分析完成，项目ID: {}，结果: {}", projectId, response.getAnalysisId());
                callbackTransactionService.processAIAnalysisSuccess(projectId, response);
            }
        } catch (Exception e) {
            logger.error("处理AI分析结果时发生错误，项目ID: {}", projectId, e);
            callbackTransactionService.processAIAnalysisFailure(projectId, e.getMessage());
        }
    }
    
    
    /**
     * 测试回调函数执行 - 用于调试
     */
    public void testCallbackExecution(Long projectId) {
        logger.info("=== 开始测试回调函数执行，项目ID: {} ===", projectId);
        
        // 创建模拟AI分析响应
        AIAnalysisResponse mockResponse = new AIAnalysisResponse();
        mockResponse.setAnalysisId("test-manual-callback-" + System.currentTimeMillis());
        mockResponse.setResult("{\n" +
            "  \"isPass\": true,\n" +
            "  \"proposal\": \"这是一个手动测试回调函数的模拟分析结果\",\n" +
            "  \"feasibilityScore\": 0.88,\n" +
            "  \"riskLevel\": \"LOW\"\n" +
            "}");
        mockResponse.setProviderUsed("manual-test");
        
        try {
            logger.info("调用回调函数处理AI分析结果...");
            handleAIAnalysisResult(projectId, mockResponse, null);
            logger.info("回调函数调用完成");
        } catch (Exception e) {
            logger.error("回调函数调用失败", e);
        }
        
        logger.info("=== 回调函数测试完成 ===");
    }
    
}