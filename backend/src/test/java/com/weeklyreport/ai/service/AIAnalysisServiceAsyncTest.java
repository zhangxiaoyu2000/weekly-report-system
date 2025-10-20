package com.weeklyreport.ai.service;

import com.weeklyreport.ai.entity.AIAnalysisResult;
import com.weeklyreport.ai.repository.AIAnalysisResultRepository;
import com.weeklyreport.weeklyreport.entity.WeeklyReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * AI分析服务异步功能测试
 */
@SpringJUnitConfig
public class AIAnalysisServiceAsyncTest {

    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisServiceAsyncTest.class);

    @Mock
    private AIAnalysisResultRepository aiAnalysisResultRepository;

    private AIAnalysisService aiAnalysisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        aiAnalysisService = new AIAnalysisService();
        
        // 使用反射注入依赖 (在实际环境中会通过Spring注入)
        try {
            var field = AIAnalysisService.class.getDeclaredField("aiAnalysisResultRepository");
            field.setAccessible(true);
            field.set(aiAnalysisService, aiAnalysisResultRepository);
        } catch (Exception e) {
            logger.error("设置依赖注入失败", e);
        }
    }

    @Test
    void testAsyncAnalysisWithTimeout() throws Exception {
        logger.info("🧪 开始测试异步AI分析功能");
        
        // 创建测试周报
        WeeklyReport testReport = createTestReport();
        
        // Mock repository行为
        AIAnalysisResult mockResult = createMockAnalysisResult();
        when(aiAnalysisResultRepository.save(any(AIAnalysisResult.class)))
            .thenReturn(mockResult);
        
        // 执行异步分析
        CompletableFuture<AIAnalysisResult> future = aiAnalysisService.analyzeWeeklyReportAsync(testReport);
        
        // 验证异步特性
        assertNotNull(future, "异步Future对象不应为null");
        logger.info("🧪 异步任务已启动");
        
        // 等待结果 (最多5秒)
        AIAnalysisResult result = future.get(5, TimeUnit.SECONDS);
        
        // 验证结果
        assertNotNull(result, "分析结果不应为null");
        assertEquals(AIAnalysisResult.AnalysisStatus.COMPLETED, result.getStatus());
        assertEquals(testReport.getId(), result.getReportId());
        
        logger.info("🧪 ✅ 异步AI分析测试通过 - 结果ID: {}", result.getId());
    }

    @Test
    void testAsyncAnalysisErrorHandling() throws Exception {
        logger.info("🧪 开始测试异步AI分析错误处理");
        
        // 创建测试周报
        WeeklyReport testReport = createTestReport();
        
        // Mock repository抛出异常
        when(aiAnalysisResultRepository.save(any(AIAnalysisResult.class)))
            .thenThrow(new RuntimeException("数据库连接失败"));
        
        // 执行异步分析
        CompletableFuture<AIAnalysisResult> future = aiAnalysisService.analyzeWeeklyReportAsync(testReport);
        
        // 验证异常处理
        assertThrows(Exception.class, () -> {
            future.get(5, TimeUnit.SECONDS);
        }, "应该抛出异常");
        
        logger.info("🧪 ✅ 异步AI分析错误处理测试通过");
    }

    @Test
    void testConcurrentAnalysis() throws Exception {
        logger.info("🧪 开始测试并发AI分析");
        
        // Mock repository行为
        AIAnalysisResult mockResult = createMockAnalysisResult();
        when(aiAnalysisResultRepository.save(any(AIAnalysisResult.class)))
            .thenReturn(mockResult);
        
        // 创建多个并发任务
        CompletableFuture<AIAnalysisResult>[] futures = new CompletableFuture[5];
        for (int i = 0; i < 5; i++) {
            WeeklyReport report = createTestReport();
            report.setId((long) (i + 1));
            report.setTitle("测试周报 " + (i + 1));
            futures[i] = aiAnalysisService.analyzeWeeklyReportAsync(report);
        }
        
        // 等待所有任务完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures);
        allOf.get(10, TimeUnit.SECONDS);
        
        // 验证结果
        for (int i = 0; i < 5; i++) {
            AIAnalysisResult result = futures[i].get();
            assertNotNull(result);
            assertEquals(AIAnalysisResult.AnalysisStatus.COMPLETED, result.getStatus());
            logger.info("🧪 并发任务 {} 完成", i + 1);
        }
        
        logger.info("🧪 ✅ 并发AI分析测试通过");
    }

    private WeeklyReport createTestReport() {
        WeeklyReport report = new WeeklyReport();
        report.setId(1L);
        report.setTitle("测试周报");
        report.setReportWeek("2025年第1周");
        report.setAdditionalNotes("测试备注");
        report.setDevelopmentOpportunities("测试发展机会");
        report.setUserId(1L);
        report.setStatus(WeeklyReport.ReportStatus.DRAFT);
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        return report;
    }

    private AIAnalysisResult createMockAnalysisResult() {
        AIAnalysisResult result = new AIAnalysisResult();
        result.setId(100L);
        result.setReportId(1L);
        result.setEntityType(AIAnalysisResult.EntityType.WEEKLY_REPORT);
        result.setAnalysisType(AIAnalysisResult.AnalysisType.COMPLETENESS_CHECK);
        result.setStatus(AIAnalysisResult.AnalysisStatus.COMPLETED);
        result.setResult("测试分析结果：周报内容完整，质量良好");
        result.setConfidence(0.85);
        result.setCreatedAt(LocalDateTime.now());
        result.setUpdatedAt(LocalDateTime.now());
        result.setCompletedAt(LocalDateTime.now());
        return result;
    }
}