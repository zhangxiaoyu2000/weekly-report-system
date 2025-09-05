package com.weeklyreport.service;

import com.weeklyreport.dto.weeklyreport.*;
import com.weeklyreport.entity.*;
import com.weeklyreport.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Weekly Report operations
 */
@Service
@Transactional
public class WeeklyReportService {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyReportService.class);

    @Autowired
    private WeeklyReportRepository weeklyReportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TemplateRepository templateRepository;

    /**
     * Create a new weekly report
     */
    public WeeklyReportResponse createWeeklyReport(WeeklyReportCreateRequest request, Long authorId) {
        logger.info("Creating new weekly report: {} for user: {}", request.getTitle(), authorId);

        // Validate author exists
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        // Check for duplicate report for the same week and author
        if (weeklyReportRepository.existsByAuthorAndReportWeek(author, request.getReportWeek())) {
            throw new IllegalArgumentException("Weekly report already exists for this week");
        }

        // Validate template if provided
        Template template = null;
        if (request.getTemplateId() != null) {
            template = templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new IllegalArgumentException("Template not found"));
        }

        // Validate project if provided
        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Project not found"));
            
            // Check if user has access to the project
            if (!canUserAccessProject(project, authorId)) {
                throw new SecurityException("User does not have access to this project");
            }
        }

        // Create weekly report entity
        WeeklyReport report = new WeeklyReport();
        report.setTitle(request.getTitle());
        report.setReportWeek(request.getReportWeek());
        report.setContent(request.getContent());
        report.setWorkSummary(request.getWorkSummary());
        report.setAchievements(request.getAchievements());
        report.setChallenges(request.getChallenges());
        report.setNextWeekPlan(request.getNextWeekPlan());
        report.setAdditionalNotes(request.getAdditionalNotes());
        report.setPriority(request.getPriority() != null ? request.getPriority() : 5);
        report.setAuthor(author);
        report.setTemplate(template);
        report.setProject(project);

        // Check if report is late
        LocalDate deadline = request.getReportWeek().plusDays(7);
        report.setIsLate(LocalDate.now().isAfter(deadline));

        // Save report
        WeeklyReport savedReport = weeklyReportRepository.save(report);
        logger.info("Weekly report created successfully with ID: {}", savedReport.getId());

        return new WeeklyReportResponse(savedReport);
    }

    /**
     * Update an existing weekly report
     */
    public WeeklyReportResponse updateWeeklyReport(Long reportId, WeeklyReportUpdateRequest request, Long userId) {
        logger.info("Updating weekly report: {} by user: {}", reportId, userId);

        // Find existing report
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Weekly report not found"));

        // Check if user can update this report
        if (!canUserModifyReport(report, userId)) {
            throw new SecurityException("User not authorized to update this report");
        }

        // Check if report can be updated based on status
        if (!canReportBeUpdated(report)) {
            throw new IllegalStateException("Report cannot be updated in current status: " + report.getStatus());
        }

        // Validate template if provided
        Template template = null;
        if (request.getTemplateId() != null) {
            template = templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new IllegalArgumentException("Template not found"));
        }

        // Validate project if provided
        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Project not found"));

            if (!canUserAccessProject(project, userId)) {
                throw new SecurityException("User does not have access to this project");
            }
        }

        // Update report fields
        report.setTitle(request.getTitle());
        report.setContent(request.getContent());
        report.setWorkSummary(request.getWorkSummary());
        report.setAchievements(request.getAchievements());
        report.setChallenges(request.getChallenges());
        report.setNextWeekPlan(request.getNextWeekPlan());
        report.setAdditionalNotes(request.getAdditionalNotes());
        if (request.getPriority() != null) {
            report.setPriority(request.getPriority());
        }
        report.setTemplate(template);
        report.setProject(project);

        // Save updated report
        WeeklyReport savedReport = weeklyReportRepository.save(report);
        logger.info("Weekly report updated successfully: {}", savedReport.getId());

        return new WeeklyReportResponse(savedReport);
    }

    /**
     * Get weekly report by ID
     */
    @Transactional(readOnly = true)
    public WeeklyReportResponse getWeeklyReport(Long reportId, Long userId) {
        logger.info("Getting weekly report: {} for user: {}", reportId, userId);

        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Weekly report not found"));

        // Check if user can view this report
        if (!canUserViewReport(report, userId)) {
            throw new SecurityException("User not authorized to view this report");
        }

        return new WeeklyReportResponse(report);
    }

    /**
     * Get weekly reports with filtering and pagination
     */
    @Transactional(readOnly = true)
    public WeeklyReportListResponse getWeeklyReports(WeeklyReportFilterRequest filter, Long userId) {
        logger.info("Getting weekly reports with filter for user: {}", userId);

        // Validate filter parameters
        validateFilterRequest(filter);

        // Create pageable
        Sort sort = createSort(filter.getSortBy(), filter.getSortDirection());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // Get user for permission checks
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Build query based on filters and user permissions
        Page<WeeklyReport> reportPage;
        if (filter.hasFilters()) {
            reportPage = weeklyReportRepository.findWithFilters(
                    filter.getTitle(),
                    filter.getStatus(),
                    filter.getAuthorId(),
                    filter.getReviewerId(),
                    filter.getProjectId(),
                    filter.getTemplateId(),
                    filter.getYear(),
                    filter.getWeekNumber(),
                    filter.getReportWeekFrom(),
                    filter.getReportWeekTo(),
                    filter.getSubmittedFrom(),
                    filter.getSubmittedTo(),
                    filter.getPriorityMin(),
                    filter.getPriorityMax(),
                    filter.getIsLate(),
                    filter.getSearchTerm(),
                    pageable
            );
        } else {
            // Default: show user's own reports or reports they can review
            reportPage = weeklyReportRepository.findAccessibleReports(user, pageable);
        }

        // Convert to response DTOs and apply additional permission filtering
        List<WeeklyReportResponse> reportResponses = reportPage.getContent()
                .stream()
                .filter(report -> canUserViewReport(report, userId))
                .map(WeeklyReportResponse::new)
                .collect(Collectors.toList());

        WeeklyReportListResponse response = new WeeklyReportListResponse(reportResponses, reportPage);
        response.calculateStatistics();
        
        return response;
    }

    /**
     * Submit a weekly report
     */
    public WeeklyReportResponse submitWeeklyReport(Long reportId, Long userId) {
        logger.info("Submitting weekly report: {} by user: {}", reportId, userId);

        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Weekly report not found"));

        if (!canUserModifyReport(report, userId)) {
            throw new SecurityException("User not authorized to submit this report");
        }

        if (report.getStatus() != WeeklyReport.ReportStatus.DRAFT) {
            throw new IllegalStateException("Only draft reports can be submitted");
        }

        // Validate that the report has required content
        if (report.getContent() == null || report.getContent().trim().isEmpty()) {
            throw new IllegalStateException("Report must have content before submission");
        }

        report.submit();
        WeeklyReport savedReport = weeklyReportRepository.save(report);
        logger.info("Weekly report submitted successfully: {}", savedReport.getId());

        return new WeeklyReportResponse(savedReport);
    }

    /**
     * Review a weekly report (approve, reject, or request revision)
     */
    public WeeklyReportResponse reviewWeeklyReport(Long reportId, String action, String comment, Long reviewerId) {
        logger.info("Reviewing weekly report: {} with action: {} by reviewer: {}", reportId, action, reviewerId);

        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Weekly report not found"));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));

        // Check if user can review reports
        if (!canUserReviewReports(reviewer)) {
            throw new SecurityException("User not authorized to review reports");
        }

        if (report.getStatus() != WeeklyReport.ReportStatus.SUBMITTED &&
            report.getStatus() != WeeklyReport.ReportStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Only submitted or under-review reports can be reviewed");
        }

        switch (action.toLowerCase()) {
            case "approve" -> report.approve(reviewer, comment);
            case "reject" -> report.reject(reviewer, comment);
            case "request_revision" -> report.requestRevision(reviewer, comment);
            default -> throw new IllegalArgumentException("Invalid review action: " + action);
        }

        WeeklyReport savedReport = weeklyReportRepository.save(report);
        logger.info("Weekly report reviewed successfully: {} with action: {}", savedReport.getId(), action);

        return new WeeklyReportResponse(savedReport);
    }

    /**
     * Delete weekly report (soft delete)
     */
    public void deleteWeeklyReport(Long reportId, Long userId) {
        logger.info("Deleting weekly report: {} by user: {}", reportId, userId);

        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Weekly report not found"));

        if (!canUserModifyReport(report, userId)) {
            throw new SecurityException("User not authorized to delete this report");
        }

        if (report.getStatus() != WeeklyReport.ReportStatus.DRAFT) {
            throw new IllegalStateException("Only draft reports can be deleted");
        }

        weeklyReportRepository.delete(report);
        logger.info("Weekly report deleted successfully: {}", reportId);
    }

    /**
     * Get weekly reports statistics
     */
    @Transactional(readOnly = true)
    public List<Object[]> getWeeklyReportStatistics(Long userId) {
        logger.info("Getting weekly report statistics for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // If user is admin or manager, get global statistics, otherwise user-specific
        if (canUserViewGlobalStats(user)) {
            return weeklyReportRepository.getGlobalReportStatsByStatus();
        } else {
            return weeklyReportRepository.getUserReportStatsByStatus(user);
        }
    }

    // Helper methods

    private boolean canUserModifyReport(WeeklyReport report, Long userId) {
        // User can modify if they are the author and report is in modifiable state
        return report.getAuthor().getId().equals(userId) && canReportBeUpdated(report);
    }

    private boolean canUserViewReport(WeeklyReport report, Long userId) {
        // User can view if:
        // 1. They are the author
        // 2. They are a reviewer/manager
        // 3. Report is approved and public (for team members)
        if (report.getAuthor().getId().equals(userId)) {
            return true;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user != null && canUserReviewReports(user)) {
            return true;
        }

        // Additional logic for project-based access could be added here
        return false;
    }

    private boolean canUserAccessProject(Project project, Long userId) {
        // User can access project if they are creator, member, or admin
        if (project.getCreatedBy().getId().equals(userId)) {
            return true;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user != null && (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.HR_MANAGER)) {
            return true;
        }

        // Check project membership (would need ProjectMember entity)
        // return projectMemberRepository.existsByProjectAndUserAndStatus(project, user, "ACTIVE");
        return true; // Simplified for now
    }

    private boolean canUserReviewReports(User user) {
        return user.getRole() == User.Role.ADMIN || 
               user.getRole() == User.Role.HR_MANAGER || 
               user.getRole() == User.Role.DEPARTMENT_MANAGER;
    }

    private boolean canUserViewGlobalStats(User user) {
        return user.getRole() == User.Role.ADMIN || 
               user.getRole() == User.Role.HR_MANAGER;
    }

    private boolean canReportBeUpdated(WeeklyReport report) {
        return report.getStatus() == WeeklyReport.ReportStatus.DRAFT ||
               report.getStatus() == WeeklyReport.ReportStatus.REVISION_REQUESTED;
    }

    private void validateFilterRequest(WeeklyReportFilterRequest filter) {
        if (!filter.isValidSortDirection()) {
            throw new IllegalArgumentException("Invalid sort direction. Use ASC or DESC");
        }

        if (filter.getReportWeekFrom() != null && filter.getReportWeekTo() != null) {
            if (filter.getReportWeekFrom().isAfter(filter.getReportWeekTo())) {
                throw new IllegalArgumentException("Report week 'from' date cannot be after 'to' date");
            }
        }
    }

    private Sort createSort(String sortBy, String sortDirection) {
        // Validate sortBy field
        String[] validSortFields = {"id", "title", "reportWeek", "year", "weekNumber", 
                                   "status", "priority", "createdAt", "updatedAt", "submittedAt"};
        boolean isValidField = false;
        for (String field : validSortFields) {
            if (field.equals(sortBy)) {
                isValidField = true;
                break;
            }
        }

        if (!isValidField) {
            sortBy = "reportWeek"; // default
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? 
                Sort.Direction.ASC : Sort.Direction.DESC;

        return Sort.by(direction, sortBy);
    }
}