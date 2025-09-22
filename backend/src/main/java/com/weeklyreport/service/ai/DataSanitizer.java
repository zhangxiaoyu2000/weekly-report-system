package com.weeklyreport.service.ai;

import com.weeklyreport.entity.SimpleProject;
import com.weeklyreport.entity.WeeklyReport;
import com.weeklyreport.entity.ProjectPhase;
import com.weeklyreport.service.ai.dto.EnhancedAIAnalysisRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Data sanitizer for removing sensitive information before AI analysis
 */
@Component
public class DataSanitizer {
    
    // 敏感信息匹配模式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\b(?:\\+86|86)?\\s*1[3-9]\\d{9}\\b");
    
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
        "\\b\\d{17}[\\dXx]\\b");
    
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile(
        "\\b\\d{16,19}\\b");
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "(?i)(?:password|密码|pwd)\\s*[:：=]\\s*\\S+");
    
    /**
     * Sanitize project data for AI analysis
     */
    public EnhancedAIAnalysisRequest.ProjectData sanitizeProjectData(SimpleProject project) {
        EnhancedAIAnalysisRequest.ProjectData data = new EnhancedAIAnalysisRequest.ProjectData();
        
        data.setProjectName(sanitizeText(project.getProjectName()));
        data.setProjectContent(sanitizeText(project.getProjectContent()));
        data.setProjectMembers(anonymizeMembers(project.getProjectMembers()));
        data.setExpectedResults(sanitizeText(project.getExpectedResults()));
        data.setTimeline(sanitizeText(project.getTimeline()));
        data.setStopLoss(sanitizeText(project.getStopLoss()));
        
        // SimpleProject实体不支持项目阶段 - 设置为空列表
        data.setProjectPhases(new ArrayList<>());
        
        return data;
    }
    
    /**
     * Sanitize project phase data
     */
    private EnhancedAIAnalysisRequest.ProjectPhase sanitizeProjectPhase(ProjectPhase phase) {
        EnhancedAIAnalysisRequest.ProjectPhase sanitizedPhase = new EnhancedAIAnalysisRequest.ProjectPhase();
        
        sanitizedPhase.setPhaseName(sanitizeText(phase.getPhaseName()));
        sanitizedPhase.setPhaseOrder(1); // 简化版本中没有phaseOrder字段，设置默认值
        sanitizedPhase.setPhaseDescription(sanitizeText(phase.getDescription()));
        sanitizedPhase.setAssignedMembers(anonymizeMembers(phase.getAssignedMembers()));
        sanitizedPhase.setTimeline(sanitizeText(phase.getSchedule()));
        sanitizedPhase.setEstimatedResults(sanitizeText(phase.getExpectedResults()));
        // actualResults 已移至 DevTaskReport，暂时设置默认状态
        sanitizedPhase.setStatus("PENDING");
        
        return sanitizedPhase;
    }
    
    /**
     * Sanitize weekly report data for AI analysis
     */
    public EnhancedAIAnalysisRequest.WeeklyReportData sanitizeWeeklyReportData(WeeklyReport report) {
        EnhancedAIAnalysisRequest.WeeklyReportData data = new EnhancedAIAnalysisRequest.WeeklyReportData();
        
        data.setTitle(sanitizeText(report.getTitle()));
        data.setContent(sanitizeText(report.getContent()));
        data.setDevelopmentOpportunities(sanitizeText(report.getDevelopmentOpportunities()));
        data.setAdditionalNotes(sanitizeText(report.getAdditionalNotes()));
        
        return data;
    }
    
    /**
     * Remove sensitive information from text
     */
    private String sanitizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        String sanitized = text;
        
        // 替换敏感信息
        sanitized = EMAIL_PATTERN.matcher(sanitized).replaceAll("[邮箱地址]");
        sanitized = PHONE_PATTERN.matcher(sanitized).replaceAll("[电话号码]");
        sanitized = ID_CARD_PATTERN.matcher(sanitized).replaceAll("[身份证号]");
        sanitized = BANK_CARD_PATTERN.matcher(sanitized).replaceAll("[银行卡号]");
        sanitized = PASSWORD_PATTERN.matcher(sanitized).replaceAll("密码: [已隐藏]");
        
        return sanitized;
    }
    
    /**
     * Anonymize member names while preserving count and roles
     */
    private String anonymizeMembers(String members) {
        if (members == null || members.trim().isEmpty()) {
            return members;
        }
        
        // 简单的成员匿名化：保留职位，隐藏姓名
        String anonymized = members;
        
        // 替换常见的姓名模式
        anonymized = anonymized.replaceAll("\\b[\\u4e00-\\u9fa5]{2,4}\\b", "[成员]");
        
        // 如果包含逗号分隔的成员列表，保留数量
        if (anonymized.contains(",")) {
            String[] memberArray = anonymized.split(",");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < memberArray.length; i++) {
                if (i > 0) result.append(", ");
                result.append("成员").append(i + 1);
            }
            return result.toString();
        }
        
        return anonymized;
    }
    
    /**
     * Check if text contains sensitive information
     */
    public boolean containsSensitiveInfo(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(text).find() ||
               PHONE_PATTERN.matcher(text).find() ||
               ID_CARD_PATTERN.matcher(text).find() ||
               BANK_CARD_PATTERN.matcher(text).find() ||
               PASSWORD_PATTERN.matcher(text).find();
    }
    
    /**
     * Get sanitization report
     */
    public SanitizationReport getSanitizationReport(String originalText) {
        SanitizationReport report = new SanitizationReport();
        
        if (originalText == null) {
            return report;
        }
        
        report.setOriginalLength(originalText.length());
        report.setEmailsFound(EMAIL_PATTERN.matcher(originalText).results().count());
        report.setPhonesFound(PHONE_PATTERN.matcher(originalText).results().count());
        report.setIdCardsFound(ID_CARD_PATTERN.matcher(originalText).results().count());
        report.setBankCardsFound(BANK_CARD_PATTERN.matcher(originalText).results().count());
        report.setPasswordsFound(PASSWORD_PATTERN.matcher(originalText).results().count());
        
        String sanitized = sanitizeText(originalText);
        report.setSanitizedLength(sanitized.length());
        report.setHasSensitiveInfo(containsSensitiveInfo(originalText));
        
        return report;
    }
    
    /**
     * Sanitization report
     */
    public static class SanitizationReport {
        private int originalLength;
        private int sanitizedLength;
        private long emailsFound;
        private long phonesFound;
        private long idCardsFound;
        private long bankCardsFound;
        private long passwordsFound;
        private boolean hasSensitiveInfo;
        
        // Getters and setters
        public int getOriginalLength() {
            return originalLength;
        }
        
        public void setOriginalLength(int originalLength) {
            this.originalLength = originalLength;
        }
        
        public int getSanitizedLength() {
            return sanitizedLength;
        }
        
        public void setSanitizedLength(int sanitizedLength) {
            this.sanitizedLength = sanitizedLength;
        }
        
        public long getEmailsFound() {
            return emailsFound;
        }
        
        public void setEmailsFound(long emailsFound) {
            this.emailsFound = emailsFound;
        }
        
        public long getPhonesFound() {
            return phonesFound;
        }
        
        public void setPhonesFound(long phonesFound) {
            this.phonesFound = phonesFound;
        }
        
        public long getIdCardsFound() {
            return idCardsFound;
        }
        
        public void setIdCardsFound(long idCardsFound) {
            this.idCardsFound = idCardsFound;
        }
        
        public long getBankCardsFound() {
            return bankCardsFound;
        }
        
        public void setBankCardsFound(long bankCardsFound) {
            this.bankCardsFound = bankCardsFound;
        }
        
        public long getPasswordsFound() {
            return passwordsFound;
        }
        
        public void setPasswordsFound(long passwordsFound) {
            this.passwordsFound = passwordsFound;
        }
        
        public boolean isHasSensitiveInfo() {
            return hasSensitiveInfo;
        }
        
        public void setHasSensitiveInfo(boolean hasSensitiveInfo) {
            this.hasSensitiveInfo = hasSensitiveInfo;
        }
    }
}