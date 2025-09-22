package com.weeklyreport.dto.simple;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 简化的周报创建请求DTO
 */
public class SimpleWeeklyReportCreateRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;


    @NotBlank(message = "实际结果不能为空")
    private String actualResults;

    // 构造函数
    public SimpleWeeklyReportCreateRequest() {}

    // Getters and Setters
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }


    public String getActualResults() {
        return actualResults;
    }

    public void setActualResults(String actualResults) {
        this.actualResults = actualResults;
    }
}