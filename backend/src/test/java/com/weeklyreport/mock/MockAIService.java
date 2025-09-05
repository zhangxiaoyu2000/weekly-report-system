package com.weeklyreport.mock;

import com.weeklyreport.dto.ai.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock AI Service for testing environment
 * Provides predictable responses for AI functionality testing
 */
@TestConfiguration
@Profile("test")
public class MockAIService {

    private static final Logger logger = LoggerFactory.getLogger(MockAIService.class);
    
    // In-memory storage for mock analysis results
    private final Map<Long, AIAnalysisResponse> analysisResults = new ConcurrentHashMap<>();
    private final Map<String, AISuggestionResponse> suggestionResults = new ConcurrentHashMap<>();
    private final Map<Long, AIProjectInsightResponse> insightResults = new ConcurrentHashMap<>();

    /**
     * Mock AI Analysis Service
     */
    // @Bean
    // @Primary
    // public AIAnalysisService mockAIAnalysisService() {
    //     return new AIAnalysisService() {
    //         @Override
    //         public CompletableFuture<AIAnalysisResponse> analyzeReportAsync(AIAnalysisRequest request, Long userId) {
    //             logger.info("Mock AI analysis started for report {} by user {}", request.getReportId(), userId);
    //             
    //             // Simulate processing delay
    //             return CompletableFuture.supplyAsync(() -> {
    //                 try {
    //                     Thread.sleep(100); // Simulate brief processing time
    //                 } catch (InterruptedException e) {
    //                     Thread.currentThread().interrupt();
    //                 }
    //                 
    //                 AIAnalysisResponse response = createMockAnalysisResponse(request);
    //                 analysisResults.put(request.getReportId(), response);
    //                 return response;
    //             });
    //         }
    //         
    //         @Override
    //         public AIAnalysisResponse getAnalysisResult(Long reportId) {
    //             logger.info("Mock retrieving analysis result for report {}", reportId);
    //             return analysisResults.getOrDefault(reportId, createMockAnalysisResponse(reportId));
    //         }
    //     };
    // }

    /**
     * Mock AI Suggestion Service
     */
    // @Bean
    // @Primary
    // public AISuggestionService mockAISuggestionService() {
    //     return new AISuggestionService() {
    //         @Override
    //         public AISuggestionResponse generateSuggestions(AISuggestionRequest request) {
    //             logger.info("Mock generating suggestions for user {} with context {}", 
    //                         request.getUserId(), request.getContext());
    //             
    //             AISuggestionResponse response = createMockSuggestionResponse(request);
    //             String key = request.getUserId() + "_" + request.getContext();
    //             suggestionResults.put(key, response);
    //             return response;
    //         }
    //     };
    // }

    /**
     * Mock AI Project Insight Service
     */
    // @Bean
    // @Primary
    // public AIProjectInsightService mockAIProjectInsightService() {
    //     return new AIProjectInsightService() {
    //         @Override
    //         public AIProjectInsightResponse generateProjectInsights(AIProjectInsightRequest request) {
    //             logger.info("Mock generating project insights for project {}", request.getProjectId());
    //             
    //             AIProjectInsightResponse response = createMockProjectInsightResponse(request);
    //             insightResults.put(request.getProjectId(), response);
    //             return response;
    //         }
    //     };
    // }

    // Mock response creation methods

    public AIAnalysisResponse createMockAnalysisResponse(AIAnalysisRequest request) {
        return createMockAnalysisResponse(request.getReportId());
    }

    public AIAnalysisResponse createMockAnalysisResponse(Long reportId) {
        LocalDateTime now = LocalDateTime.now();
        
        return AIAnalysisResponse.builder()
                .analysisId(System.currentTimeMillis())
                .reportId(reportId)
                .status("COMPLETED")
                .summary("Mock分析摘要：本周工作进展良好，团队协作效率高，项目按计划推进。")
                .sentiment("POSITIVE", 0.75)
                .keywords(Arrays.asList("项目进展", "团队协作", "效率提升", "代码质量", "测试覆盖"))
                .risks(Arrays.asList("时间压力可能导致质量下降", "新技术学习曲线较陡"))
                .suggestions(Arrays.asList(
                    "建议增加代码审查频次以确保质量", 
                    "可以考虑引入自动化测试工具提高效率",
                    "建议定期举行技术分享会促进知识交流"
                ))
                .insights(createMockInsightsMap())
                .confidenceScore(85)
                .timing(now.minusMinutes(5), now, 300000L)
                .build();
    }

    public AISuggestionResponse createMockSuggestionResponse(AISuggestionRequest request) {
        AISuggestionResponse response = new AISuggestionResponse();
        response.setSuggestionId("mock-" + System.currentTimeMillis());
        response.setContext(request.getContext());
        response.setConfidence("HIGH");

        List<AISuggestionResponse.Suggestion> mockSuggestions = new ArrayList<>();
        
        // Context-specific suggestions
        switch (request.getContext()) {
            case "report_improvement":
                mockSuggestions.addAll(createReportImprovementSuggestions());
                break;
            case "project_planning":
                mockSuggestions.addAll(createProjectPlanningSuggestions());
                break;
            case "team_management":
                mockSuggestions.addAll(createTeamManagementSuggestions());
                break;
            default:
                mockSuggestions.addAll(createGeneralSuggestions());
        }

        // Limit to requested number
        int maxSuggestions = request.getMaxSuggestions() != null ? request.getMaxSuggestions() : 5;
        if (mockSuggestions.size() > maxSuggestions) {
            mockSuggestions = mockSuggestions.subList(0, maxSuggestions);
        }

        response.setSuggestions(mockSuggestions);
        return response;
    }

    public AIProjectInsightResponse createMockProjectInsightResponse(AIProjectInsightRequest request) {
        AIProjectInsightResponse response = new AIProjectInsightResponse(
            request.getProjectId(), 
            "Mock项目-" + request.getProjectId()
        );

        // Mock progress insight
        AIProjectInsightResponse.ProjectProgressInsight progressInsight = 
            new AIProjectInsightResponse.ProjectProgressInsight();
        progressInsight.setCompletionPercentage(68.5);
        progressInsight.setProgressStatus("on_track");
        progressInsight.setTasksCompleted(15);
        progressInsight.setTotalTasks(22);
        progressInsight.setProgressSummary("项目整体进展良好，关键里程碑按时完成");
        progressInsight.setKeyAchievements(Arrays.asList(
            "用户认证模块开发完成",
            "数据库设计和迁移完成", 
            "基础API接口实现完成"
        ));
        progressInsight.setBlockers(Arrays.asList(
            "第三方API集成等待审批", 
            "UI设计评审需要更多时间"
        ));
        response.setProgressInsight(progressInsight);

        // Mock team performance insight
        AIProjectInsightResponse.TeamPerformanceInsight teamInsight = 
            new AIProjectInsightResponse.TeamPerformanceInsight();
        teamInsight.setAverageProductivity(78.5);
        teamInsight.setTeamMorale("high");
        teamInsight.setActiveMembers(5);
        teamInsight.setMemberContributions(Map.of(
            "张三", 85.0,
            "李四", 92.0,
            "王五", 76.0,
            "赵六", 88.0,
            "钱七", 79.0
        ));
        teamInsight.setCollaborationPatterns(Arrays.asList(
            "代码审查参与度高",
            "知识分享活跃",
            "问题解决响应及时"
        ));
        teamInsight.setImprovementAreas(Arrays.asList(
            "可以增加跨团队技术交流",
            "建议优化会议效率"
        ));
        response.setTeamInsight(teamInsight);

        // Mock risks
        List<AIProjectInsightResponse.RiskInsight> risks = Arrays.asList(
            createMockRiskInsight("timeline", "medium", "项目时间线存在一定压力，需要关注关键路径上的任务进展"),
            createMockRiskInsight("technical", "low", "新技术栈学习成本相对较低，团队适应良好"),
            createMockRiskInsight("resource", "medium", "后期可能需要额外的前端开发资源")
        );
        response.setRisks(risks);

        // Mock trends
        List<AIProjectInsightResponse.TrendInsight> trends = Arrays.asList(
            createMockTrendInsight("productivity", "increasing", "团队生产效率呈上升趋势"),
            createMockTrendInsight("code_quality", "stable", "代码质量保持稳定的高水准"),
            createMockTrendInsight("bug_rate", "decreasing", "缺陷率逐步下降，测试覆盖率提升")
        );
        response.setTrends(trends);

        return response;
    }

    // Helper methods for creating mock data

    private Map<String, Object> createMockInsightsMap() {
        Map<String, Object> insights = new HashMap<>();
        insights.put("workloadDistribution", Map.of(
            "development", 60,
            "testing", 25,
            "documentation", 10,
            "meetings", 5
        ));
        insights.put("communicationScore", 88);
        insights.put("innovationIndex", 75);
        insights.put("technicalDebt", "低");
        return insights;
    }

    private List<AISuggestionResponse.Suggestion> createReportImprovementSuggestions() {
        List<AISuggestionResponse.Suggestion> suggestions = new ArrayList<>();
        
        AISuggestionResponse.Suggestion s1 = new AISuggestionResponse.Suggestion();
        s1.setTitle("增加具体数据支撑");
        s1.setDescription("在周报中加入具体的性能指标、完成度百分比等量化数据，使报告更具说服力");
        s1.setCategory("improvement");
        s1.setPriority("HIGH");
        s1.setConfidenceScore(92);
        s1.setTags(Arrays.asList("数据化", "量化指标", "可视化"));
        s1.setActionType("immediate");
        suggestions.add(s1);

        AISuggestionResponse.Suggestion s2 = new AISuggestionResponse.Suggestion();
        s2.setTitle("优化问题描述格式");
        s2.setDescription("采用结构化的问题描述模板，包括问题背景、影响范围、解决方案和时间计划");
        s2.setCategory("optimization");
        s2.setPriority("MEDIUM");
        s2.setConfidenceScore(85);
        s2.setTags(Arrays.asList("格式化", "结构化", "模板"));
        s2.setActionType("short_term");
        suggestions.add(s2);

        return suggestions;
    }

    private List<AISuggestionResponse.Suggestion> createProjectPlanningSuggestions() {
        List<AISuggestionResponse.Suggestion> suggestions = new ArrayList<>();
        
        AISuggestionResponse.Suggestion s1 = new AISuggestionResponse.Suggestion();
        s1.setTitle("采用敏捷开发方法论");
        s1.setDescription("建议引入Scrum框架，通过短迭代周期提高项目适应性和交付质量");
        s1.setCategory("methodology");
        s1.setPriority("HIGH");
        s1.setConfidenceScore(90);
        s1.setTags(Arrays.asList("敏捷", "Scrum", "迭代"));
        s1.setActionType("long_term");
        suggestions.add(s1);

        return suggestions;
    }

    private List<AISuggestionResponse.Suggestion> createTeamManagementSuggestions() {
        List<AISuggestionResponse.Suggestion> suggestions = new ArrayList<>();
        
        AISuggestionResponse.Suggestion s1 = new AISuggestionResponse.Suggestion();
        s1.setTitle("建立定期一对一沟通机制");
        s1.setDescription("与团队成员建立定期的一对一沟通，了解个人发展需求和工作挑战");
        s1.setCategory("communication");
        s1.setPriority("MEDIUM");
        s1.setConfidenceScore(88);
        s1.setTags(Arrays.asList("沟通", "一对一", "团队建设"));
        s1.setActionType("immediate");
        suggestions.add(s1);

        return suggestions;
    }

    private List<AISuggestionResponse.Suggestion> createGeneralSuggestions() {
        List<AISuggestionResponse.Suggestion> suggestions = new ArrayList<>();
        
        AISuggestionResponse.Suggestion s1 = new AISuggestionResponse.Suggestion();
        s1.setTitle("建立知识共享机制");
        s1.setDescription("定期组织技术分享会，促进团队内部知识传递和最佳实践分享");
        s1.setCategory("knowledge_sharing");
        s1.setPriority("MEDIUM");
        s1.setConfidenceScore(80);
        s1.setTags(Arrays.asList("知识分享", "团队学习", "技术交流"));
        s1.setActionType("short_term");
        suggestions.add(s1);

        return suggestions;
    }

    private AIProjectInsightResponse.RiskInsight createMockRiskInsight(String riskType, String severity, String description) {
        AIProjectInsightResponse.RiskInsight risk = new AIProjectInsightResponse.RiskInsight();
        risk.setRiskType(riskType);
        risk.setSeverity(severity);
        risk.setDescription(description);
        risk.setProbability(0.3 + Math.random() * 0.4); // Random probability between 0.3-0.7
        risk.setMitigation(Arrays.asList("制定应对预案", "增加监控频率", "提前准备备选方案"));
        risk.setImpact("中等影响");
        return risk;
    }

    private AIProjectInsightResponse.TrendInsight createMockTrendInsight(String trendType, String direction, String description) {
        AIProjectInsightResponse.TrendInsight trend = new AIProjectInsightResponse.TrendInsight();
        trend.setTrendType(trendType);
        trend.setDirection(direction);
        trend.setDescription(description);
        trend.setSignificance("medium");
        trend.setData(Map.of(
            "currentValue", Math.random() * 100,
            "previousValue", Math.random() * 100,
            "changeRate", (Math.random() - 0.5) * 20
        ));
        return trend;
    }

    // Utility methods for test data manipulation

    public void clearMockData() {
        analysisResults.clear();
        suggestionResults.clear();
        insightResults.clear();
        logger.info("Mock AI service data cleared");
    }

    public void setMockAnalysisResult(Long reportId, AIAnalysisResponse response) {
        analysisResults.put(reportId, response);
        logger.info("Mock analysis result set for report {}", reportId);
    }

    public Map<Long, AIAnalysisResponse> getAllAnalysisResults() {
        return new HashMap<>(analysisResults);
    }

    public Map<String, AISuggestionResponse> getAllSuggestionResults() {
        return new HashMap<>(suggestionResults);
    }

    public Map<Long, AIProjectInsightResponse> getAllInsightResults() {
        return new HashMap<>(insightResults);
    }
}