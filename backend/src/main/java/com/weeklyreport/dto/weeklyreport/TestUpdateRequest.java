package com.weeklyreport.dto.weeklyreport;

/**
 * Simple test DTO for debugging JSON parsing issues
 */
public class TestUpdateRequest {
    
    private String title;
    private String reportWeek;
    private String additionalNotes;
    private String developmentOpportunities;
    
    public TestUpdateRequest() {}
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getReportWeek() {
        return reportWeek;
    }
    
    public void setReportWeek(String reportWeek) {
        this.reportWeek = reportWeek;
    }
    
    public String getAdditionalNotes() {
        return additionalNotes;
    }
    
    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }
    
    public String getDevelopmentOpportunities() {
        return developmentOpportunities;
    }
    
    public void setDevelopmentOpportunities(String developmentOpportunities) {
        this.developmentOpportunities = developmentOpportunities;
    }
}