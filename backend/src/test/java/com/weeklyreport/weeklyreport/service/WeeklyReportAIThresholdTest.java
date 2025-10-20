package com.weeklyreport.weeklyreport.service;

import com.weeklyreport.ai.entity.AIAnalysisResult;
import com.weeklyreport.ai.repository.AIAnalysisResultRepository;
import com.weeklyreport.ai.service.AIAnalysisService;
import com.weeklyreport.weeklyreport.entity.WeeklyReport;
import com.weeklyreport.weeklyreport.repository.WeeklyReportRepository;
import com.weeklyreport.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeeklyReportAIThresholdTest {

    @Test
    void lowConfidenceShouldRejectDuringStatusSync() throws Exception {
        WeeklyReport report = buildWeeklyReport();

        WeeklyReportRepository weeklyRepo = weeklyReportRepositoryStub(report);
        AIAnalysisService aiAnalysisService = new AIAnalysisService();
        setField(aiAnalysisService, "weeklyReportRepository", weeklyRepo);
        setField(aiAnalysisService, "aiAnalysisResultRepository", aiAnalysisResultRepositoryStub(null));
        setField(aiAnalysisService, "weeklyReportConfidenceThreshold", 0.7d);

        AIAnalysisResult result = new AIAnalysisResult();
        result.setId(10L);
        result.setEntityType(AIAnalysisResult.EntityType.WEEKLY_REPORT);
        result.setStatus(AIAnalysisResult.AnalysisStatus.COMPLETED);
        result.setConfidence(0.45);
        result.setResult("建议补充关键成果信息");

        Method updateStatus = AIAnalysisService.class
            .getDeclaredMethod("updateWeeklyReportStatus", WeeklyReport.class, AIAnalysisResult.class);
        updateStatus.setAccessible(true);
        updateStatus.invoke(aiAnalysisService, report, result);

        assertEquals(WeeklyReport.ReportStatus.REJECTED, report.getStatus());
        assertEquals(WeeklyReport.RejectedBy.AI, report.getRejectedBy());
        assertTrue(report.getRejectionReason().contains("AI分析置信度过低"));
    }

    @Test
    void notificationShouldKeepReportRejectedWhenConfidenceLow() throws Exception {
        WeeklyReport report = buildWeeklyReport();
        report.setAiAnalysisId(99L);

        AIAnalysisResult analysisResult = new AIAnalysisResult();
        analysisResult.setId(99L);
        analysisResult.setEntityType(AIAnalysisResult.EntityType.WEEKLY_REPORT);
        analysisResult.setStatus(AIAnalysisResult.AnalysisStatus.COMPLETED);
        analysisResult.setConfidence(0.5);
        analysisResult.setResult("建议完善下周计划");

        WeeklyReportRepository weeklyRepo = weeklyReportRepositoryStub(report);
        AIAnalysisResultRepository resultRepository = aiAnalysisResultRepositoryStub(analysisResult);
        UserRepository userRepository = userRepositoryStub();
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();

        WeeklyReportNotificationService notificationService = new WeeklyReportNotificationService(
            weeklyRepo,
            userRepository,
            eventPublisher,
            resultRepository
        );
        setField(notificationService, "weeklyReportConfidenceThreshold", 0.7d);

        notificationService.handleAIAnalysisCompleted(report.getId());

        assertEquals(WeeklyReport.ReportStatus.REJECTED, report.getStatus());
        assertEquals(WeeklyReport.RejectedBy.AI, report.getRejectedBy());
        assertTrue(report.getRejectionReason().contains("低于阈值"));
        assertFalse(eventPublisher.wasPublished());
    }

    private WeeklyReport buildWeeklyReport() {
        WeeklyReport report = new WeeklyReport();
        report.setId(1L);
        report.setUserId(42L);
        report.setTitle("低置信度周报");
        report.setReportWeek("2025-W01");
        report.setStatus(WeeklyReport.ReportStatus.DRAFT);
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        return report;
    }

    private WeeklyReportRepository weeklyReportRepositoryStub(WeeklyReport trackedReport) {
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            if ("findById".equals(name)) {
                Long id = (Long) args[0];
                return trackedReport.getId().equals(id) ? Optional.of(trackedReport) : Optional.empty();
            }
            if ("save".equals(name)) {
                return args[0];
            }
            if ("saveAll".equals(name)) {
                return args[0];
            }
            if ("toString".equals(name)) {
                return "WeeklyReportRepositoryStub";
            }
            if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(name)) {
                return proxy == args[0];
            }
            throw new UnsupportedOperationException("Unsupported method in stub: " + name);
        };

        return (WeeklyReportRepository) Proxy.newProxyInstance(
            WeeklyReportRepository.class.getClassLoader(),
            new Class[]{WeeklyReportRepository.class},
            handler
        );
    }

    private AIAnalysisResultRepository aiAnalysisResultRepositoryStub(AIAnalysisResult storedResult) {
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            if ("findById".equals(name)) {
                if (storedResult == null) {
                    return Optional.empty();
                }
                Long id = (Long) args[0];
                return storedResult.getId().equals(id) ? Optional.of(storedResult) : Optional.empty();
            }
            if ("save".equals(name)) {
                return args[0];
            }
            if ("toString".equals(name)) {
                return "AIAnalysisResultRepositoryStub";
            }
            if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(name)) {
                return proxy == args[0];
            }
            throw new UnsupportedOperationException("Unsupported method in stub: " + name);
        };

        return (AIAnalysisResultRepository) Proxy.newProxyInstance(
            AIAnalysisResultRepository.class.getClassLoader(),
            new Class[]{AIAnalysisResultRepository.class},
            handler
        );
    }

    private UserRepository userRepositoryStub() {
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            if ("findById".equals(name)) {
                return Optional.empty();
            }
            if ("toString".equals(name)) {
                return "UserRepositoryStub";
            }
            if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(name)) {
                return proxy == args[0];
            }
            throw new UnsupportedOperationException("Unsupported method in stub: " + name);
        };

        return (UserRepository) Proxy.newProxyInstance(
            UserRepository.class.getClassLoader(),
            new Class[]{UserRepository.class},
            handler
        );
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static class RecordingEventPublisher implements ApplicationEventPublisher {
        private final AtomicBoolean published = new AtomicBoolean(false);

        @Override
        public void publishEvent(Object event) {
            published.set(true);
        }

        @Override
        public void publishEvent(ApplicationEvent event) {
            published.set(true);
        }

        boolean wasPublished() {
            return published.get();
        }
    }
}
