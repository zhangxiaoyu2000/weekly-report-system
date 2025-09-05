package com.weeklyreport.repository;

import com.weeklyreport.entity.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Template entity
 */
@Repository
public interface TemplateRepository extends JpaRepository<Template, Long>, JpaSpecificationExecutor<Template> {

    // Basic finder methods
    Optional<Template> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Template> findByNameContainingIgnoreCase(String name);

    // Find by status
    List<Template> findByStatus(Template.TemplateStatus status);
    
    Page<Template> findByStatus(Template.TemplateStatus status, Pageable pageable);
    
    @Query("SELECT t FROM Template t WHERE t.status = :status ORDER BY t.sortOrder ASC")
    List<Template> findByStatusOrderBySortOrder(@Param("status") Template.TemplateStatus status);

    // Find by type
    List<Template> findByType(Template.TemplateType type);
    
    @Query("SELECT t FROM Template t WHERE t.type = :type AND t.status = :status ORDER BY t.sortOrder ASC")
    List<Template> findByTypeAndStatusOrderBySortOrder(@Param("type") Template.TemplateType type, 
                                                       @Param("status") Template.TemplateStatus status);

    // Find by department
    List<Template> findByDepartmentId(Long departmentId);
    
    @Query("SELECT t FROM Template t WHERE t.department.id = :departmentId AND t.status = :status ORDER BY t.sortOrder ASC")
    List<Template> findByDepartmentIdAndStatus(@Param("departmentId") Long departmentId, 
                                              @Param("status") Template.TemplateStatus status);

    // Find by creator
    List<Template> findByCreatedById(Long createdById);
    
    @Query("SELECT t FROM Template t WHERE t.createdBy.id = :createdById AND t.status = :status")
    List<Template> findByCreatedByIdAndStatus(@Param("createdById") Long createdById, 
                                             @Param("status") Template.TemplateStatus status);

    // Find default templates
    @Query("SELECT t FROM Template t WHERE t.isDefault = true AND t.status = 'ACTIVE'")
    List<Template> findDefaultTemplates();
    
    @Query("SELECT t FROM Template t WHERE t.isDefault = true AND t.type = :type AND t.status = 'ACTIVE'")
    List<Template> findDefaultTemplatesByType(@Param("type") Template.TemplateType type);
    
    @Query("SELECT t FROM Template t WHERE t.isDefault = true AND t.department.id = :departmentId AND t.status = 'ACTIVE'")
    Optional<Template> findDefaultTemplateByDepartment(@Param("departmentId") Long departmentId);

    // Find system templates (global templates)
    @Query("SELECT t FROM Template t WHERE t.type = 'SYSTEM' AND t.status = 'ACTIVE' ORDER BY t.sortOrder ASC")
    List<Template> findSystemTemplates();

    // Find available templates for user/department
    @Query("SELECT t FROM Template t WHERE " +
           "(t.type = 'SYSTEM' OR " +
           "(t.type = 'DEPARTMENT' AND t.department.id = :departmentId) OR " +
           "(t.type = 'PERSONAL' AND t.createdBy.id = :userId)) AND " +
           "t.status = 'ACTIVE' " +
           "ORDER BY t.type ASC, t.sortOrder ASC")
    List<Template> findAvailableTemplatesForUser(@Param("userId") Long userId, 
                                                 @Param("departmentId") Long departmentId);

    // Find by version
    List<Template> findByVersion(Integer version);
    
    @Query("SELECT t FROM Template t WHERE t.name = :templateName ORDER BY t.version DESC")
    List<Template> findVersionsByName(@Param("templateName") String templateName);
    
    @Query("SELECT t FROM Template t WHERE t.name = :templateName AND t.version = (SELECT MAX(t2.version) FROM Template t2 WHERE t2.name = :templateName)")
    Optional<Template> findLatestVersionByName(@Param("templateName") String templateName);

    // Search methods
    @Query("SELECT t FROM Template t WHERE " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "t.status = :status")
    Page<Template> searchByKeywordAndStatus(@Param("keyword") String keyword, 
                                           @Param("status") Template.TemplateStatus status, 
                                           Pageable pageable);

    // Statistics and counts
    @Query("SELECT COUNT(t) FROM Template t WHERE t.status = :status")
    long countByStatus(@Param("status") Template.TemplateStatus status);
    
    @Query("SELECT COUNT(t) FROM Template t WHERE t.type = :type")
    long countByType(@Param("type") Template.TemplateType type);
    
    @Query("SELECT COUNT(t) FROM Template t WHERE t.department.id = :departmentId")
    long countByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT COUNT(t) FROM Template t WHERE t.createdBy.id = :createdById")
    long countByCreatedById(@Param("createdById") Long createdById);

    // Usage statistics
    List<Template> findByUsageCountGreaterThan(Integer usageCount);
    
    @Query("SELECT t FROM Template t ORDER BY t.usageCount DESC")
    List<Template> findMostUsedTemplates();
    
    @Query("SELECT t FROM Template t WHERE t.usageCount = 0 AND t.status = 'ACTIVE'")
    List<Template> findUnusedActiveTemplates();
    
    @Query("SELECT AVG(t.usageCount) FROM Template t WHERE t.status = 'ACTIVE'")
    Double getAverageUsageCount();

    // Template analytics
    @Query("SELECT t.type, COUNT(t) FROM Template t WHERE t.status = 'ACTIVE' GROUP BY t.type")
    List<Object[]> countActiveTemplatesByType();
    
    @Query("SELECT t.department.id, t.department.name, COUNT(t) FROM Template t WHERE t.department IS NOT NULL AND t.status = 'ACTIVE' GROUP BY t.department.id, t.department.name ORDER BY COUNT(t) DESC")
    List<Object[]> countTemplatesByDepartment();
    
    @Query("SELECT t.createdBy.id, t.createdBy.fullName, COUNT(t) FROM Template t WHERE t.type = 'PERSONAL' GROUP BY t.createdBy.id, t.createdBy.fullName ORDER BY COUNT(t) DESC")
    List<Object[]> countPersonalTemplatesByCreator();

    // Required templates
    @Query("SELECT t FROM Template t WHERE t.isRequired = true AND t.status = 'ACTIVE'")
    List<Template> findRequiredTemplates();
    
    @Query("SELECT t FROM Template t WHERE t.isRequired = true AND t.department.id = :departmentId AND t.status = 'ACTIVE'")
    List<Template> findRequiredTemplatesByDepartment(@Param("departmentId") Long departmentId);

    // Template validation
    @Query("SELECT t FROM Template t WHERE t.content IS NULL OR t.content = ''")
    List<Template> findTemplatesWithEmptyContent();
    
    @Query("SELECT t FROM Template t WHERE t.type = 'DEPARTMENT' AND t.department IS NULL")
    List<Template> findDepartmentTemplatesWithoutDepartment();
    
    @Query("SELECT t FROM Template t WHERE t.type = 'PERSONAL' AND t.createdBy IS NULL")
    List<Template> findPersonalTemplatesWithoutCreator();

    // Duplicate detection
    @Query("SELECT t1 FROM Template t1 WHERE EXISTS (SELECT t2 FROM Template t2 WHERE t2.id != t1.id AND t2.name = t1.name AND t2.department = t1.department AND t2.type = t1.type)")
    List<Template> findDuplicateTemplates();

    // Complex filtering
    @Query("SELECT t FROM Template t WHERE " +
           "(:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:departmentId IS NULL OR t.department.id = :departmentId) AND " +
           "(:createdById IS NULL OR t.createdBy.id = :createdById) " +
           "ORDER BY t.type ASC, t.sortOrder ASC")
    Page<Template> findTemplatesWithFilters(@Param("name") String name,
                                           @Param("type") Template.TemplateType type,
                                           @Param("status") Template.TemplateStatus status,
                                           @Param("departmentId") Long departmentId,
                                           @Param("createdById") Long createdById,
                                           Pageable pageable);

    // Update operations
    @Query("UPDATE Template t SET t.status = :newStatus WHERE t.id IN :templateIds")
    int updateStatusBatch(@Param("templateIds") List<Long> templateIds, 
                         @Param("newStatus") Template.TemplateStatus newStatus);
    
    @Query("UPDATE Template t SET t.usageCount = t.usageCount + 1 WHERE t.id = :templateId")
    int incrementUsageCount(@Param("templateId") Long templateId);
    
    @Query("UPDATE Template t SET t.isDefault = false WHERE t.department.id = :departmentId AND t.type = 'DEPARTMENT'")
    int unsetDepartmentDefaults(@Param("departmentId") Long departmentId);
    
    @Query("UPDATE Template t SET t.isDefault = false WHERE t.type = 'SYSTEM'")
    int unsetSystemDefaults();

    // Sort order management
    @Query("SELECT MAX(t.sortOrder) FROM Template t WHERE t.type = :type AND (:departmentId IS NULL OR t.department.id = :departmentId)")
    Integer getMaxSortOrder(@Param("type") Template.TemplateType type, 
                           @Param("departmentId") Long departmentId);
    
    @Query("UPDATE Template t SET t.sortOrder = :sortOrder WHERE t.id = :templateId")
    int updateSortOrder(@Param("templateId") Long templateId, 
                       @Param("sortOrder") Integer sortOrder);

    // Template relationships
    @Query("SELECT DISTINCT t FROM Template t LEFT JOIN FETCH t.weeklyReports WHERE t.id = :templateId")
    Optional<Template> findWithReports(@Param("templateId") Long templateId);
    
    @Query("SELECT t FROM Template t WHERE NOT EXISTS (SELECT r FROM WeeklyReport r WHERE r.template = t)")
    List<Template> findTemplatesWithoutReports();
}