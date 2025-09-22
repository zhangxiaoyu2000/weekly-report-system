package com.weeklyreport.util;

import com.weeklyreport.entity.User;
import com.weeklyreport.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 密码修复工具
 * 将数据库中的明文密码转换为BCrypt加密格式
 */
@Component
public class PasswordFixUtil implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PasswordFixUtil.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否需要运行密码修复
        if (args.length > 0 && "fix-passwords".equals(args[0])) {
            fixAllPasswords();
        }
    }

    public void fixAllPasswords() {
        logger.info("=== 开始修复用户密码编码 ===");
        
        try {
            // 获取所有用户
            List<User> users = userRepository.findAll();
            logger.info("找到 {} 个用户需要处理", users.size());

            int fixedCount = 0;
            for (User user : users) {
                logger.info("处理用户: {}", user.getUsername());
                logger.info("当前密码: {}", user.getPassword());

                // 检查密码是否已经是BCrypt格式
                if (user.getPassword() != null && user.getPassword().startsWith("$2")) {
                    logger.info("✅ 密码已经加密，跳过");
                    continue;
                }

                // 假设当前密码为明文，进行加密
                String originalPassword = user.getPassword();
                if (originalPassword == null || originalPassword.trim().isEmpty()) {
                    // 如果密码为空，设置一个默认密码
                    originalPassword = user.getUsername(); // 使用用户名作为默认密码
                    logger.info("密码为空，使用用户名作为默认密码");
                }

                // 加密密码
                String encodedPassword = passwordEncoder.encode(originalPassword);
                logger.info("新的加密密码: {}", encodedPassword);

                // 更新用户密码
                user.setPassword(encodedPassword);
                userRepository.save(user);

                fixedCount++;
                logger.info("✅ 用户 {} 的密码已更新", user.getUsername());
            }

            logger.info("=== 密码修复完成，共修复 {} 个用户 ===", fixedCount);

        } catch (Exception e) {
            logger.error("❌ 密码修复过程中发生错误", e);
            throw e;
        }
    }

    /**
     * 手动触发密码修复的方法
     */
    public void fixPasswordsManually() {
        fixAllPasswords();
    }
}