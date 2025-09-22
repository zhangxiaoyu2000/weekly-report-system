package com.weeklyreport.service.ai.deepseek;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weeklyreport.service.ai.AbstractAIServiceProviderWithMetrics;
import com.weeklyreport.service.ai.AIServiceType;
import com.weeklyreport.service.ai.dto.AIAnalysisRequest;
import com.weeklyreport.service.ai.dto.AIAnalysisResponse;
import com.weeklyreport.service.ai.dto.EnhancedAIAnalysisRequest;
import com.weeklyreport.service.ai.dto.StandardizedAIResponse;
import com.weeklyreport.service.ai.deepseek.dto.DeepSeekRequest;
import com.weeklyreport.service.ai.deepseek.dto.DeepSeekResponse;
import com.weeklyreport.service.ai.exception.AIServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * DeepSeek AI service implementation
 */
@Component("deepseekProvider")
public class DeepSeekAIService extends AbstractAIServiceProviderWithMetrics {
    
    private static final Logger logger = LoggerFactory.getLogger(DeepSeekAIService.class);
    
    @Value("${ai.deepseek.api-key:sk-4613204f1ddc4fcf88894d77be5da3e8}")
    private String apiKey;
    
    @Value("${ai.deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;
    
    @Value("${ai.deepseek.model:deepseek-chat}")
    private String model;
    
    @Value("${ai.deepseek.max-tokens:2000}")
    private Integer maxTokens;
    
    @Value("${ai.deepseek.temperature:0.7}")
    private Double temperature;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public DeepSeekAIService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    protected AIAnalysisResponse performAnalysis(AIAnalysisRequest request) throws AIServiceException {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Starting DeepSeek analysis for type: {}", request.getAnalysisType());
            
            // Build request
            DeepSeekRequest deepSeekRequest = buildDeepSeekRequest(request);
            
            // Call DeepSeek API
            DeepSeekResponse deepSeekResponse = callDeepSeekAPI(deepSeekRequest);
            
            // Convert to standard response
            AIAnalysisResponse response = convertToStandardResponse(deepSeekResponse, request);
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            response.setProviderUsed("deepseek");
            
            logger.info("DeepSeek analysis completed successfully in {}ms", 
                       response.getProcessingTimeMs());
            
            return response;
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            logger.error("DeepSeek analysis failed after {}ms: {}", processingTime, e.getMessage(), e);
            throw new AIServiceException("DeepSeek analysis failed: " + e.getMessage(), e);
        }
    }
    
    private DeepSeekRequest buildDeepSeekRequest(AIAnalysisRequest request) throws AIServiceException {
        String prompt = buildAnalysisPrompt(request);
        
        return DeepSeekRequest.builder()
            .model(model)
            .messages(List.of(new DeepSeekRequest.Message("user", prompt)))
            .maxTokens(maxTokens)
            .temperature(temperature)
            .stream(false)
            .build();
    }
    
    private String buildAnalysisPrompt(AIAnalysisRequest request) throws AIServiceException {
        if (request instanceof EnhancedAIAnalysisRequest) {
            EnhancedAIAnalysisRequest enhanced = (EnhancedAIAnalysisRequest) request;
            return switch (enhanced.getAnalysisContext()) {
                case PROJECT_FEASIBILITY -> buildProjectFeasibilityPrompt(enhanced);
                case WEEKLY_REPORT_QUALITY -> buildWeeklyReportQualityPrompt(enhanced);
                default -> request.getContent();
            };
        }
        return request.getContent();
    }
    
    private String buildProjectFeasibilityPrompt(EnhancedAIAnalysisRequest request) {
        if (request.getProjectData() == null) {
            throw new IllegalArgumentException("Project data is required for project feasibility analysis");
        }
        
        EnhancedAIAnalysisRequest.ProjectData project = request.getProjectData();
        
        // 构建阶段性任务信息
        StringBuilder phaseInfo = new StringBuilder();
        if (project.getProjectPhases() != null && !project.getProjectPhases().isEmpty()) {
            phaseInfo.append("\n\n项目阶段性任务：\n");
            for (EnhancedAIAnalysisRequest.ProjectPhase phase : project.getProjectPhases()) {
                phaseInfo.append(String.format(
                    "阶段%d: %s\n  - 描述：%s\n  - 负责成员：%s\n  - 时间安排：%s\n  - 关键指标：%s\n  - 预期结果：%s\n  - 当前状态：%s\n\n",
                    phase.getPhaseOrder() != null ? phase.getPhaseOrder() : 0,
                    phase.getPhaseName() != null ? phase.getPhaseName() : "未指定",
                    phase.getPhaseDescription() != null ? phase.getPhaseDescription() : "无",
                    phase.getAssignedMembers() != null ? phase.getAssignedMembers() : "未指定",
                    phase.getTimeline() != null ? phase.getTimeline() : "未指定",
                    phase.getKeyIndicators() != null ? phase.getKeyIndicators() : "无",
                    phase.getEstimatedResults() != null ? phase.getEstimatedResults() : "无",
                    phase.getStatus() != null ? phase.getStatus() : "PENDING"
                ));
            }
        } else {
            phaseInfo.append("\n\n项目阶段性任务：暂无具体阶段规划\n");
        }

        return String.format("""
            你是一位资深的项目管理专家，请分析以下项目的可行性：
            
            项目基本信息：
            - 项目名称：%s
            - 项目内容：%s
            - 项目成员：%s
            - 关键指标：%s
            - 预期结果：%s
            - 时间计划：%s
            - 止损点：%s
            - 主管ID：%s
            %s
            
            请从以下维度进行分析：
            1. 项目目标的明确性和可实现性
            2. 资源配置的合理性（包括阶段性任务的人员分配）
            3. 时间规划的现实性（包括各阶段的时间安排）
            4. 风险控制的充分性
            5. 关键指标的可衡量性
            6. 阶段性任务的科学性和连贯性
            
            请以JSON格式返回分析结果：
            {
                "isPass": true/false,
                "proposal": "详细的分析意见",
                "feasibilityScore": 0.0-1.0,
                "riskLevel": "LOW/MEDIUM/HIGH",
                "keyIssues": ["问题1", "问题2"],
                "recommendations": ["建议1", "建议2"]
            }
            """, 
            project.getProjectName(),
            project.getProjectContent(),
            project.getProjectMembers(),
            project.getKeyIndicators(),
            project.getExpectedResults(),
            project.getTimeline(),
            project.getStopLoss(),
            request.getManagerId(),
            phaseInfo.toString()
        );
    }
    
    private String buildWeeklyReportQualityPrompt(EnhancedAIAnalysisRequest request) {
        if (request.getWeeklyReportData() == null) {
            throw new IllegalArgumentException("Weekly report data is required for quality analysis");
        }
        
        EnhancedAIAnalysisRequest.WeeklyReportData report = request.getWeeklyReportData();
        
        return String.format("""
            你是一位经验丰富的工作汇报审核专家，请评估以下周报的质量：
            
            周报信息：
            - 标题：%s
            - 主要内容：%s
            - 可发展性清单：%s
            - 其他备注：%s
            - 提交者主管ID：%s
            
            注意：周报内容现在采用基于任务的结构化格式，包含本周完成工作和下周计划。
            
            请从以下维度评估：
            1. 内容的完整性和详细程度
            2. 工作成果的具体性和可衡量性
            3. 问题识别的准确性和深度
            4. 下周计划的可执行性
            5. 整体表达的专业性
            
            请以JSON格式返回评估结果：
            {
                "isPass": true/false,
                "proposal": "详细的评估意见和改进建议",
                "qualityScore": 0.0-1.0,
                "riskLevel": "LOW/MEDIUM/HIGH",
                "keyIssues": ["问题1", "问题2"],
                "recommendations": ["建议1", "建议2"]
            }
            """,
            report.getTitle(),
            report.getContent(),
            report.getDevelopmentOpportunities() != null ? report.getDevelopmentOpportunities() : "无",
            report.getAdditionalNotes() != null ? report.getAdditionalNotes() : "无",
            request.getManagerId()
        );
    }
    
    private DeepSeekResponse callDeepSeekAPI(DeepSeekRequest request) throws AIServiceException {
        try {
            String url = baseUrl + "/chat/completions";
            logger.debug("Calling DeepSeek API with URL: {}", url);
            logger.debug("Request payload: {}", objectMapper.writeValueAsString(request));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("User-Agent", "WeeklyReport-System/1.0");
            
            HttpEntity<DeepSeekRequest> entity = new HttpEntity<>(request, headers);
            
            // 使用String响应先获取原始响应，便于调试
            ResponseEntity<String> response = restTemplate.postForEntity(
                url, entity, String.class);
            
            logger.debug("DeepSeek API response status: {}", response.getStatusCode());
            logger.debug("DeepSeek API response body: {}", response.getBody());
            
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new AIServiceException("DeepSeek API call failed with status: " + 
                    response.getStatusCode() + ", body: " + response.getBody());
            }
            
            if (response.getBody() == null || response.getBody().trim().isEmpty()) {
                throw new AIServiceException("DeepSeek API returned empty response");
            }
            
            // 手动解析响应
            try {
                return objectMapper.readValue(response.getBody(), DeepSeekResponse.class);
            } catch (JsonProcessingException e) {
                logger.error("Failed to parse DeepSeek response: {}", response.getBody());
                throw new AIServiceException("Failed to parse DeepSeek response: " + e.getMessage(), e);
            }
            
        } catch (AIServiceException e) {
            throw e; // 重新抛出已知的AI服务异常
        } catch (Exception e) {
            logger.error("Unexpected error calling DeepSeek API: {}", e.getMessage(), e);
            throw new AIServiceException("Failed to call DeepSeek API: " + e.getMessage(), e);
        }
    }
    
    private AIAnalysisResponse convertToStandardResponse(DeepSeekResponse deepSeekResponse, 
                                                       AIAnalysisRequest request) throws AIServiceException {
        try {
            logger.debug("Converting DeepSeek response to standard response");
            
            if (deepSeekResponse == null) {
                throw new AIServiceException("DeepSeek response is null");
            }
            
            if (deepSeekResponse.getChoices() == null || deepSeekResponse.getChoices().isEmpty()) {
                throw new AIServiceException("No response choices from DeepSeek");
            }
            
            String content = deepSeekResponse.getChoices().get(0).getMessage().getContent();
            logger.debug("DeepSeek AI generated content: {}", content);
            
            // Try to parse as JSON for structured response
            StandardizedAIResponse structuredResponse = parseStructuredResponse(content);
            
            // Create AI analysis response
            AIAnalysisResponse response = new AIAnalysisResponse();
            response.setAnalysisId(UUID.randomUUID().toString());
            response.setAnalysisType(request.getAnalysisType());
            response.setResult(structuredResponse.getProposal());
            response.setConfidence(structuredResponse.getConfidence());
            response.setProviderUsed("deepseek");
            response.setTimestamp(LocalDateTime.now());
            
            // Add comprehensive metadata
            java.util.Map<String, Object> metadata = new java.util.HashMap<>();
            metadata.put("isPass", structuredResponse.getIsPass());
            
            if (structuredResponse.getAnalysisDetails() != null) {
                StandardizedAIResponse.AnalysisDetails details = structuredResponse.getAnalysisDetails();
                metadata.put("riskLevel", details.getRiskLevel() != null ? details.getRiskLevel().name() : null);
                metadata.put("feasibilityScore", details.getFeasibilityScore());
                metadata.put("keyIssues", details.getKeyIssues());
                metadata.put("recommendations", details.getRecommendations());
            }
            
            // Add API usage information from DeepSeek response
            if (deepSeekResponse.getUsage() != null) {
                metadata.put("promptTokens", deepSeekResponse.getUsage().getPromptTokens());
                metadata.put("completionTokens", deepSeekResponse.getUsage().getCompletionTokens());
                metadata.put("totalTokens", deepSeekResponse.getUsage().getTotalTokens());
            }
            
            response.setMetadata(metadata);
            
            logger.info("Successfully converted DeepSeek response - Pass: {}, Confidence: {}", 
                structuredResponse.getIsPass(), structuredResponse.getConfidence());
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error converting DeepSeek response", e);
            logger.error("DeepSeek response details: {}", 
                deepSeekResponse != null ? deepSeekResponse.toString() : "null");
            throw new AIServiceException("Failed to convert DeepSeek response: " + e.getMessage(), e);
        }
    }
    
    private StandardizedAIResponse parseStructuredResponse(String content) {
        try {
            logger.debug("Parsing structured response content: {}", content);
            
            // Try to extract JSON from the response
            String jsonContent = extractJsonFromResponse(content);
            logger.debug("Extracted JSON content: {}", jsonContent);
            
            // Parse as generic JSON first
            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(jsonContent);
            
            // Create standardized response
            StandardizedAIResponse response = new StandardizedAIResponse();
            
            // Extract basic fields
            if (jsonNode.has("isPass")) {
                response.setIsPass(jsonNode.get("isPass").asBoolean());
            }
            
            if (jsonNode.has("proposal")) {
                response.setProposal(jsonNode.get("proposal").asText());
            }
            
            if (jsonNode.has("feasibilityScore")) {
                response.setConfidence(jsonNode.get("feasibilityScore").asDouble());
            }
            
            // Create analysis details
            StandardizedAIResponse.AnalysisDetails details = new StandardizedAIResponse.AnalysisDetails();
            
            if (jsonNode.has("feasibilityScore")) {
                details.setFeasibilityScore(jsonNode.get("feasibilityScore").asDouble());
            }
            
            if (jsonNode.has("riskLevel")) {
                String riskLevel = jsonNode.get("riskLevel").asText();
                try {
                    details.setRiskLevel(StandardizedAIResponse.RiskLevel.valueOf(riskLevel));
                } catch (IllegalArgumentException e) {
                    logger.warn("Unknown risk level: {}", riskLevel);
                    details.setRiskLevel(StandardizedAIResponse.RiskLevel.MEDIUM);
                }
            }
            
            // Extract arrays
            if (jsonNode.has("keyIssues") && jsonNode.get("keyIssues").isArray()) {
                java.util.List<String> keyIssues = new java.util.ArrayList<>();
                jsonNode.get("keyIssues").forEach(node -> keyIssues.add(node.asText()));
                details.setKeyIssues(keyIssues);
            }
            
            if (jsonNode.has("recommendations") && jsonNode.get("recommendations").isArray()) {
                java.util.List<String> recommendations = new java.util.ArrayList<>();
                jsonNode.get("recommendations").forEach(node -> recommendations.add(node.asText()));
                details.setRecommendations(recommendations);
            }
            
            response.setAnalysisDetails(details);
            
            logger.debug("Successfully parsed structured response: isPass={}, confidence={}", 
                response.getIsPass(), response.getConfidence());
            
            return response;
            
        } catch (JsonProcessingException e) {
            logger.warn("Failed to parse structured response, using fallback: {}", e.getMessage());
            logger.debug("Failed content was: {}", content);
            
            // Fallback to basic response
            StandardizedAIResponse response = new StandardizedAIResponse();
            response.setIsPass(true); // Default to pass if we can't parse
            response.setProposal(content);
            response.setConfidence(0.5); // Default confidence
            return response;
        }
    }
    
    private String extractJsonFromResponse(String content) {
        // Find JSON block in the response
        int jsonStart = content.indexOf("{");
        int jsonEnd = content.lastIndexOf("}");
        
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return content.substring(jsonStart, jsonEnd + 1);
        }
        
        // If no JSON found, create a basic structure
        return String.format("""
            {
                "isPass": true,
                "proposal": "%s",
                "qualityScore": 0.8,
                "riskLevel": "LOW",
                "keyIssues": [],
                "recommendations": ["建议进一步完善"]
            }
            """, content.replace("\"", "\\\""));
    }
    
    public boolean isHealthy() {
        try {
            // Simple health check - try a minimal request
            DeepSeekRequest healthCheck = DeepSeekRequest.builder()
                .model(model)
                .messages(List.of(new DeepSeekRequest.Message("user", "hello")))
                .maxTokens(10)
                .temperature(0.1)
                .build();
            
            callDeepSeekAPI(healthCheck);
            return true;
        } catch (Exception e) {
            logger.warn("DeepSeek health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public AIServiceType getServiceType() {
        return AIServiceType.DEEPSEEK;
    }
    
    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.trim().isEmpty() && isHealthy();
    }
    
    @Override
    public String getConfigurationStatus() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "Missing API key";
        }
        if (!isHealthy()) {
            return "Service unavailable";
        }
        return "Configured and healthy";
    }
    
    @Override
    public int getMaxTokens() {
        return maxTokens;
    }
    
    @Override
    public double getCostEstimate(String content) {
        // DeepSeek pricing estimation (approximate)
        int tokens = content.length() / 4; // Rough token estimation
        return tokens * 0.0001; // Very low cost estimate in USD cents
    }
    
    public String getProviderId() {
        return "deepseek";
    }
}