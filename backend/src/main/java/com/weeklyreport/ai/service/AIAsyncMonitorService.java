package com.weeklyreport.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

/**
 * AI异步处理监控服务
 * 监控线程池状态，记录性能指标
 */
@Service
public class AIAsyncMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(AIAsyncMonitorService.class);

    @Autowired
    @Qualifier("aiAnalysisExecutor")
    private Executor aiAnalysisExecutor;

    /**
     * 每分钟监控一次线程池状态
     */
    @Scheduled(fixedRate = 60000) // 60秒
    public void monitorThreadPoolStatus() {
        if (aiAnalysisExecutor instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) aiAnalysisExecutor;
            
            int activeCount = executor.getActiveCount();
            int poolSize = executor.getPoolSize();
            int corePoolSize = executor.getCorePoolSize();
            int maxPoolSize = executor.getMaxPoolSize();
            int queueSize = executor.getThreadPoolExecutor().getQueue().size();
            long completedTaskCount = executor.getThreadPoolExecutor().getCompletedTaskCount();
            long taskCount = executor.getThreadPoolExecutor().getTaskCount();
            
            // 只在有活动任务或队列不为空时记录
            if (activeCount > 0 || queueSize > 0) {
                logger.info("📊 AI线程池状态监控:");
                logger.info("  🔄 活跃线程: {}/{} (最大: {})", activeCount, poolSize, maxPoolSize);
                logger.info("  📋 队列任务: {} 个", queueSize);
                logger.info("  ✅ 已完成: {} / 总任务: {}", completedTaskCount, taskCount);
                
                // 检查是否接近容量限制
                if (queueSize > 150) { // 75% of 200
                    logger.warn("⚠️ AI分析队列接近满载 ({}%), 当前: {} / 200", 
                               (queueSize * 100 / 200), queueSize);
                }
                
                if (activeCount >= maxPoolSize) {
                    logger.warn("⚠️ AI分析线程池已满载，所有 {} 个线程都在工作", maxPoolSize);
                }
            }
        }
    }

    /**
     * 获取当前线程池状态快照
     */
    public ThreadPoolStatus getThreadPoolStatus() {
        if (aiAnalysisExecutor instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) aiAnalysisExecutor;
            
            return new ThreadPoolStatus(
                executor.getActiveCount(),
                executor.getPoolSize(),
                executor.getCorePoolSize(),
                executor.getMaxPoolSize(),
                executor.getThreadPoolExecutor().getQueue().size(),
                executor.getThreadPoolExecutor().getCompletedTaskCount(),
                executor.getThreadPoolExecutor().getTaskCount()
            );
        }
        return null;
    }

    /**
     * 线程池状态数据类
     */
    public static class ThreadPoolStatus {
        private final int activeCount;
        private final int poolSize;
        private final int corePoolSize;
        private final int maxPoolSize;
        private final int queueSize;
        private final long completedTaskCount;
        private final long taskCount;

        public ThreadPoolStatus(int activeCount, int poolSize, int corePoolSize, 
                               int maxPoolSize, int queueSize, long completedTaskCount, long taskCount) {
            this.activeCount = activeCount;
            this.poolSize = poolSize;
            this.corePoolSize = corePoolSize;
            this.maxPoolSize = maxPoolSize;
            this.queueSize = queueSize;
            this.completedTaskCount = completedTaskCount;
            this.taskCount = taskCount;
        }

        // Getters
        public int getActiveCount() { return activeCount; }
        public int getPoolSize() { return poolSize; }
        public int getCorePoolSize() { return corePoolSize; }
        public int getMaxPoolSize() { return maxPoolSize; }
        public int getQueueSize() { return queueSize; }
        public long getCompletedTaskCount() { return completedTaskCount; }
        public long getTaskCount() { return taskCount; }

        public double getUtilizationRate() {
            return maxPoolSize > 0 ? (double) activeCount / maxPoolSize * 100 : 0;
        }

        public double getQueueUtilizationRate() {
            return 200 > 0 ? (double) queueSize / 200 * 100 : 0; // 队列容量200
        }

        @Override
        public String toString() {
            return String.format("ThreadPoolStatus{active: %d/%d, queue: %d, completed: %d, utilization: %.1f%%}", 
                               activeCount, maxPoolSize, queueSize, completedTaskCount, getUtilizationRate());
        }
    }
}