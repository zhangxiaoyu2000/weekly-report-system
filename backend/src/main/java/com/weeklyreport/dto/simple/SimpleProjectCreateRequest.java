package com.weeklyreport.dto.simple;

import jakarta.validation.constraints.NotBlank;

/**
 * 简化的项目创建请求DTO
 */
public class SimpleProjectCreateRequest {

    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    @NotBlank(message = "项目内容不能为空")
    private String projectContent;

    @NotBlank(message = "项目成员不能为空")
    private String projectMembers;


    @NotBlank(message = "预期结果不能为空，需要以量化指标形式填写")
    private String expectedResults;

    private String actualResults;

    @NotBlank(message = "时间线不能为空")
    private String timeline;

    @NotBlank(message = "止损点不能为空")
    private String stopLoss;

    // 构造函数
    public SimpleProjectCreateRequest() {}

    // Getters and Setters
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectContent() {
        return projectContent;
    }

    public void setProjectContent(String projectContent) {
        this.projectContent = projectContent;
    }

    public String getProjectMembers() {
        return projectMembers;
    }

    public void setProjectMembers(String projectMembers) {
        this.projectMembers = projectMembers;
    }


    public String getExpectedResults() {
        return expectedResults;
    }

    public void setExpectedResults(String expectedResults) {
        this.expectedResults = expectedResults;
    }

    public String getActualResults() {
        return actualResults;
    }

    public void setActualResults(String actualResults) {
        this.actualResults = actualResults;
    }

    public String getTimeline() {
        return timeline;
    }

    public void setTimeline(String timeline) {
        this.timeline = timeline;
    }

    public String getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(String stopLoss) {
        this.stopLoss = stopLoss;
    }
}