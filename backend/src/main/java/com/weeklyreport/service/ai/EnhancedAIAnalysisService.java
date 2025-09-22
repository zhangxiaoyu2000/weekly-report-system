package com.weeklyreport.service.ai;

import com.weeklyreport.service.ai.dto.StandardizedAIResponse;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.time.LocalDateTime;

/**
 * Enhanced AI Analysis Service - simplified stub for core functionality
 */
@Service
public class EnhancedAIAnalysisService {
    
    /**
     * Analyze project - simplified implementation
     */
    public String analyzeProject(Object project) {
        return "AI analysis feature temporarily disabled";
    }
    
    /**
     * Analyze weekly report - simplified implementation  
     */
    public String analyzeWeeklyReport(Object report) {
        return "AI analysis feature temporarily disabled";
    }
    
    /**
     * Get analysis result - simplified implementation
     */
    public String getAnalysisResult(Long id) {
        return "AI analysis feature temporarily disabled";
    }
    
    /**
     * Analyze project feasibility - simplified implementation
     */
    public StandardizedAIResponse analyzeProjectFeasibility(Long projectId, Long userId) {
        StandardizedAIResponse response = new StandardizedAIResponse();
        response.setIsPass(true);
        response.setProposal("AI analysis feature temporarily disabled - project approved by default");
        response.setConfidence(0.5);
        response.setProviderId("stub-service");
        response.setProcessingTimeMs(100L);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
    
    /**
     * Analyze project feasibility asynchronously - simplified implementation
     */
    public CompletableFuture<StandardizedAIResponse> analyzeProjectFeasibilityAsync(Long projectId, Long userId) {
        return CompletableFuture.supplyAsync(() -> analyzeProjectFeasibility(projectId, userId));
    }
    
    /**
     * Analyze weekly report quality - simplified implementation
     */
    public StandardizedAIResponse analyzeWeeklyReportQuality(Long reportId, Long userId) {
        StandardizedAIResponse response = new StandardizedAIResponse();
        response.setIsPass(true);
        response.setProposal("AI analysis feature temporarily disabled - report approved by default");
        response.setConfidence(0.5);
        response.setProviderId("stub-service");
        response.setProcessingTimeMs(100L);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
    
    /**
     * Analyze weekly report quality asynchronously - simplified implementation
     */
    public CompletableFuture<StandardizedAIResponse> analyzeWeeklyReportQualityAsync(Long reportId, Long userId) {
        return CompletableFuture.supplyAsync(() -> analyzeWeeklyReportQuality(reportId, userId));
    }
}