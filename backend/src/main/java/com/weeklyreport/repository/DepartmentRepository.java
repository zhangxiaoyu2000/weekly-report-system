package com.weeklyreport.repository;

import com.weeklyreport.entity.Department;
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
 * Repository interface for Department entity
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    // Basic finder methods
    Optional<Department> findByCode(String code);
    
    Optional<Department> findByName(String name);
    
    boolean existsByCode(String code);
    
    boolean existsByName(String name);

    // Find by status
    List<Department> findByStatus(Department.DepartmentStatus status);
    
    Page<Department> findByStatus(Department.DepartmentStatus status, Pageable pageable);

    // Hierarchical queries
    List<Department> findByParentIsNull(); // Root departments
    
    List<Department> findByParentId(Long parentId); // Direct children
    
    @Query("SELECT d FROM Department d WHERE d.parent.id = :parentId ORDER BY d.sortOrder ASC")
    List<Department> findChildrenByParentIdOrderBySortOrder(@Param("parentId") Long parentId);

    // Find by level
    List<Department> findByLevel(Integer level);
    
    @Query("SELECT d FROM Department d WHERE d.level = :level AND d.status = :status ORDER BY d.sortOrder ASC")
    List<Department> findByLevelAndStatusOrderBySortOrder(@Param("level") Integer level, 
                                                          @Param("status") Department.DepartmentStatus status);

    // Hierarchical path queries
    @Query("SELECT d FROM Department d WHERE d.path LIKE CONCAT(:parentPath, '%') AND d.id != :parentId")
    List<Department> findDescendants(@Param("parentPath") String parentPath, @Param("parentId") Long parentId);
    
    @Query("SELECT d FROM Department d WHERE d.path LIKE CONCAT(:parentPath, '%') AND d.level = :level")
    List<Department> findDescendantsByLevel(@Param("parentPath") String parentPath, @Param("level") Integer level);

    // Search methods
    @Query("SELECT d FROM Department d WHERE " +
           "(LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "d.status = :status")
    Page<Department> searchByKeywordAndStatus(@Param("keyword") String keyword, 
                                             @Param("status") Department.DepartmentStatus status, 
                                             Pageable pageable);

    @Query("SELECT d FROM Department d WHERE " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Department> searchByKeyword(@Param("keyword") String keyword);

    // Find by manager
    @Query("SELECT d FROM Department d WHERE d.managerName = :managerName")
    List<Department> findByManagerName(@Param("managerName") String managerName);
    
    @Query("SELECT d FROM Department d WHERE d.contactEmail = :email")
    Optional<Department> findByContactEmail(@Param("email") String email);

    // Statistics and counts
    @Query("SELECT COUNT(d) FROM Department d WHERE d.status = :status")
    long countByStatus(@Param("status") Department.DepartmentStatus status);
    
    @Query("SELECT COUNT(d) FROM Department d WHERE d.level = :level")
    long countByLevel(@Param("level") Integer level);
    
    @Query("SELECT COUNT(d) FROM Department d WHERE d.parent.id = :parentId")
    long countByParentId(@Param("parentId") Long parentId);

    // Level statistics
    @Query("SELECT d.level, COUNT(d) FROM Department d WHERE d.status = 'ACTIVE' GROUP BY d.level ORDER BY d.level")
    List<Object[]> countDepartmentsByLevel();
    
    @Query("SELECT MAX(d.level) FROM Department d")
    Integer getMaxLevel();

    // Find departments with/without certain relationships
    @Query("SELECT d FROM Department d WHERE NOT EXISTS (SELECT u FROM User u WHERE u.department = d)")
    List<Department> findDepartmentsWithoutUsers();
    
    @Query("SELECT d FROM Department d WHERE EXISTS (SELECT u FROM User u WHERE u.department = d AND u.status = 'ACTIVE')")
    List<Department> findDepartmentsWithActiveUsers();
    
    @Query("SELECT d FROM Department d WHERE NOT EXISTS (SELECT t FROM Template t WHERE t.department = d)")
    List<Department> findDepartmentsWithoutTemplates();

    // Organizational structure queries
    @Query("SELECT d FROM Department d WHERE d.parent IS NULL AND d.status = 'ACTIVE' ORDER BY d.sortOrder ASC")
    List<Department> findRootDepartments();
    
    @Query("SELECT d FROM Department d WHERE d.children IS EMPTY AND d.status = 'ACTIVE'")
    List<Department> findLeafDepartments();

    // Path-based queries for tree operations
    @Query("SELECT d FROM Department d WHERE d.path LIKE CONCAT(:ancestorPath, '%') ORDER BY d.level ASC, d.sortOrder ASC")
    List<Department> findSubtree(@Param("ancestorPath") String ancestorPath);
    
    @Query("SELECT d FROM Department d WHERE :childPath LIKE CONCAT(d.path, '%') ORDER BY d.level DESC")
    List<Department> findAncestors(@Param("childPath") String childPath);

    // Tree validation queries
    @Query("SELECT d FROM Department d WHERE d.path IS NULL OR d.level IS NULL")
    List<Department> findInconsistentHierarchy();
    
    @Query("SELECT d FROM Department d WHERE d.level != (LENGTH(d.path) - LENGTH(REPLACE(d.path, '/', '')))")
    List<Department> findIncorrectLevels();

    // Business logic queries
    @Query("SELECT d FROM Department d WHERE d.status = 'ACTIVE' AND " +
           "(:level IS NULL OR d.level = :level) AND " +
           "(:parentId IS NULL OR d.parent.id = :parentId) " +
           "ORDER BY d.sortOrder ASC")
    List<Department> findDepartmentsWithFilters(@Param("level") Integer level,
                                               @Param("parentId") Long parentId);

    // Update operations
    @Query("UPDATE Department d SET d.status = :newStatus WHERE d.id IN :departmentIds")
    int updateStatusBatch(@Param("departmentIds") List<Long> departmentIds, 
                         @Param("newStatus") Department.DepartmentStatus newStatus);
    
    @Query("UPDATE Department d SET d.sortOrder = :sortOrder WHERE d.id = :departmentId")
    int updateSortOrder(@Param("departmentId") Long departmentId, 
                       @Param("sortOrder") Integer sortOrder);

    // Reporting and analytics
    @Query("SELECT d.id, d.name, COUNT(u) as userCount " +
           "FROM Department d LEFT JOIN d.users u " +
           "WHERE d.status = 'ACTIVE' AND (u.status = 'ACTIVE' OR u IS NULL) " +
           "GROUP BY d.id, d.name " +
           "ORDER BY userCount DESC")
    List<Object[]> getDepartmentUserCounts();
    
    @Query("SELECT d.id, d.name, COUNT(r) as reportCount " +
           "FROM Department d " +
           "LEFT JOIN d.users u " +
           "LEFT JOIN u.weeklyReports r " +
           "WHERE d.status = 'ACTIVE' AND " +
           "(:weekStart IS NULL OR r.reportWeek = :weekStart) " +
           "GROUP BY d.id, d.name " +
           "ORDER BY reportCount DESC")
    List<Object[]> getDepartmentReportCounts(@Param("weekStart") java.time.LocalDate weekStart);

    // Navigation helpers
    @Query("SELECT d FROM Department d WHERE d.id = :departmentId")
    Optional<Department> findWithChildren(@Param("departmentId") Long departmentId);
    
    @Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.children WHERE d.parent IS NULL ORDER BY d.sortOrder")
    List<Department> findRootDepartmentsWithChildren();

    // Batch operations for tree management
    @Query("SELECT d FROM Department d WHERE d.path LIKE CONCAT(:oldPath, '%')")
    List<Department> findByPathStartsWith(@Param("oldPath") String oldPath);
}