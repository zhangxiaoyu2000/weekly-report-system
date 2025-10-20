package com.weeklyreport.core.config;

import com.weeklyreport.user.entity.User;
import com.weeklyreport.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


/**
 * Data Initializer for creating default admin users
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        fixExistingPasswords();
        createSuperAdminUser();
        createAdminUsers();
        createManagerUsers();
        // 强制重置测试用户密码
        forceResetTestPasswords();
    }

    private void createSuperAdminUser() {
        // 创建默认超级管理员
        createSuperAdmin("superadmin", "superadmin@weeklyreport.com", "SuperAdmin123@");
        // 创建zhangxiaoyu超级管理员账户
        createSuperAdmin("zhangxiaoyu", "zhangxiaoyu@weeklyreport.com", "SuperAdmin123@");
    }
    
    private void createSuperAdmin(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            logger.info("Super admin user already exists: {}", username);
            return;
        }

        User superAdmin = new User();
        superAdmin.setUsername(username);
        superAdmin.setEmail(email);
        superAdmin.setPassword(passwordEncoder.encode(password));
        superAdmin.setRole(User.Role.SUPER_ADMIN);
        superAdmin.setStatus(User.UserStatus.ACTIVE);

        userRepository.save(superAdmin);
        logger.info("Created super admin user: {}", username);
    }

    private void createAdminUsers() {
        // 创建管理员账户
        createAdminUser("admin", "admin@weeklyreport.com", "admin123");
        createAdminUser("admin1", "admin1@weeklyreport.com", "Admin123@");
        createAdminUser("admin2", "admin2@weeklyreport.com", "Admin123@");
    }

    private void createAdminUser(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            logger.info("Admin user already exists: {}", username);
            return;
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(User.Role.ADMIN);
        admin.setStatus(User.UserStatus.ACTIVE);

        userRepository.save(admin);
        logger.info("Created admin user: {}", username);
    }

    private void createManagerUsers() {
        // 创建一个主管账户用于测试
        createManagerUser("manager1", "manager1@weeklyreport.com", "Manager123@");
    }

    private void createManagerUser(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            logger.info("Manager user already exists: {}", username);
            return;
        }

        User manager = new User();
        manager.setUsername(username);
        manager.setEmail(email);
        manager.setPassword(passwordEncoder.encode(password));
        manager.setRole(User.Role.MANAGER);
        manager.setStatus(User.UserStatus.ACTIVE);

        userRepository.save(manager);
        logger.info("Created manager user: {}", username);
    }

    /**
     * 修复现有用户的密码编码问题
     */
    private void fixExistingPasswords() {
        logger.info("=== 开始修复现有用户密码编码 ===");
        
        try {
            // 获取所有用户
            var users = userRepository.findAll();
            logger.info("检查 {} 个用户的密码编码", users.size());

            int fixedCount = 0;
            for (User user : users) {
                // 检查密码是否已经是BCrypt格式
                if (user.getPassword() != null && user.getPassword().startsWith("$2")) {
                    logger.debug("用户 {} 的密码已经加密，跳过", user.getUsername());
                    continue;
                }

                String originalPassword = user.getPassword();
                if (originalPassword == null || originalPassword.trim().isEmpty()) {
                    // 如果密码为空，设置默认密码为用户名
                    originalPassword = user.getUsername();
                    logger.info("用户 {} 密码为空，设置默认密码为用户名", user.getUsername());
                } else {
                    logger.info("修复用户 {} 的明文密码: {}", user.getUsername(), originalPassword);
                }

                // 加密密码
                String encodedPassword = passwordEncoder.encode(originalPassword);
                user.setPassword(encodedPassword);
                userRepository.save(user);

                fixedCount++;
                logger.info("✅ 用户 {} 的密码已修复", user.getUsername());
            }

            if (fixedCount > 0) {
                logger.info("=== 密码修复完成，共修复 {} 个用户 ===", fixedCount);
            } else {
                logger.info("=== 所有用户密码都已正确编码 ===");
            }

        } catch (Exception e) {
            logger.error("❌ 密码修复过程中发生错误", e);
        }
    }
    
    /**
     * 强制重置测试用户密码 - 用于调试登录问题
     */
    private void forceResetTestPasswords() {
        logger.info("=== 强制重置测试用户密码 ===");
        
        try {
            // 重置superadmin密码
            var superAdminOpt = userRepository.findByUsername("superadmin");
            if (superAdminOpt.isPresent()) {
                User superAdmin = superAdminOpt.get();
                superAdmin.setPassword(passwordEncoder.encode("SuperAdmin123@"));
                userRepository.save(superAdmin);
                logger.info("✅ 重置superadmin密码成功");
            }
            
            // 重置manager1密码
            var managerOpt = userRepository.findByUsername("manager1");
            if (managerOpt.isPresent()) {
                User manager = managerOpt.get();
                manager.setPassword(passwordEncoder.encode("Manager123@"));
                userRepository.save(manager);
                logger.info("✅ 重置manager1密码成功");
            }
            
        } catch (Exception e) {
            logger.error("❌ 强制重置密码失败", e);
        }
        
        logger.info("=== 强制重置测试用户密码完成 ===");
    }
}