package com.weeklyreport.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weeklyreport.ai.dto.DeepSeekRequest;
import com.weeklyreport.ai.dto.DeepSeekResponse;
import com.weeklyreport.ai.entity.AIAnalysisResult;
import com.weeklyreport.ai.repository.AIAnalysisResultRepository;
import com.weeklyreport.weeklyreport.entity.WeeklyReport;
import com.weeklyreport.weeklyreport.repository.WeeklyReportRepository;
import com.weeklyreport.weeklyreport.service.WeeklyReportNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * AI分析服务
 * 负责周报的AI智能分析功能
 */
@Service
@Transactional
public class AIAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    // 项目分析专用的系统提示词 - 模拟专业项目经理角色
    private static final String PROJECT_ANALYSIS_SYSTEM_PROMPT =
        "你是一位资深的项目管理专家(PMP认证)，拥有15年以上的项目管理经验。" +
        "你的职责是评估项目可行性、识别潜在风险、提供专业建议。" +
        "请以JSON格式返回分析结果，包含以下字段：" +
        "{" +
        "  \"feasibility_score\": 0-10分(项目可行性评分)," +
        "  \"risk_level\": \"LOW/MEDIUM/HIGH\"(风险等级)," +
        "  \"key_risks\": [\"风险1\", \"风险2\"](关键风险列表，最多3个)," +
        "  \"strengths\": [\"优势1\", \"优势2\"](项目优势，最多3个)," +
        "  \"recommendations\": [\"建议1\", \"建议2\"](改进建议，最多3个)," +
        "  \"summary\": \"100字以内的总体评价\"" +
        "}" +
        "请确保返回的是合法的JSON格式，不要包含任何其他文本。";

    // 周报分析的系统提示词
    private static final String WEEKLY_REPORT_SYSTEM_PROMPT =
        "你是一位专业的工作汇报分析师，请分析周报内容并以JSON格式返回：" +
        "{" +
        "  \"completeness_score\": 0-10分," +
        "  \"highlights\": [\"亮点1\", \"亮点2\"]," +
        "  \"concerns\": [\"关注点1\", \"关注点2\"]," +
        "  \"suggestions\": [\"建议1\", \"建议2\"]," +
        "  \"summary\": \"总体评价\"" +
        "}";

    @Value("${ai.deepseek.api-key:sk-4613204f1ddc4fcf88894d77be5da3e8}")
    private String deepseekApiKey;

    @Value("${ai.deepseek.base-url:https://api.deepseek.com}")
    private String deepseekBaseUrl;

    @Value("${ai.deepseek.model:deepseek-chat}")
    private String deepseekModel;

    @Value("${ai.deepseek.temperature:0.7}")
    private Double deepseekTemperature;

    @Value("${ai.deepseek.max-tokens:2000}")
    private Integer deepseekMaxTokens;

    @Value("${weekly-report.ai.confidence-threshold:0.7}")
    private double weeklyReportConfidenceThreshold;

    @Autowired
    private AIAnalysisResultRepository aiAnalysisResultRepository;

    @Autowired
    private WeeklyReportNotificationService notificationService;

    @Autowired
    @Qualifier("aiRestTemplate")
    private RestTemplate restTemplate;

    @Autowired(required = false)
    private WeeklyReportRepository weeklyReportRepository;

    @Autowired
    @Qualifier("aiAnalysisExecutor")
    private java.util.concurrent.Executor aiAnalysisExecutor;

    /**
     * 同步分析周报
     */
    public AIAnalysisResult analyzeWeeklyReportSync(WeeklyReport report) {
        logger.info("开始AI分析周报: {}", report.getId());

        try {
            // 创建分析结果
            AIAnalysisResult result = new AIAnalysisResult();
            result.setEntityType(AIAnalysisResult.EntityType.WEEKLY_REPORT);
            result.setReportId(report.getId());
            result.setAnalysisType(AIAnalysisResult.AnalysisType.COMPLETENESS_CHECK);
            result.setStatus(AIAnalysisResult.AnalysisStatus.PROCESSING);
            result.setCreatedAt(LocalDateTime.now());
            result.setUpdatedAt(LocalDateTime.now());

            // 模拟AI分析过程
            performAnalysis(result, report);

            // 保存结果
            result = aiAnalysisResultRepository.save(result);
            updateWeeklyReportStatus(report, result);
            logger.info("AI分析完成: {}", result.getId());

            return result;

        } catch (Exception e) {
            logger.error("AI分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI分析失败: " + e.getMessage());
        }
    }

    /**
     * 执行AI分析 - 调用真实的DeepSeek API
     */
    private void performAnalysis(AIAnalysisResult result, WeeklyReport report) {
        String userPrompt = buildWeeklyReportPrompt(report);
        long startTime = System.currentTimeMillis();

        try {
            // 调用DeepSeek API - 周报分析
            String aiResponse = callDeepSeekAPI(WEEKLY_REPORT_SYSTEM_PROMPT, userPrompt);
            long processingTime = System.currentTimeMillis() - startTime;

            // 解析并验证JSON响应
            Map<String, Object> parsedResult = parseAndValidateJsonResponse(aiResponse);

            // 提取summary作为主要result
            String summary = (String) parsedResult.getOrDefault("summary", aiResponse);

            // 设置分析结果
            result.setResult(summary);
            result.setConfidence(calculateConfidence(parsedResult));
            result.setStatus(AIAnalysisResult.AnalysisStatus.COMPLETED);
            result.setProcessingTimeMs(processingTime);
            result.setModelVersion(deepseekModel);
            result.setUpdatedAt(LocalDateTime.now());

            // 将完整的JSON结果存储在metadata字段
            result.setMetadata(buildMetadata(WEEKLY_REPORT_SYSTEM_PROMPT, userPrompt, aiResponse, parsedResult));

            logger.info("周报AI分析成功，处理时间: {}ms, 结果长度: {}", processingTime, aiResponse.length());
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            logger.error("DeepSeek API调用失败: {}", e.getMessage(), e);

            // 失败时设置错误状态
            result.setResult("AI分析失败: " + e.getMessage());
            result.setConfidence(0.0);
            result.setStatus(AIAnalysisResult.AnalysisStatus.FAILED);
            result.setErrorMessage(e.getMessage());
            result.setProcessingTimeMs(processingTime);
            result.setUpdatedAt(LocalDateTime.now());
        }
    }

    /**
     * 异步分析项目
     */
    @Async("aiAnalysisExecutor")
    public CompletableFuture<AIAnalysisResult> analyzeProjectAsync(com.weeklyreport.project.entity.Project project) {
        logger.info("🚀 启动异步项目分析，项目ID: {}, 线程: {}", project.getId(), Thread.currentThread().getName());

        return CompletableFuture
            .supplyAsync(() -> {
                try {
                    return analyzeProjectSync(project);
                } catch (Exception e) {
                    logger.error("🤖 ❌ 项目分析执行失败，项目ID: {}, 错误: {}", project.getId(), e.getMessage());
                    throw new RuntimeException("项目分析失败: " + e.getMessage(), e);
                }
            }, aiAnalysisExecutor)  // 使用配置的AI分析线程池
            .orTimeout(30, TimeUnit.SECONDS)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    logger.error("🤖 ❌ 异步项目分析失败，项目ID: {}, 错误: {}", 
                               project.getId(), throwable.getMessage());
                } else {
                    logger.info("🤖 ✅ 异步项目分析完成，项目ID: {}, 结果ID: {}", 
                               project.getId(), result.getId());
                }
            });
    }

    /**
     * 同步分析项目 (重命名以保持兼容性)
     */
    public AIAnalysisResult analyzeProjectSync(com.weeklyreport.project.entity.Project project) {
        logger.info("开始AI分析项目: {}", project.getId());

        try {
            // 创建分析结果
            AIAnalysisResult result = new AIAnalysisResult();
            result.setEntityType(AIAnalysisResult.EntityType.PROJECT);
            result.setReportId(project.getId());
            result.setAnalysisType(AIAnalysisResult.AnalysisType.PROGRESS_ANALYSIS);
            result.setStatus(AIAnalysisResult.AnalysisStatus.PROCESSING);
            result.setCreatedAt(LocalDateTime.now());
            result.setUpdatedAt(LocalDateTime.now());

            // 模拟AI分析过程
            performProjectAnalysis(result, project);

            // 保存结果
            result = aiAnalysisResultRepository.save(result);
            logger.info("项目AI分析完成: {}", result.getId());

            return result;
        } catch (Exception e) {
            logger.error("项目AI分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 分析项目 (保持向后兼容)
     */
    public AIAnalysisResult analyzeProject(com.weeklyreport.project.entity.Project project) {
        return analyzeProjectSync(project);
    }

    /**
     * 执行项目分析 - 调用真实的DeepSeek API (专业项目经理角色)
     */
    private void performProjectAnalysis(AIAnalysisResult result, com.weeklyreport.project.entity.Project project) {
        String userPrompt = buildProjectPrompt(project);
        long startTime = System.currentTimeMillis();

        try {
            // 调用DeepSeek API - 使用项目经理角色的系统提示词
            String aiResponse = callDeepSeekAPI(PROJECT_ANALYSIS_SYSTEM_PROMPT, userPrompt);
            long processingTime = System.currentTimeMillis() - startTime;

            // 解析并验证JSON响应
            Map<String, Object> parsedResult = parseAndValidateJsonResponse(aiResponse);

            // 提取summary作为主要result
            String summary = (String) parsedResult.getOrDefault("summary", aiResponse);

            // 根据可行性评分和风险等级计算置信度
            Double feasibilityScore = getDoubleValue(parsedResult.get("feasibility_score"));
            String riskLevel = (String) parsedResult.getOrDefault("risk_level", "MEDIUM");
            Double confidence = calculateProjectConfidence(feasibilityScore, riskLevel);

            // 设置分析结果
            result.setResult(summary);
            result.setConfidence(confidence);
            result.setStatus(AIAnalysisResult.AnalysisStatus.COMPLETED);
            result.setProcessingTimeMs(processingTime);
            result.setModelVersion(deepseekModel);
            result.setUpdatedAt(LocalDateTime.now());

            // 将完整的JSON结果存储在metadata字段
            result.setMetadata(buildMetadata(PROJECT_ANALYSIS_SYSTEM_PROMPT, userPrompt, aiResponse, parsedResult));

            logger.info("项目AI分析成功 - 可行性评分: {}, 风险等级: {}, 置信度: {}, 处理时间: {}ms",
                       feasibilityScore, riskLevel, confidence, processingTime);
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            logger.error("DeepSeek API项目分析失败: {}", e.getMessage(), e);

            // 失败时设置错误状态
            result.setResult("AI分析失败: " + e.getMessage());
            result.setConfidence(0.0);
            result.setStatus(AIAnalysisResult.AnalysisStatus.FAILED);
            result.setErrorMessage(e.getMessage());
            result.setProcessingTimeMs(processingTime);
            result.setUpdatedAt(LocalDateTime.now());
        }
    }

    /**
     * 调用DeepSeek API
     */
    private String callDeepSeekAPI(String systemPrompt, String userPrompt) throws Exception {
        String url = deepseekBaseUrl + "/chat/completions";

        // 构建请求
        DeepSeekRequest request = new DeepSeekRequest();
        request.setModel(deepseekModel);
        request.setTemperature(deepseekTemperature);
        request.setMaxTokens(deepseekMaxTokens);
        request.setStream(false);

        // 设置消息 - 使用传入的systemPrompt
        DeepSeekRequest.Message systemMessage = new DeepSeekRequest.Message("system", systemPrompt);
        DeepSeekRequest.Message userMessage = new DeepSeekRequest.Message("user", userPrompt);
        request.setMessages(List.of(systemMessage, userMessage));

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepseekApiKey);

        HttpEntity<DeepSeekRequest> entity = new HttpEntity<>(request, headers);

        logger.debug("调用DeepSeek API: {}", url);
        logger.debug("请求参数: model={}, temperature={}, maxTokens={}", deepseekModel, deepseekTemperature, deepseekMaxTokens);

        // 发送请求
        ResponseEntity<DeepSeekResponse> response = restTemplate.postForEntity(url, entity, DeepSeekResponse.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("DeepSeek API调用失败，状态码: " + response.getStatusCode());
        }

        DeepSeekResponse deepSeekResponse = response.getBody();
        if (deepSeekResponse == null || deepSeekResponse.getChoices() == null || deepSeekResponse.getChoices().isEmpty()) {
            throw new RuntimeException("DeepSeek API返回空响应");
        }

        String content = deepSeekResponse.getChoices().get(0).getMessage().getContent();
        logger.info("DeepSeek API响应成功，tokens使用: {}",
                   deepSeekResponse.getUsage() != null ? deepSeekResponse.getUsage().getTotalTokens() : "unknown");

        return content;
    }

    private String buildWeeklyReportPrompt(WeeklyReport report) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请在100到150字内总结以下周报的核心亮点与风险提示。\n");
        prompt.append("标题: ").append(safeValue(report.getTitle())).append('\n');
        prompt.append("报告周期: ").append(safeValue(report.getReportWeek())).append('\n');
        prompt.append("工作概览: ").append(truncateForPrompt(report.getContent(), 280)).append('\n');
        prompt.append("备注: ").append(truncateForPrompt(report.getAdditionalNotes(), 200)).append('\n');
        prompt.append("发展机会: ").append(truncateForPrompt(report.getDevelopmentOpportunities(), 200));
        return prompt.toString();
    }

    private String buildProjectPrompt(com.weeklyreport.project.entity.Project project) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请在100到150字内给出项目进度与风险建议。\n");
        prompt.append("项目名称: ").append(safeValue(project.getName())).append('\n');
        prompt.append("项目描述: ").append(truncateForPrompt(project.getDescription(), 280)).append('\n');
        prompt.append("预期结果: ").append(truncateForPrompt(project.getExpectedResults(), 200)).append('\n');
        prompt.append("时间线: ").append(truncateForPrompt(project.getTimeline(), 120)).append('\n');
        prompt.append("项目成员: ").append(truncateForPrompt(project.getMembers(), 180));
        return prompt.toString();
    }

    /**
     * 构建完整的metadata JSON，包含prompt和AI分析结果
     */
    private String buildMetadata(String systemPrompt, String userPrompt, String rawResponse, Map<String, Object> parsedResult) {
        Map<String, Object> metadata = new HashMap<>();

        // 存储prompt信息
        metadata.put("systemPrompt", truncateForMetadata(systemPrompt, 500));
        metadata.put("userPrompt", truncateForMetadata(userPrompt, 500));

        // 存储AI的完整JSON分析结果
        metadata.put("aiAnalysis", parsedResult);

        // 存储原始响应(截断)
        metadata.put("rawResponse", truncateForMetadata(rawResponse, 1000));

        // 存储时间戳
        metadata.put("analysisTimestamp", LocalDateTime.now().toString());
        metadata.put("modelUsed", deepseekModel);

        try {
            return OBJECT_MAPPER.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize metadata: {}", e.getMessage());
            return "{\"error\":\"Failed to serialize metadata\"}";
        }
    }

    /**
     * 解析并验证DeepSeek返回的JSON响应
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAndValidateJsonResponse(String response) {
        try {
            // 尝试提取JSON部分（去除可能的markdown代码块标记）
            String jsonContent = extractJsonFromResponse(response);

            // 解析JSON
            Map<String, Object> result = OBJECT_MAPPER.readValue(jsonContent, Map.class);
            logger.debug("成功解析AI返回的JSON: {}", result.keySet());
            return result;

        } catch (Exception e) {
            logger.warn("无法解析AI返回的JSON，使用原始文本: {}", e.getMessage());
            // 如果解析失败，返回包含原始响应的Map
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("summary", response);
            fallback.put("parse_error", true);
            return fallback;
        }
    }

    /**
     * 从响应中提取JSON内容（处理markdown代码块）
     */
    private String extractJsonFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "{}";
        }

        String trimmed = response.trim();

        // 移除markdown代码块标记
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }

        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }

        return trimmed.trim();
    }

    /**
     * 计算周报分析的置信度
     */
    private Double calculateConfidence(Map<String, Object> parsedResult) {
        if (parsedResult == null || parsedResult.isEmpty()) {
            return 0.4; // 没有解析结果，置信度偏低
        }

        Object parseError = parsedResult.get("parse_error");
        if (parseError instanceof Boolean && (Boolean) parseError) {
            return 0.4;
        }
        if (parseError != null && "true".equalsIgnoreCase(parseError.toString())) {
            return 0.4;
        }

        // 基于completeness_score计算
        Double completenessScore = getDoubleValue(parsedResult.get("completeness_score"));
        if (completenessScore != null) {
            double normalized = completenessScore / 10.0 * 0.9 + 0.05;
            return Math.max(0.1, Math.min(0.9, normalized));
        }

        double confidence = 0.35; // 无评分时从低基线开始累加

        String summary = parsedResult.get("summary") instanceof String
            ? ((String) parsedResult.get("summary")).trim()
            : "";
        if (summary.length() >= 60) {
            confidence += 0.1;
        }
        if (summary.length() >= 120) {
            confidence += 0.05;
        }

        int highlightCount = countMeaningfulEntries(parsedResult.get("highlights"));
        if (highlightCount > 0) {
            confidence += 0.1;
            if (highlightCount > 2) {
                confidence += 0.05;
            }
        }

        int concernCount = countMeaningfulEntries(parsedResult.get("concerns"));
        if (concernCount > 0) {
            confidence += 0.05;
        }

        int suggestionCount = countMeaningfulEntries(parsedResult.get("suggestions"));
        if (suggestionCount > 0) {
            confidence += 0.1;
            if (suggestionCount > 1) {
                confidence += 0.05;
            }
        }

        return Math.max(0.2, Math.min(0.85, confidence));
    }

    /**
     * 计算项目分析的置信度
     */
    private Double calculateProjectConfidence(Double feasibilityScore, String riskLevel) {
        if (feasibilityScore == null) {
            return 0.7; // 默认置信度
        }

        // 基础置信度基于可行性评分
        double baseConfidence = feasibilityScore / 10.0;

        // 根据风险等级调整
        double riskAdjustment = switch (riskLevel.toUpperCase()) {
            case "LOW" -> 0.1;
            case "HIGH" -> -0.1;
            default -> 0.0; // MEDIUM
        };

        return Math.max(0.1, Math.min(0.95, baseConfidence + riskAdjustment));
    }

    /**
     * 安全地将Object转换为Double
     */
    private Double getDoubleValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private void updateWeeklyReportStatus(WeeklyReport sourceReport, AIAnalysisResult analysisResult) {
        if (analysisResult == null || analysisResult.getEntityType() != AIAnalysisResult.EntityType.WEEKLY_REPORT) {
            return;
        }
        if (sourceReport == null || sourceReport.getId() == null) {
            return;
        }
        if (weeklyReportRepository == null) {
            logger.warn("WeeklyReportRepository 未注入，无法同步周报状态");
            return;
        }

        try {
            weeklyReportRepository.findById(sourceReport.getId()).ifPresent(report -> {
                report.setAiAnalysisId(analysisResult.getId());

                if (analysisResult.getStatus() == AIAnalysisResult.AnalysisStatus.COMPLETED) {
                    double confidence = analysisResult.getConfidence() != null ? analysisResult.getConfidence() : 0.0;

                    // 统一格式的调试日志
                    String logPattern = "🔍[状态检查] 周报ID={}, 状态={}, 置信度={}, 阈值={}, 决策={}, 触发点={}";
                    String decision = confidence >= weeklyReportConfidenceThreshold ? "APPROVE" : "REJECT";
                    logger.info(logPattern, report.getId(),
                        report.getStatus(),
                        confidence, weeklyReportConfidenceThreshold, decision,
                        "AIAnalysisService.updateWeeklyReportStatus");

                    if (confidence >= weeklyReportConfidenceThreshold) {
                        // AI分析通过：进入待审核状态
                        report.aiApprove();
                        logger.info("✅ 周报ID {} AI分析通过，置信度={}，状态: {}",
                            report.getId(), confidence, report.getStatus());
                    } else {
                        // AI分析置信度不足：拒绝
                        String summary = analysisResult.getResult() != null ? analysisResult.getResult() : "AI分析建议请参考详情";
                        String rejectionReason = String.format(
                            "AI分析置信度过低: %.0f%% (阈值: %.0f%%)。建议: %s",
                            confidence * 100,
                            weeklyReportConfidenceThreshold * 100,
                            summary
                        );
                        report.aiReject(rejectionReason);
                        logger.info("⚠️ 周报ID {} AI分析置信度不足，已拒绝，置信度={}，状态: {}",
                            report.getId(), confidence, report.getStatus());
                    }
                } else {
                    // AI分析失败：拒绝
                    String reason = analysisResult.getErrorMessage() != null ? analysisResult.getErrorMessage() : "AI分析失败";
                    report.aiReject("AI分析失败: " + reason);
                    logger.warn("❌ 周报ID {} AI分析失败，原因: {}，状态: {}",
                        report.getId(), reason, report.getStatus());
                }

                weeklyReportRepository.save(report);
            });
        } catch (Exception e) {
            logger.error("同步周报AI分析状态失败，周报ID: {}, 错误: {}", sourceReport.getId(), e.getMessage(), e);
        }
    }

    private int countMeaningfulEntries(Object value) {
        if (value instanceof java.util.Collection<?> collection) {
            int count = 0;
            for (Object item : collection) {
                if (item != null && !item.toString().trim().isEmpty()) {
                    count++;
                }
            }
            return count;
        }
        if (value instanceof Map<?, ?> map) {
            int count = 0;
            for (Object item : map.values()) {
                if (item != null && !item.toString().trim().isEmpty()) {
                    count++;
                }
            }
            return count;
        }
        if (value instanceof String text) {
            return text.trim().isEmpty() ? 0 : 1;
        }
        return 0;
    }

    /**
     * 截断元数据字符串
     */
    private String truncateForMetadata(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private String truncateForPrompt(String value, int maxLength) {
        if (value == null) {
            return "无";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "无";
        }
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, Math.max(0, maxLength)).trim() + "…";
    }

    private String safeValue(String value) {
        return (value == null || value.trim().isEmpty()) ? "无" : value.trim();
    }


    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /**
     * 异步分析周报 - 真正的异步实现
     */
    @Async("aiAnalysisExecutor")
    public CompletableFuture<AIAnalysisResult> analyzeWeeklyReportAsync(WeeklyReport report) {
        logger.info("🚀 启动异步AI分析，周报ID: {}, 线程: {}", report.getId(), Thread.currentThread().getName());

        return CompletableFuture
            .supplyAsync(() -> {
                try {
                    // 模拟AI分析处理时间
                    logger.info("🤖 正在执行AI分析，周报ID: {}, 线程: {}", report.getId(), Thread.currentThread().getName());
                    return analyzeWeeklyReportSync(report);
                } catch (Exception e) {
                    logger.error("🤖 ❌ AI分析执行失败，周报ID: {}, 错误: {}", report.getId(), e.getMessage());
                    throw new RuntimeException("AI分析失败: " + e.getMessage(), e);
                }
            }, aiAnalysisExecutor)  // 使用配置的AI分析线程池
            .orTimeout(30, TimeUnit.SECONDS) // 30秒超时
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    logger.error("🤖 ❌ 异步AI分析失败，周报ID: {}, 错误类型: {}, 错误: {}",
                               report.getId(), throwable.getClass().getSimpleName(), throwable.getMessage());

                    // 超时或失败时，自动恢复周报状态为REJECTED
                    try {
                        WeeklyReport failedReport = weeklyReportRepository.findById(report.getId()).orElse(null);
                        if (failedReport != null && failedReport.getStatus() == WeeklyReport.ReportStatus.AI_PROCESSING) {
                            String errorMsg;
                            if (throwable instanceof java.util.concurrent.TimeoutException) {
                                errorMsg = "AI分析超时(30秒)，可能是网络问题或API响应慢，请稍后重试";
                                logger.warn("⏰ 周报ID {} AI分析超时，自动设置为拒绝状态", report.getId());
                            } else {
                                errorMsg = "AI分析失败: " + throwable.getMessage();
                                logger.warn("❌ 周报ID {} AI分析失败，自动设置为拒绝状态", report.getId());
                            }

                            failedReport.aiReject(errorMsg);
                            weeklyReportRepository.save(failedReport);
                            logger.info("🔄 周报ID {} 状态已从AI_PROCESSING恢复为REJECTED", report.getId());

                            // 发送AI分析完成通知（会触发AI拒绝邮件）
                            try {
                                notificationService.handleAIAnalysisCompleted(report.getId());
                                logger.info("📧 AI分析失败通知已触发，周报ID: {}", report.getId());
                            } catch (Exception e) {
                                logger.error("📧 ❌ 触发AI分析失败通知时出错，周报ID: {}", report.getId(), e);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("🤖 ❌ 恢复周报状态时出错，周报ID: {}", report.getId(), e);
                    }
                } else {
                    logger.info("🤖 ✅ 异步AI分析完成，周报ID: {}, 结果ID: {}",
                               report.getId(), result.getId());

                    // 触发AI分析完成通知
                    try {
                        notificationService.handleAIAnalysisCompleted(report.getId());
                        logger.info("📧 AI分析完成通知已触发，周报ID: {}", report.getId());
                    } catch (Exception e) {
                        logger.error("📧 ❌ 触发AI分析完成通知失败，周报ID: {}", report.getId(), e);
                    }
                }
            });
    }

    /**
     * 获取分析结果
     */
    public AIAnalysisResult getAnalysisResults(Long entityId) {
        logger.info("获取实体{}的分析结果", entityId);

        try {
            // 查找最近的分析结果
            return aiAnalysisResultRepository.findTopByReportIdOrderByCreatedAtDesc(entityId)
                .orElse(null);
        } catch (Exception e) {
            logger.error("获取分析结果失败", e);
            return null;
        }
    }
}
