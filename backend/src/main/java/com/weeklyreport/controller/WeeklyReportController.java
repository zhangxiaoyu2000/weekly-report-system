package com.weeklyreport.controller;

import com.weeklyreport.dto.ApiResponse;
import com.weeklyreport.dto.weeklyreport.*;
import com.weeklyreport.entity.WeeklyReport;
import com.weeklyreport.security.CustomUserPrincipal;
import com.weeklyreport.service.WeeklyReportService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Weekly Report operations
 * Handles CRUD operations for weekly reports
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WeeklyReportController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyReportController.class);

    @Autowired
    private WeeklyReportService weeklyReportService;

    /**
     * Create a new weekly report
     * POST /api/reports
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<WeeklyReportResponse>> createWeeklyReport(
            @Valid @RequestBody WeeklyReportCreateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        try {
            logger.info("Creating new weekly report: {} for user: {}", request.getTitle(), currentUser.getId());

            WeeklyReportResponse report = weeklyReportService.createWeeklyReport(request, currentUser.getId());
            logger.info("Weekly report created successfully with ID: {}", report.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Weekly report created successfully", report));

        } catch (IllegalArgumentException e) {
            logger.warn("Weekly report creation failed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            logger.warn("Weekly report creation unauthorized - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Weekly report creation error - {}", e.getMessage(), e);
            return error("Weekly report creation failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing weekly report
     * PUT /api/reports/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<WeeklyReportResponse>> updateWeeklyReport(
            @PathVariable Long id,
            @Valid @RequestBody WeeklyReportUpdateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        try {
            logger.info("Updating weekly report: {} by user: {}", id, currentUser.getId());

            WeeklyReportResponse report = weeklyReportService.updateWeeklyReport(id, request, currentUser.getId());
            logger.info("Weekly report updated successfully: {}", report.getId());

            return success("Weekly report updated successfully", report);

        } catch (IllegalArgumentException e) {
            logger.warn("Weekly report update failed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            logger.warn("Weekly report update unauthorized - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            logger.warn("Weekly report update not allowed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.error("Weekly report update error - {}", e.getMessage(), e);
            return error("Weekly report update failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get weekly report by ID
     * GET /api/reports/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<WeeklyReportResponse>> getWeeklyReport(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        try {
            logger.info("Getting weekly report: {} for user: {}", id, currentUser.getId());

            WeeklyReportResponse report = weeklyReportService.getWeeklyReport(id, currentUser.getId());
            return success("Weekly report retrieved successfully", report);

        } catch (IllegalArgumentException e) {
            logger.warn("Weekly report retrieval failed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            logger.warn("Weekly report access unauthorized - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Weekly report retrieval error - {}", e.getMessage(), e);
            return error("Weekly report retrieval failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get weekly reports with filtering and pagination
     * GET /api/reports
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<WeeklyReportListResponse>> getWeeklyReports(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long reviewerId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer weekNumber,
            @RequestParam(required = false) String reportWeekFrom,
            @RequestParam(required = false) String reportWeekTo,
            @RequestParam(required = false) String submittedFrom,
            @RequestParam(required = false) String submittedTo,
            @RequestParam(required = false) Integer priorityMin,
            @RequestParam(required = false) Integer priorityMax,
            @RequestParam(required = false) Boolean isLate,
            @RequestParam(required = false) Boolean needsReview,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "reportWeek") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        try {
            logger.info("Getting weekly reports with filters - page: {}, size: {} for user: {}", 
                    page, size, currentUser.getId());

            // Build filter request
            WeeklyReportFilterRequest filter = new WeeklyReportFilterRequest();
            filter.setTitle(title);
            filter.setStatus(status != null ? parseReportStatus(status) : null);
            filter.setAuthorId(authorId);
            filter.setReviewerId(reviewerId);
            filter.setProjectId(projectId);
            filter.setTemplateId(templateId);
            filter.setYear(year);
            filter.setWeekNumber(weekNumber);
            filter.setReportWeekFrom(reportWeekFrom != null ? parseDate(reportWeekFrom) : null);
            filter.setReportWeekTo(reportWeekTo != null ? parseDate(reportWeekTo) : null);
            filter.setSubmittedFrom(submittedFrom != null ? parseDate(submittedFrom) : null);
            filter.setSubmittedTo(submittedTo != null ? parseDate(submittedTo) : null);
            filter.setPriorityMin(priorityMin);
            filter.setPriorityMax(priorityMax);
            filter.setIsLate(isLate);
            filter.setNeedsReview(needsReview);
            filter.setSearchTerm(searchTerm);
            filter.setPage(page);
            filter.setSize(size);
            filter.setSortBy(sortBy);
            filter.setSortDirection(sortDirection);

            WeeklyReportListResponse reports = weeklyReportService.getWeeklyReports(filter, currentUser.getId());
            return success("Weekly reports retrieved successfully", reports);

        } catch (IllegalArgumentException e) {
            logger.warn("Weekly report list retrieval failed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Weekly report list retrieval error - {}", e.getMessage(), e);
            return error("Weekly report list retrieval failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Submit a weekly report
     * POST /api/reports/{id}/submit
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<WeeklyReportResponse>> submitWeeklyReport(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        try {
            logger.info("Submitting weekly report: {} by user: {}", id, currentUser.getId());

            WeeklyReportResponse report = weeklyReportService.submitWeeklyReport(id, currentUser.getId());
            logger.info("Weekly report submitted successfully: {}", report.getId());

            return success("Weekly report submitted successfully", report);

        } catch (IllegalArgumentException e) {
            logger.warn("Weekly report submission failed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            logger.warn("Weekly report submission unauthorized - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            logger.warn("Weekly report submission not allowed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.error("Weekly report submission error - {}", e.getMessage(), e);
            return error("Weekly report submission failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Review a weekly report
     * POST /api/reports/{id}/review
     */
    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('DEPARTMENT_MANAGER')")
    public ResponseEntity<ApiResponse<WeeklyReportResponse>> reviewWeeklyReport(
            @PathVariable Long id,
            @RequestParam String action,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        try {
            logger.info("Reviewing weekly report: {} with action: {} by user: {}", 
                    id, action, currentUser.getId());

            WeeklyReportResponse report = weeklyReportService.reviewWeeklyReport(
                    id, action, comment, currentUser.getId());
            logger.info("Weekly report reviewed successfully: {} with action: {}", report.getId(), action);

            return success("Weekly report reviewed successfully", report);

        } catch (IllegalArgumentException e) {
            logger.warn("Weekly report review failed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            logger.warn("Weekly report review unauthorized - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            logger.warn("Weekly report review not allowed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.error("Weekly report review error - {}", e.getMessage(), e);
            return error("Weekly report review failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete weekly report
     * DELETE /api/reports/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> deleteWeeklyReport(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        try {
            logger.info("Deleting weekly report: {} by user: {}", id, currentUser.getId());

            weeklyReportService.deleteWeeklyReport(id, currentUser.getId());
            logger.info("Weekly report deleted successfully: {}", id);

            return success("Weekly report deleted successfully", "");

        } catch (IllegalArgumentException e) {
            logger.warn("Weekly report deletion failed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            logger.warn("Weekly report deletion unauthorized - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            logger.warn("Weekly report deletion not allowed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.error("Weekly report deletion error - {}", e.getMessage(), e);
            return error("Weekly report deletion failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get weekly report statistics
     * GET /api/reports/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<Object[]>>> getWeeklyReportStatistics(
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        try {
            logger.info("Getting weekly report statistics for user: {}", currentUser.getId());

            List<Object[]> statistics = weeklyReportService.getWeeklyReportStatistics(currentUser.getId());
            return success("Weekly report statistics retrieved successfully", statistics);

        } catch (Exception e) {
            logger.error("Weekly report statistics error - {}", e.getMessage(), e);
            return error("Weekly report statistics failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper methods

    /**
     * Parse report status from string
     */
    private WeeklyReport.ReportStatus parseReportStatus(String status) {
        try {
            return WeeklyReport.ReportStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid report status: " + status);
        }
    }

    /**
     * Parse date from string (ISO format)
     */
    private java.time.LocalDate parseDate(String dateString) {
        try {
            return java.time.LocalDate.parse(dateString);
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateString + ". Use YYYY-MM-DD format.");
        }
    }
}