package com.weeklyreport.service;

import com.weeklyreport.entity.AIAnalysisResult;
import com.weeklyreport.entity.WeeklyReport;
import com.weeklyreport.repository.AIAnalysisResultRepository;
import com.weeklyreport.repository.WeeklyReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for AI-powered analysis of weekly reports
 * Provides content analysis, risk assessment, and intelligent suggestions
 */
@Service
@Transactional
public class AIAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisService.class);

    private final AIAnalysisResultRepository analysisRepository;
    private final WeeklyReportRepository weeklyReportRepository;

    // Text preprocessing patterns
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[\\p{Punct}]");
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");
    private static final Pattern ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]+");

    // Analysis keywords for different categories
    private static final Map<String, Set<String>> KEYWORD_CATEGORIES = Map.of(
        "risk", Set.of("问题", "困难", "挑战", "阻碍", "延期", "风险", "bug", "error", "issue", "problem", "challenge", "delay", "risk"),
        "achievement", Set.of("完成", "达成", "成功", "优化", "提升", "改进", "实现", "上线", "发布", "finished", "completed", "achieved", "success", "improved", "optimized"),
        "collaboration", Set.of("协作", "合作", "配合", "沟通", "讨论", "会议", "团队", "collaborate", "cooperation", "teamwork", "meeting", "discussion", "communicate"),
        "technical", Set.of("开发", "编码", "测试", "部署", "数据库", "API", "系统", "框架", "技术", "develop", "code", "test", "deploy", "database", "system", "framework", "technical")
    );

    @Autowired
    public AIAnalysisService(AIAnalysisResultRepository analysisRepository, 
                           WeeklyReportRepository weeklyReportRepository) {
        this.analysisRepository = analysisRepository;
        this.weeklyReportRepository = weeklyReportRepository;
    }

    /**
     * Start comprehensive analysis for a weekly report
     */
    @Async("aiTaskExecutor")
    public CompletableFuture<List<AIAnalysisResult>> analyzeWeeklyReportAsync(Long reportId) {
        logger.info("Starting comprehensive AI analysis for report ID: {}", reportId);
        
        Optional<WeeklyReport> reportOpt = weeklyReportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            logger.error("Weekly report not found: {}", reportId);
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        WeeklyReport report = reportOpt.get();
        List<AIAnalysisResult> results = new ArrayList<>();

        try {
            // Create analysis tasks for different types
            List<AIAnalysisResult.AnalysisType> analysisTypes = Arrays.asList(
                AIAnalysisResult.AnalysisType.SUMMARY,
                AIAnalysisResult.AnalysisType.KEYWORDS,
                AIAnalysisResult.AnalysisType.SENTIMENT,
                AIAnalysisResult.AnalysisType.RISK_ASSESSMENT,
                AIAnalysisResult.AnalysisType.SUGGESTIONS,
                AIAnalysisResult.AnalysisType.PROGRESS_ANALYSIS,
                AIAnalysisResult.AnalysisType.WORKLOAD_ANALYSIS
            );

            for (AIAnalysisResult.AnalysisType type : analysisTypes) {
                // Check if analysis already exists and is not failed
                if (!analysisRepository.existsByWeeklyReportIdAndAnalysisTypeAndStatus(
                        reportId, type, AIAnalysisResult.AnalysisStatus.COMPLETED)) {
                    
                    AIAnalysisResult analysisResult = performAnalysis(report, type);
                    if (analysisResult != null) {
                        results.add(analysisResult);
                    }
                }
            }

            logger.info("Completed comprehensive AI analysis for report ID: {}. Generated {} results.", 
                       reportId, results.size());
            return CompletableFuture.completedFuture(results);

        } catch (Exception e) {
            logger.error("Error during AI analysis for report ID: {}", reportId, e);
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }

    /**
     * Perform specific type of analysis
     */
    public AIAnalysisResult performAnalysis(WeeklyReport report, AIAnalysisResult.AnalysisType analysisType) {
        logger.debug("Performing {} analysis for report ID: {}", analysisType, report.getId());
        
        long startTime = System.currentTimeMillis();
        AIAnalysisResult analysisResult = new AIAnalysisResult(report, analysisType);
        analysisResult.startProcessing();
        analysisResult = analysisRepository.save(analysisResult);

        try {
            String result;
            double confidence;

            switch (analysisType) {
                case SUMMARY:
                    result = generateSummary(report);
                    confidence = calculateSummaryConfidence(report, result);
                    break;
                case KEYWORDS:
                    result = extractKeywords(report);
                    confidence = calculateKeywordConfidence(report, result);
                    break;
                case SENTIMENT:
                    result = analyzeSentiment(report);
                    confidence = calculateSentimentConfidence(report, result);
                    break;
                case RISK_ASSESSMENT:
                    result = assessRisks(report);
                    confidence = calculateRiskConfidence(report, result);
                    break;
                case SUGGESTIONS:
                    result = generateSuggestions(report);
                    confidence = calculateSuggestionConfidence(report, result);
                    break;
                case PROGRESS_ANALYSIS:
                    result = analyzeProgress(report);
                    confidence = calculateProgressConfidence(report, result);
                    break;
                case WORKLOAD_ANALYSIS:
                    result = analyzeWorkload(report);
                    confidence = calculateWorkloadConfidence(report, result);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported analysis type: " + analysisType);
            }

            long processingTime = System.currentTimeMillis() - startTime;
            analysisResult.markAsCompleted(result, confidence);
            analysisResult.setProcessingTimeMs(processingTime);
            analysisResult.setModelVersion("internal-v1.0");

            return analysisRepository.save(analysisResult);

        } catch (Exception e) {
            logger.error("Error performing {} analysis for report ID: {}", analysisType, report.getId(), e);
            analysisResult.markAsFailed("Analysis failed: " + e.getMessage());
            return analysisRepository.save(analysisResult);
        }
    }

    /**
     * Generate content summary
     */
    private String generateSummary(WeeklyReport report) {
        StringBuilder summary = new StringBuilder();
        
        // Extract key information from different sections
        if (report.getWorkSummary() != null && !report.getWorkSummary().trim().isEmpty()) {
            String workSummary = preprocessText(report.getWorkSummary());
            List<String> keyPoints = extractKeyPoints(workSummary, 3);
            if (!keyPoints.isEmpty()) {
                summary.append("本周工作重点：").append(String.join("；", keyPoints)).append("。");
            }
        }

        if (report.getAchievements() != null && !report.getAchievements().trim().isEmpty()) {
            String achievements = preprocessText(report.getAchievements());
            List<String> keyAchievements = extractKeyPoints(achievements, 2);
            if (!keyAchievements.isEmpty()) {
                summary.append("主要成果：").append(String.join("；", keyAchievements)).append("。");
            }
        }

        if (report.getChallenges() != null && !report.getChallenges().trim().isEmpty()) {
            String challenges = preprocessText(report.getChallenges());
            List<String> keyChallenges = extractKeyPoints(challenges, 2);
            if (!keyChallenges.isEmpty()) {
                summary.append("面临挑战：").append(String.join("；", keyChallenges)).append("。");
            }
        }

        if (report.getNextWeekPlan() != null && !report.getNextWeekPlan().trim().isEmpty()) {
            String nextWeekPlan = preprocessText(report.getNextWeekPlan());
            List<String> keyPlans = extractKeyPoints(nextWeekPlan, 2);
            if (!keyPlans.isEmpty()) {
                summary.append("下周计划：").append(String.join("；", keyPlans)).append("。");
            }
        }

        return summary.length() > 0 ? summary.toString() : "本周工作总结暂无具体内容。";
    }

    /**
     * Extract keywords from report content
     */
    private String extractKeywords(WeeklyReport report) {
        Map<String, Integer> keywordFreq = new HashMap<>();
        List<String> allContent = Arrays.asList(
            report.getContent() != null ? report.getContent() : "",
            report.getWorkSummary() != null ? report.getWorkSummary() : "",
            report.getAchievements() != null ? report.getAchievements() : "",
            report.getChallenges() != null ? report.getChallenges() : "",
            report.getNextWeekPlan() != null ? report.getNextWeekPlan() : ""
        );

        for (String content : allContent) {
            if (content != null && !content.trim().isEmpty()) {
                String processedText = preprocessText(content);
                extractWordsFromText(processedText, keywordFreq);
            }
        }

        // Get top keywords
        List<String> topKeywords = keywordFreq.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        return String.join(", ", topKeywords);
    }

    /**
     * Analyze sentiment of the report
     */
    private String analyzeSentiment(WeeklyReport report) {
        int positiveScore = 0;
        int negativeScore = 0;
        int neutralScore = 0;

        List<String> positiveWords = Arrays.asList("成功", "完成", "达成", "提升", "优化", "顺利", "满意", "excellent", "good", "success", "achieve", "improve");
        List<String> negativeWords = Arrays.asList("问题", "困难", "挑战", "延期", "失败", "bug", "error", "problem", "issue", "delay", "difficult");

        String allText = getAllTextContent(report).toLowerCase();

        for (String word : positiveWords) {
            positiveScore += countOccurrences(allText, word.toLowerCase());
        }

        for (String word : negativeWords) {
            negativeScore += countOccurrences(allText, word.toLowerCase());
        }

        neutralScore = Math.max(1, allText.length() / 100 - positiveScore - negativeScore);

        String sentiment;
        if (positiveScore > negativeScore * 1.5) {
            sentiment = "积极";
        } else if (negativeScore > positiveScore * 1.5) {
            sentiment = "消极";
        } else {
            sentiment = "中性";
        }

        return String.format("情感倾向：%s（积极：%d，消极：%d，中性：%d）", sentiment, positiveScore, negativeScore, neutralScore);
    }

    /**
     * Assess risks in the report
     */
    private String assessRisks(WeeklyReport report) {
        List<String> risks = new ArrayList<>();
        String allText = getAllTextContent(report);

        // Check for risk keywords
        Set<String> riskKeywords = KEYWORD_CATEGORIES.get("risk");
        for (String keyword : riskKeywords) {
            if (allText.toLowerCase().contains(keyword.toLowerCase())) {
                risks.add("发现关键词：" + keyword);
            }
        }

        // Check challenges section
        if (report.getChallenges() != null && !report.getChallenges().trim().isEmpty()) {
            risks.add("存在明确的挑战和困难");
        }

        // Check completion rate
        int completionRate = report.getCompletionPercentage();
        if (completionRate < 60) {
            risks.add("报告完整性较低（" + completionRate + "%）");
        }

        // Check if report is late
        if (report.getIsLate() != null && report.getIsLate()) {
            risks.add("报告提交延迟");
        }

        String riskLevel;
        if (risks.size() >= 3) {
            riskLevel = "高风险";
        } else if (risks.size() >= 1) {
            riskLevel = "中等风险";
        } else {
            riskLevel = "低风险";
        }

        return String.format("风险等级：%s。风险点：%s", riskLevel, 
                           risks.isEmpty() ? "无明显风险" : String.join("；", risks));
    }

    /**
     * Generate intelligent suggestions
     */
    private String generateSuggestions(WeeklyReport report) {
        List<String> suggestions = new ArrayList<>();

        // Content completeness suggestions
        int completionRate = report.getCompletionPercentage();
        if (completionRate < 80) {
            suggestions.add("建议完善报告内容，提高信息完整性");
        }

        // Collaboration suggestions
        String allText = getAllTextContent(report);
        boolean hasCollaboration = KEYWORD_CATEGORIES.get("collaboration").stream()
            .anyMatch(keyword -> allText.toLowerCase().contains(keyword.toLowerCase()));
        if (!hasCollaboration) {
            suggestions.add("建议增加团队协作相关内容，加强沟通交流");
        }

        // Technical depth suggestions
        boolean hasTechnical = KEYWORD_CATEGORIES.get("technical").stream()
            .anyMatch(keyword -> allText.toLowerCase().contains(keyword.toLowerCase()));
        if (!hasTechnical && report.getWorkSummary() != null && report.getWorkSummary().length() < 200) {
            suggestions.add("建议增加技术细节，提供更具体的工作描述");
        }

        // Next week planning suggestions
        if (report.getNextWeekPlan() == null || report.getNextWeekPlan().trim().isEmpty()) {
            suggestions.add("建议添加下周工作计划，提高工作的前瞻性");
        }

        // Achievement highlighting suggestions
        if (report.getAchievements() == null || report.getAchievements().trim().isEmpty()) {
            boolean hasAchievementKeywords = KEYWORD_CATEGORIES.get("achievement").stream()
                .anyMatch(keyword -> allText.toLowerCase().contains(keyword.toLowerCase()));
            if (hasAchievementKeywords) {
                suggestions.add("建议在成果部分突出展示本周的主要成就");
            }
        }

        return suggestions.isEmpty() ? "当前报告质量良好，暂无改进建议" : String.join("；", suggestions);
    }

    /**
     * Analyze progress from the report
     */
    private String analyzeProgress(WeeklyReport report) {
        StringBuilder analysis = new StringBuilder();

        // Completion rate analysis
        int completionRate = report.getCompletionPercentage();
        analysis.append("报告完整度：").append(completionRate).append("%");

        // Work summary analysis
        if (report.getWorkSummary() != null) {
            int wordCount = report.getWorkSummary().length();
            if (wordCount > 500) {
                analysis.append("，工作内容详实");
            } else if (wordCount > 200) {
                analysis.append("，工作内容适中");
            } else {
                analysis.append("，工作内容简略");
            }
        }

        // Achievement analysis
        String achievementLevel;
        if (report.getAchievements() != null && report.getAchievements().length() > 100) {
            achievementLevel = "成果丰富";
        } else if (report.getAchievements() != null && report.getAchievements().length() > 50) {
            achievementLevel = "有一定成果";
        } else {
            achievementLevel = "成果待完善";
        }
        analysis.append("，").append(achievementLevel);

        // Planning analysis
        if (report.getNextWeekPlan() != null && report.getNextWeekPlan().length() > 50) {
            analysis.append("，下周规划明确");
        } else {
            analysis.append("，下周规划需加强");
        }

        return analysis.toString();
    }

    /**
     * Analyze workload from the report
     */
    private String analyzeWorkload(WeeklyReport report) {
        String allText = getAllTextContent(report);
        int totalLength = allText.length();

        String workloadLevel;
        if (totalLength > 2000) {
            workloadLevel = "工作量较大";
        } else if (totalLength > 1000) {
            workloadLevel = "工作量适中";
        } else {
            workloadLevel = "工作量偏少";
        }

        // Count task indicators
        int taskCount = countOccurrences(allText, "完成") + 
                       countOccurrences(allText, "处理") + 
                       countOccurrences(allText, "开发") + 
                       countOccurrences(allText, "测试") +
                       countOccurrences(allText, "finished") +
                       countOccurrences(allText, "completed");

        return String.format("%s，识别到约%d项任务活动", workloadLevel, Math.max(1, taskCount));
    }

    /**
     * Get analysis results for a report
     */
    @Cacheable(value = "analysisResults", key = "#reportId")
    public List<AIAnalysisResult> getAnalysisResults(Long reportId) {
        return analysisRepository.findByWeeklyReportId(reportId);
    }

    /**
     * Get analysis results by type
     */
    public Optional<AIAnalysisResult> getLatestAnalysisResult(Long reportId, AIAnalysisResult.AnalysisType analysisType) {
        return analysisRepository.findTopByWeeklyReportIdAndAnalysisTypeOrderByCreatedAtDesc(reportId, analysisType);
    }

    /**
     * Get pending analysis tasks
     */
    public List<AIAnalysisResult> getPendingAnalysisTasks() {
        return analysisRepository.findPendingAnalysisTasks();
    }

    /**
     * Get analysis statistics
     */
    public Map<String, Object> getAnalysisStatistics(LocalDateTime since) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalAnalysis", analysisRepository.count());
        stats.put("completedAnalysis", analysisRepository.countByStatus(AIAnalysisResult.AnalysisStatus.COMPLETED));
        stats.put("pendingAnalysis", analysisRepository.countByStatus(AIAnalysisResult.AnalysisStatus.PENDING));
        stats.put("failedAnalysis", analysisRepository.countByStatus(AIAnalysisResult.AnalysisStatus.FAILED));
        
        if (since != null) {
            stats.put("recentCompleted", analysisRepository.findRecentCompletedAnalysisSince(since).size());
            stats.put("recentFailed", analysisRepository.findFailedAnalysisTasksSince(since).size());
        }

        // Get analysis type distribution
        List<Object[]> typeStats = analysisRepository.countByAnalysisType();
        Map<String, Long> typeDistribution = new HashMap<>();
        for (Object[] stat : typeStats) {
            typeDistribution.put(stat[0].toString(), (Long) stat[1]);
        }
        stats.put("typeDistribution", typeDistribution);

        return stats;
    }

    // Helper methods for text processing and analysis

    private String preprocessText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        return HTML_TAG_PATTERN.matcher(text)
            .replaceAll(" ")
            .replaceAll(WHITESPACE_PATTERN.pattern(), " ")
            .trim();
    }

    private List<String> extractKeyPoints(String text, int maxPoints) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String[] sentences = text.split("[。！？\\n]");
        return Arrays.stream(sentences)
            .filter(s -> s.trim().length() > 10)
            .map(String::trim)
            .limit(maxPoints)
            .collect(Collectors.toList());
    }

    private void extractWordsFromText(String text, Map<String, Integer> keywordFreq) {
        // Extract Chinese words (simplified approach)
        String[] chineseWords = CHINESE_PATTERN.matcher(text).results()
            .map(mr -> text.substring(mr.start(), mr.end()))
            .toArray(String[]::new);

        for (String word : chineseWords) {
            if (word.length() >= 2) {
                keywordFreq.merge(word, 1, Integer::sum);
            }
        }

        // Extract English words
        String[] englishWords = text.toLowerCase().split("\\W+");
        for (String word : englishWords) {
            if (word.length() >= 3 && ENGLISH_PATTERN.matcher(word).matches()) {
                keywordFreq.merge(word, 1, Integer::sum);
            }
        }
    }

    private String getAllTextContent(WeeklyReport report) {
        StringBuilder content = new StringBuilder();
        if (report.getContent() != null) content.append(report.getContent()).append(" ");
        if (report.getWorkSummary() != null) content.append(report.getWorkSummary()).append(" ");
        if (report.getAchievements() != null) content.append(report.getAchievements()).append(" ");
        if (report.getChallenges() != null) content.append(report.getChallenges()).append(" ");
        if (report.getNextWeekPlan() != null) content.append(report.getNextWeekPlan()).append(" ");
        if (report.getAdditionalNotes() != null) content.append(report.getAdditionalNotes()).append(" ");
        return content.toString();
    }

    private int countOccurrences(String text, String word) {
        if (text == null || word == null || text.isEmpty() || word.isEmpty()) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(word, index)) != -1) {
            count++;
            index += word.length();
        }
        return count;
    }

    // Confidence calculation methods
    private double calculateSummaryConfidence(WeeklyReport report, String result) {
        if (result == null || result.trim().isEmpty()) return 0.0;
        
        double confidence = 0.5; // Base confidence
        
        // Higher confidence if more sections are filled
        int filledSections = 0;
        if (report.getWorkSummary() != null && !report.getWorkSummary().trim().isEmpty()) filledSections++;
        if (report.getAchievements() != null && !report.getAchievements().trim().isEmpty()) filledSections++;
        if (report.getChallenges() != null && !report.getChallenges().trim().isEmpty()) filledSections++;
        if (report.getNextWeekPlan() != null && !report.getNextWeekPlan().trim().isEmpty()) filledSections++;
        
        confidence += filledSections * 0.1;
        
        // Higher confidence for longer, more detailed summaries
        if (result.length() > 100) confidence += 0.2;
        if (result.length() > 200) confidence += 0.1;
        
        return Math.min(1.0, confidence);
    }

    private double calculateKeywordConfidence(WeeklyReport report, String result) {
        if (result == null || result.trim().isEmpty()) return 0.0;
        
        String[] keywords = result.split(",");
        double confidence = 0.3 + Math.min(0.5, keywords.length * 0.05);
        
        // Higher confidence if keywords cover different categories
        String allText = getAllTextContent(report).toLowerCase();
        int categoryMatches = 0;
        for (Set<String> categoryKeywords : KEYWORD_CATEGORIES.values()) {
            if (categoryKeywords.stream().anyMatch(kw -> allText.contains(kw.toLowerCase()))) {
                categoryMatches++;
            }
        }
        
        confidence += categoryMatches * 0.05;
        return Math.min(1.0, confidence);
    }

    private double calculateSentimentConfidence(WeeklyReport report, String result) {
        // Sentiment analysis confidence based on the presence of clear indicators
        return 0.75; // Moderate confidence for rule-based sentiment analysis
    }

    private double calculateRiskConfidence(WeeklyReport report, String result) {
        if (result == null || result.trim().isEmpty()) return 0.0;
        
        double confidence = 0.6; // Base confidence for risk assessment
        
        // Higher confidence if specific risk indicators are found
        if (result.contains("高风险")) confidence += 0.2;
        if (result.contains("挑战") || result.contains("问题")) confidence += 0.1;
        if (report.getIsLate() != null && report.getIsLate()) confidence += 0.1;
        
        return Math.min(1.0, confidence);
    }

    private double calculateSuggestionConfidence(WeeklyReport report, String result) {
        if (result == null || result.trim().isEmpty()) return 0.0;
        
        // Confidence based on report completeness and content analysis
        int completionRate = report.getCompletionPercentage();
        double confidence = 0.4 + (completionRate / 100.0) * 0.4;
        
        if (!result.equals("当前报告质量良好，暂无改进建议")) {
            confidence += 0.2; // Higher confidence when specific suggestions are made
        }
        
        return Math.min(1.0, confidence);
    }

    private double calculateProgressConfidence(WeeklyReport report, String result) {
        // Progress analysis confidence based on data availability
        return 0.85; // High confidence for objective progress metrics
    }

    private double calculateWorkloadConfidence(WeeklyReport report, String result) {
        // Workload analysis confidence based on content length and task indicators
        return 0.8; // High confidence for quantitative workload analysis
    }
}