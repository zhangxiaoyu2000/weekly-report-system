package com.weeklyreport.service.ai;

import com.weeklyreport.entity.AIAnalysisResult;
import com.weeklyreport.entity.Project;
import com.weeklyreport.repository.AIAnalysisResultRepository;
import com.weeklyreport.repository.ProjectRepository;
import com.weeklyreport.service.ai.dto.AIAnalysisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI分析回调专用事务服务
 * 解决@Async和@Transactional在同一类中的冲突问题
 */
@Service
public class AICallbackTransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(AICallbackTransactionService.class);
    
    @Autowired
    private AIAnalysisResultRepository aiAnalysisResultRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    /**
     * 在新事务中处理AI分析成功的情况
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAIAnalysisSuccess(Long projectId, AIAnalysisResponse response) {
        try {
            logger.info("开始在新事务中处理AI分析成功结果，项目ID: {}", projectId);
            
            // 计算置信度
            double confidence = calculateConfidenceFromResult(response.getResult());
            
            // 保存AI分析结果（带动态置信度）
            AIAnalysisResult analysisResultEntity = saveProjectAnalysisResult(projectId, response, confidence);
            
            // 根据置信度更新项目状态 (>= 0.7 通过)
            boolean isApproved = confidence >= 0.7;
            Project.ApprovalStatus newStatus = isApproved ? 
                Project.ApprovalStatus.ADMIN_REVIEWING :  // AI通过后直接进入管理员审核阶段
                Project.ApprovalStatus.AI_REJECTED;
            
            logger.info("=== AI结果判断 ===\n项目ID: {}\n置信度: {}\n是否通过: {}\n新状态: {}", 
                       projectId, confidence, isApproved, newStatus);
                
            updateProjectAnalysisStatus(projectId, newStatus, analysisResultEntity.getId(), null);
            logger.info("=== AI分析成功处理完成 ===\n项目ID: {}\n最终状态: {}\nAI结果ID: {}", projectId, newStatus, analysisResultEntity.getId());
            
        } catch (Exception e) {
            logger.error("处理AI分析成功结果时发生错误，项目ID: {}", projectId, e);
            throw e; // 重新抛出异常，让事务回滚
        }
    }
    
    /**
     * 在新事务中处理AI分析失败的情况
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAIAnalysisFailure(Long projectId, String errorMessage) {
        try {
            logger.info("开始在新事务中处理AI分析失败结果，项目ID: {}", projectId);
            updateProjectAnalysisStatus(projectId, Project.ApprovalStatus.AI_REJECTED, null, errorMessage);
            logger.info("AI分析失败结果处理完成，项目ID: {}", projectId);
            
        } catch (Exception e) {
            logger.error("处理AI分析失败结果时发生错误，项目ID: {}", projectId, e);
            throw e; // 重新抛出异常，让事务回滚
        }
    }
    
    /**
     * 保存项目AI分析结果到数据库
     */
    private AIAnalysisResult saveProjectAnalysisResult(Long projectId, AIAnalysisResponse response, double confidence) {
        try {
            logger.info("开始保存AI分析结果，项目ID: {}, 置信度: {}", projectId, confidence);
            
            AIAnalysisResult analysisResult = new AIAnalysisResult();
            
            // 设置基本信息 - 对于项目分析，使用PROJECT实体类型
            analysisResult.setReportId(projectId);
            analysisResult.setEntityType(AIAnalysisResult.EntityType.PROJECT);
            analysisResult.setAnalysisType(AIAnalysisResult.AnalysisType.RISK_ASSESSMENT);
            analysisResult.setResult(response.getResult());
            analysisResult.setStatus(AIAnalysisResult.AnalysisStatus.COMPLETED);
            
            // 设置动态置信度和模型信息
            analysisResult.setConfidence(confidence); // 基于AI分析结果计算的动态置信度
            analysisResult.setModelVersion(response.getProviderUsed());
            
            // 保存到数据库
            AIAnalysisResult saved = aiAnalysisResultRepository.save(analysisResult);
            logger.info("AI分析结果已保存，ID: {}, 项目ID: {}, 置信度: {}", saved.getId(), projectId, confidence);
            
            return saved;
            
        } catch (Exception e) {
            logger.error("保存AI分析结果失败，项目ID: {}", projectId, e);
            throw new RuntimeException("Failed to save AI analysis result", e);
        }
    }
    
    /**
     * 更新项目的AI分析状态
     */
    private void updateProjectAnalysisStatus(Long projectId, Project.ApprovalStatus status, 
                                           Long analysisResultId, String errorMessage) {
        try {
            logger.info("开始更新项目AI分析状态，项目ID: {}, 状态: {}", projectId, status);
            
            Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
            
            // 检查当前状态，防止AI回调覆盖人工审核状态
            Project.ApprovalStatus currentStatus = project.getApprovalStatus();
            
            // 如果项目已经被人工审核（管理员或超级管理员），则不允许AI回调覆盖状态
            if (currentStatus == Project.ApprovalStatus.ADMIN_APPROVED ||
                currentStatus == Project.ApprovalStatus.ADMIN_REJECTED ||
                currentStatus == Project.ApprovalStatus.SUPER_ADMIN_APPROVED ||
                currentStatus == Project.ApprovalStatus.SUPER_ADMIN_REJECTED ||
                currentStatus == Project.ApprovalStatus.FINAL_APPROVED) {
                
                logger.warn("阻止AI回调覆盖人工审核状态！项目ID: {}, 当前状态: {}, 尝试设置: {}", 
                           projectId, currentStatus, status);
                return; // 直接返回，不更新状态
            }
            
            // 只有当前状态是AI相关状态时，才允许AI回调更新
            if (currentStatus == Project.ApprovalStatus.AI_ANALYZING ||
                currentStatus == Project.ApprovalStatus.AI_APPROVED ||
                currentStatus == Project.ApprovalStatus.AI_REJECTED) {
                
                logger.info("AI回调更新状态：项目ID: {}, 从 {} 更新为 {}", projectId, currentStatus, status);
                // 更新审批状态
                project.setApprovalStatus(status);
            } else {
                logger.warn("项目状态异常，当前状态: {}，无法通过AI回调更新为: {}", currentStatus, status);
                return; // 不更新状态
            }
            
            // 如果有分析结果ID，则关联
            if (analysisResultId != null) {
                project.setAiAnalysisId(analysisResultId);
                logger.info("关联AI分析结果ID: {} 到项目: {}", analysisResultId, projectId);
            }
            
            // 如果有错误信息，则设置拒绝理由
            if (errorMessage != null) {
                project.setRejectionReason(errorMessage);
            }
            
            // 保存项目
            projectRepository.save(project);
            logger.info("项目状态已更新，ID: {}, 状态: {}", projectId, status);
            
        } catch (Exception e) {
            logger.error("更新项目AI分析状态失败，项目ID: {}", projectId, e);
            throw new RuntimeException("Failed to update project status", e);
        }
    }
    
    /**
     * 根据AI分析结果判断置信度 (0.0-1.0)
     * 0.0-0.5: 不通过，0.5-1.0: 通过
     * 优先从JSON中提取confidence字段，如果失败则使用关键词分析
     */
    private double calculateConfidenceFromResult(String analysisResult) {
        logger.info("=== 置信度计算 ===\n原始结果: {}", analysisResult);
        
        if (analysisResult == null || analysisResult.trim().isEmpty()) {
            logger.info("结果为空，置信度: 0.2");
            return 0.2; // 不通过
        }
        
        // 第一优先级：尝试从JSON中提取confidence字段
        try {
            if (analysisResult.contains("confidence")) {
                String confidenceStr = extractJsonValue(analysisResult, "confidence");
                if (!confidenceStr.equals("未找到") && !confidenceStr.contains("错误") && !confidenceStr.contains("失败")) {
                    double confidence = Double.parseDouble(confidenceStr);
                    logger.info("=== 从JSON提取置信度成功 ===\n置信度: {}\n通过判断: {}", 
                               confidence, confidence >= 0.5 ? "通过" : "拒绝");
                    return confidence;
                }
            }
        } catch (Exception e) {
            logger.warn("从JSON提取置信度失败: {}", e.getMessage());
        }
        
        // 第二优先级：关键词判断逻辑（兜底方案）
        String result = analysisResult.toLowerCase();
        
        // 优先检查明确的结论性词汇
        boolean hasClearReject = result.contains("总体评价：不通过") || result.contains("总体评价：**不通过**") ||
                                result.contains("不通过") || result.contains("不可行") || result.contains("拒绝");
        
        boolean hasClearPass = result.contains("总体评价：通过") || result.contains("总体评价：**通过**") ||
                              (result.contains("通过") && !result.contains("不通过"));
        
        // 辅助的积极和消极指标
        boolean hasPositiveIndicators = result.contains("可行") || result.contains("推荐") ||
                                       result.contains("合格") || result.contains("建议");
                             
        boolean hasNegativeIndicators = result.contains("风险过高") || result.contains("无法实现") || 
                                       result.contains("失败") || result.contains("不合格") ||
                                       result.contains("缺失") || result.contains("严重");
        
        double confidence;
        if (hasClearReject) {
            confidence = 0.3; // 明确拒绝 -> 低置信度
        } else if (hasClearPass) {
            confidence = 0.8; // 明确通过 -> 高置信度  
        } else if (hasNegativeIndicators && !hasPositiveIndicators) {
            confidence = 0.4; // 消极指标主导 -> 中低置信度
        } else if (hasPositiveIndicators && !hasNegativeIndicators) {
            confidence = 0.7; // 积极指标主导 -> 中高置信度
        } else {
            confidence = 0.5; // 混合或无明确信号 -> 阈值边缘
        }
        
        logger.info("=== 关键词分析置信度 ===\n明确通过: {}\n明确拒绝: {}\n积极指标: {}\n消极指标: {}\n置信度: {}\n通过判断: {}", 
                   hasClearPass, hasClearReject, hasPositiveIndicators, hasNegativeIndicators, confidence, confidence >= 0.7 ? "通过" : "拒绝");
        return confidence;
    }
    
    /**
     * 简单的JSON值提取方法（从AIAnalysisService复制）
     */
    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\"";
            int keyIndex = json.indexOf(searchKey);
            if (keyIndex == -1) return "未找到";
            
            int colonIndex = json.indexOf(":", keyIndex);
            if (colonIndex == -1) return "格式错误";
            
            int startIndex = colonIndex + 1;
            // 跳过空格
            while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
                startIndex++;
            }
            
            if (startIndex >= json.length()) return "值为空";
            
            // 确定值的结束位置
            int endIndex;
            char firstChar = json.charAt(startIndex);
            
            if (firstChar == '"') {
                // 字符串值
                startIndex++; // 跳过开始的引号
                endIndex = json.indexOf('"', startIndex);
                if (endIndex == -1) return "字符串未闭合";
                return json.substring(startIndex, endIndex);
            } else {
                // 数字或布尔值
                endIndex = startIndex;
                while (endIndex < json.length() && 
                       json.charAt(endIndex) != ',' && 
                       json.charAt(endIndex) != '}' && 
                       json.charAt(endIndex) != ']' &&
                       !Character.isWhitespace(json.charAt(endIndex))) {
                    endIndex++;
                }
                return json.substring(startIndex, endIndex).trim();
            }
        } catch (Exception e) {
            return "提取失败: " + e.getMessage();
        }
    }
}