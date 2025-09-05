package com.weeklyreport.service.ai.mock;

import com.weeklyreport.config.AIConfig;
import com.weeklyreport.service.ai.AIServiceType;
import com.weeklyreport.service.ai.AbstractAIServiceProvider;
import com.weeklyreport.service.ai.dto.AIAnalysisRequest;
import com.weeklyreport.service.ai.dto.AIAnalysisResponse;
import com.weeklyreport.service.ai.exception.AIServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Mock AI service for testing and development
 */
@Service
@ConditionalOnProperty(name = "ai.mock.enabled", havingValue = "true", matchIfMissing = true)
public class MockAIService extends AbstractAIServiceProvider {
    
    private final AIConfig aiConfig;
    
    @Autowired
    public MockAIService(AIConfig aiConfig) {
        this.aiConfig = aiConfig;
    }
    
    @Override
    public AIServiceType getServiceType() {
        return AIServiceType.MOCK;
    }
    
    @Override
    public boolean isAvailable() {
        return aiConfig.getMock().isEnabled();
    }
    
    @Override
    public String getConfigurationStatus() {
        return aiConfig.getMock().isEnabled() ? "CONFIGURED" : "DISABLED";
    }
    
    @Override
    public int getMaxTokens() {
        return 4096; // Mock unlimited tokens
    }
    
    @Override
    protected AIAnalysisResponse performAnalysis(AIAnalysisRequest request) throws AIServiceException {
        // Simulate processing delay
        try {
            Thread.sleep(aiConfig.getMock().getSimulatedDelay());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AIServiceException("Mock analysis was interrupted", e);
        }
        
        AIAnalysisResponse response = new AIAnalysisResponse();
        response.setAnalysisType(request.getAnalysisType());
        response.setConfidence(aiConfig.getMock().getConfidence());
        
        // Generate mock response based on analysis type
        switch (request.getAnalysisType()) {
            case SUMMARY:
                response.setResult(generateMockSummary(request.getContent()));
                break;
            case SENTIMENT:
                response.setResult(generateMockSentiment());
                break;
            case KEYWORDS:
                response.setKeywords(generateMockKeywords());
                response.setResult(String.join(", ", response.getKeywords()));
                break;
            case RISK_ASSESSMENT:
                response.setResult(generateMockRiskAssessment());
                break;
            case SUGGESTIONS:
                response.setResult(generateMockSuggestions());
                break;
            case PROGRESS_PREDICTION:
                response.setResult(generateMockProgressPrediction());
                break;
            default:
                response.setResult("Mock analysis completed for type: " + request.getAnalysisType());
        }
        
        // Set mock keywords if not already set
        if (response.getKeywords() == null) {
            response.setKeywords(generateMockKeywords());
        }
        
        // Add mock metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("mockService", true);
        metadata.put("simulatedDelay", aiConfig.getMock().getSimulatedDelay());
        metadata.put("contentLength", request.getContent().length());
        response.setMetadata(metadata);
        
        return response;
    }
    
    private String generateMockSummary(String content) {
        int contentLength = content.length();
        String summary = "Mock Summary: This weekly report contains " + contentLength + " characters of content. ";
        
        if (contentLength > 500) {
            summary += "The report appears comprehensive with detailed updates across multiple areas. ";
        } else {
            summary += "The report is concise and focuses on key highlights. ";
        }
        
        summary += "Key achievements have been noted, and progress indicators show positive momentum. " +
                  "Some challenges are identified with proposed solutions. Overall status appears on track.";
        
        return summary;
    }
    
    private String generateMockSentiment() {
        String[] sentiments = {"POSITIVE", "NEUTRAL", "MIXED"};
        Random random = new Random();
        return sentiments[random.nextInt(sentiments.length)];
    }
    
    private List<String> generateMockKeywords() {
        List<String> keywords = Arrays.asList(
            "Weekly Report", "Project Progress", "Development", "Testing", 
            "Implementation", "Milestone", "Team Collaboration", "Quality Assurance",
            "Performance", "Delivery"
        );
        
        // Return 5-7 random keywords
        Collections.shuffle(keywords);
        Random random = new Random();
        int count = 5 + random.nextInt(3); // 5-7 keywords
        return keywords.subList(0, Math.min(count, keywords.size()));
    }
    
    private String generateMockRiskAssessment() {
        String[] riskLevels = {"LOW", "MEDIUM"};
        Random random = new Random();
        String riskLevel = riskLevels[random.nextInt(riskLevels.length)];
        
        String assessment = "Risk Level: " + riskLevel + ". ";
        
        if ("MEDIUM".equals(riskLevel)) {
            assessment += "Some potential blockers identified including resource constraints " +
                         "and dependency management. Recommended to monitor closely and have " +
                         "contingency plans ready.";
        } else {
            assessment += "Project appears to be progressing smoothly with minimal risks identified. " +
                         "Standard monitoring procedures should be sufficient.";
        }
        
        return assessment;
    }
    
    private String generateMockSuggestions() {
        return "Mock Improvement Suggestions:\n" +
               "1. Consider implementing automated testing to improve code quality\n" +
               "2. Schedule regular team sync meetings to enhance communication\n" +
               "3. Review and update project documentation for better knowledge sharing\n" +
               "4. Implement code review processes to maintain standards\n" +
               "5. Consider performance optimization for better user experience";
    }
    
    private String generateMockProgressPrediction() {
        Random random = new Random();
        int confidence = 75 + random.nextInt(20); // 75-95% confidence
        
        return "Progress Prediction: Based on current velocity and trends, " +
               "there is a " + confidence + "% confidence level of meeting upcoming milestones. " +
               "Key factors affecting progress include team productivity, technical complexity, " +
               "and external dependencies. Recommend maintaining current pace and monitoring " +
               "critical path activities closely.";
    }
}