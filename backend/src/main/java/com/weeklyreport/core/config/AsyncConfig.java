package com.weeklyreport.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步执行配置
 * 为AI分析回调等异步操作提供专用线程池
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);
    
    /**
     * 配置默认的异步执行器
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        logger.info("创建异步线程池执行器");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(5);
        
        // 最大线程数
        executor.setMaxPoolSize(10);
        
        // 队列容量
        executor.setQueueCapacity(100);
        
        // 线程名前缀
        executor.setThreadNamePrefix("ai-task-");
        
        // 拒绝策略：由调用者线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(60);
        
        // 是否允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);
        
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);
        
        // 初始化线程池
        executor.initialize();
        
        logger.info("异步线程池配置完成 - 核心线程数: {}, 最大线程数: {}, 队列容量: {}", 
                   executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
    
    /**
     * AI分析专用线程池 - 优化配置以支持更多并发
     */
    @Bean(name = "aiAnalysisExecutor")
    public Executor aiAnalysisExecutor() {
        logger.info("🔧 创建AI分析专用线程池");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 优化线程池配置以支持更多并发
        executor.setCorePoolSize(5);        // 提高核心线程数
        executor.setMaxPoolSize(15);        // 提高最大线程数
        executor.setQueueCapacity(200);     // 增加队列容量
        executor.setThreadNamePrefix("ai-analysis-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(300);  // 延长线程存活时间
        executor.setAllowCoreThreadTimeOut(false); // 核心线程保持活跃
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(180); // 增加关闭等待时间
        
        executor.initialize();
        
        logger.info("🚀 AI分析线程池配置完成 - 核心线程数: {}, 最大线程数: {}, 队列容量: {}", 
                   executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
}