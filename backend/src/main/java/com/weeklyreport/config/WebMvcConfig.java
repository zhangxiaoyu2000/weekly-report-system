package com.weeklyreport.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web MVC configuration to handle static resources and prevent conflicts with API endpoints
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        logger.info("配置静态资源处理器，明确排除API路径（包括/ai/**）");
        
        // 设置资源处理器的优先级为最低，让控制器优先处理
        registry.setOrder(Ordered.LOWEST_PRECEDENCE);
        
        // 明确排除所有API相关路径，防止与控制器冲突
        // 注意：context-path是/api，所以内部路径不包含/api前缀
        
        // 只为明确的静态资源路径配置处理器，不使用通配符
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
                
        registry.addResourceHandler("/public/**")
                .addResourceLocations("classpath:/public/")
                .setCachePeriod(3600);
                
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(3600);
                
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(3600);
                
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(3600);
                
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .setCachePeriod(3600);
        
        // 关键：不为/ai/**、/users/**、/weekly-reports/**等API路径添加资源处理器
        logger.info("静态资源处理器配置完成，API控制器路径（/ai/**等）已明确排除");
    }
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 配置路径匹配，确保控制器优先
        configurer.setUseTrailingSlashMatch(false);
        configurer.setUseSuffixPatternMatch(false);
        
        // 确保API路径不会被误解为静态资源
        logger.info("路径匹配配置：API路径优先于静态资源处理");
    }
}