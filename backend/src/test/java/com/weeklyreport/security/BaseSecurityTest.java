package com.weeklyreport.security;

import com.weeklyreport.entity.Department;
import com.weeklyreport.entity.User;
import com.weeklyreport.repository.DepartmentRepository;
import com.weeklyreport.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Base class for security-related integration tests
 * Provides common setup and utility methods for security testing
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseSecurityTest {
    
    @Autowired
    protected MockMvc mockMvc;
    
    @Autowired
    protected UserRepository userRepository;
    
    @Autowired
    protected DepartmentRepository departmentRepository;
    
    @Autowired
    protected PasswordEncoder passwordEncoder;
    
    protected User testAdmin;
    protected User testManager;
    protected User testEmployee;
    protected User testTeamLeader;
    protected User testHrManager;
    protected Department testDepartment;
    
    @BeforeEach
    void setUpSecurityTestData() {
        // Clean up existing test data
        userRepository.deleteAll();
        departmentRepository.deleteAll();
        
        // Create test department
        testDepartment = createTestDepartment();
        departmentRepository.save(testDepartment);
        
        // Create test users with different roles
        testAdmin = createTestUser(
            SecurityTestConfig.TestUsers.ADMIN_USERNAME,
            SecurityTestConfig.TestUsers.ADMIN_EMAIL,
            SecurityTestConfig.TestUsers.ADMIN_PASSWORD,
            "Test Admin",
            User.Role.ADMIN
        );
        
        testManager = createTestUser(
            SecurityTestConfig.TestUsers.MANAGER_USERNAME,
            SecurityTestConfig.TestUsers.MANAGER_EMAIL,
            SecurityTestConfig.TestUsers.MANAGER_PASSWORD,
            "Test Manager",
            User.Role.DEPARTMENT_MANAGER
        );
        
        testEmployee = createTestUser(
            SecurityTestConfig.TestUsers.EMPLOYEE_USERNAME,
            SecurityTestConfig.TestUsers.EMPLOYEE_EMAIL,
            SecurityTestConfig.TestUsers.EMPLOYEE_PASSWORD,
            "Test Employee",
            User.Role.EMPLOYEE
        );
        
        testTeamLeader = createTestUser(
            SecurityTestConfig.TestUsers.TEAM_LEADER_USERNAME,
            SecurityTestConfig.TestUsers.TEAM_LEADER_EMAIL,
            SecurityTestConfig.TestUsers.TEAM_LEADER_PASSWORD,
            "Test Team Leader",
            User.Role.TEAM_LEADER
        );
        
        testHrManager = createTestUser(
            SecurityTestConfig.TestUsers.HR_MANAGER_USERNAME,
            SecurityTestConfig.TestUsers.HR_MANAGER_EMAIL,
            SecurityTestConfig.TestUsers.HR_MANAGER_PASSWORD,
            "Test HR Manager",
            User.Role.HR_MANAGER
        );
        
        // Save all test users
        userRepository.save(testAdmin);
        userRepository.save(testManager);
        userRepository.save(testEmployee);
        userRepository.save(testTeamLeader);
        userRepository.save(testHrManager);
    }
    
    /**
     * Create a test user with the specified details
     */
    protected User createTestUser(String username, String email, String password, String fullName, User.Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setDepartment(testDepartment);
        user.setEmployeeId("EMP" + System.currentTimeMillis());
        user.setPosition("Test Position");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
    
    /**
     * Create a test department
     */
    protected Department createTestDepartment() {
        Department department = new Department();
        department.setName("Test Department");
        department.setDescription("Department for testing");
        department.setCode("TEST");
        return department;
    }
    
    /**
     * Get user by role for testing
     */
    protected User getUserByRole(User.Role role) {
        return switch (role) {
            case ADMIN -> testAdmin;
            case DEPARTMENT_MANAGER -> testManager;
            case EMPLOYEE -> testEmployee;
            case TEAM_LEADER -> testTeamLeader;
            case HR_MANAGER -> testHrManager;
        };
    }
    
    /**
     * Get raw password for user role (for login testing)
     */
    protected String getRawPasswordByRole(User.Role role) {
        return switch (role) {
            case ADMIN -> SecurityTestConfig.TestUsers.ADMIN_PASSWORD;
            case DEPARTMENT_MANAGER -> SecurityTestConfig.TestUsers.MANAGER_PASSWORD;
            case EMPLOYEE -> SecurityTestConfig.TestUsers.EMPLOYEE_PASSWORD;
            case TEAM_LEADER -> SecurityTestConfig.TestUsers.TEAM_LEADER_PASSWORD;
            case HR_MANAGER -> SecurityTestConfig.TestUsers.HR_MANAGER_PASSWORD;
        };
    }
}