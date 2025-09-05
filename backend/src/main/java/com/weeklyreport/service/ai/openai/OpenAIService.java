package com.weeklyreport.service.ai.openai;

import com.weeklyreport.config.AIConfig;
import com.weeklyreport.service.ai.AIServiceType;
import com.weeklyreport.service.ai.AbstractAIServiceProvider;
import com.weeklyreport.service.ai.dto.AIAnalysisRequest;
import com.weeklyreport.service.ai.dto.AIAnalysisResponse;
import com.weeklyreport.service.ai.exception.AIServiceException;
import com.weeklyreport.service.ai.openai.dto.OpenAIRequest;
import com.weeklyreport.service.ai.openai.dto.OpenAIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpenAI GPT service implementation
 */
@Service
@ConditionalOnProperty(name = "ai.openai.enabled", havingValue = "true")
public class OpenAIService extends AbstractAIServiceProvider {
    
    private final AIConfig aiConfig;
    private final RestTemplate restTemplate;
    
    private static final String CHAT_COMPLETIONS_ENDPOINT = "/chat/completions";
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("\\b[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*\\b");
    
    @Autowired
    public OpenAIService(AIConfig aiConfig, RestTemplate restTemplate) {
        this.aiConfig = aiConfig;
        this.restTemplate = restTemplate;
    }
    
    @Override
    public AIServiceType getServiceType() {
        return AIServiceType.OPENAI;
    }
    
    @Override
    public boolean isAvailable() {
        AIConfig.OpenAIConfig config = aiConfig.getOpenai();
        return config.isEnabled() && 
               config.getApiKey() != null && 
               !config.getApiKey().trim().isEmpty();
    }
    
    @Override
    public String getConfigurationStatus() {
        AIConfig.OpenAIConfig config = aiConfig.getOpenai();
        if (!config.isEnabled()) {
            return "DISABLED";
        }
        if (config.getApiKey() == null || config.getApiKey().trim().isEmpty()) {
            return "MISSING_API_KEY";
        }
        return "CONFIGURED";
    }
    
    @Override
    public int getMaxTokens() {
        return aiConfig.getOpenai().getMaxTokens();
    }
    
    @Override
    public double getCostEstimate(String content) {
        // Rough estimate: $0.0015 per 1K tokens for GPT-3.5-turbo
        int estimatedTokens = content.length() / 4; // Rough estimate: 4 characters per token
        return (estimatedTokens / 1000.0) * 0.15; // Return in cents
    }
    
    @Override
    protected AIAnalysisResponse performAnalysis(AIAnalysisRequest request) throws AIServiceException {
        AIConfig.OpenAIConfig config = aiConfig.getOpenai();
        
        try {
            // Build the prompt based on analysis type
            String systemPrompt = buildSystemPrompt(request.getAnalysisType());
            String userPrompt = buildUserPrompt(request);
            
            // Create OpenAI request
            OpenAIRequest openAIRequest = new OpenAIRequest(
                config.getModel(),
                Arrays.asList(
                    new OpenAIRequest.Message("system", systemPrompt),
                    new OpenAIRequest.Message("user", userPrompt)
                ),
                config.getTemperature(),
                Math.min(config.getMaxTokens(), 1000) // Limit response tokens
            );
            
            // Make API call
            OpenAIResponse openAIResponse = callOpenAI(openAIRequest, config);
            
            // Process response
            return processResponse(openAIResponse, request.getAnalysisType());
            
        } catch (Exception e) {
            logger.error("OpenAI analysis failed", e);
            throw new AIServiceException("OpenAI analysis failed: " + e.getMessage(), e, 
                                       getProviderName(), "API_CALL_FAILED");
        }
    }
    
    /**
     * Build system prompt based on analysis type
     */
    private String buildSystemPrompt(AIAnalysisRequest.AnalysisType analysisType) {
        switch (analysisType) {
            case SUMMARY:
                return "You are an expert at creating concise, professional summaries of weekly reports. " +
                       "Focus on key achievements, main challenges, and important updates. " +
                       "Keep the summary under 200 words.";
            
            case SENTIMENT:
                return "You are a sentiment analysis expert. Analyze the overall sentiment of the text. " +
                       "Return only one of: POSITIVE, NEGATIVE, NEUTRAL, MIXED. " +
                       "Consider the tone, emotions, and overall feeling conveyed.";
            
            case KEYWORDS:
                return "You are an expert at extracting key terms and phrases from text. " +
                       "Return the 10 most important keywords or phrases, separated by commas. " +
                       "Focus on technical terms, project names, and important concepts.";
            
            case RISK_ASSESSMENT:
                return "You are a project management expert specializing in risk assessment. " +
                       "Analyze the text for potential risks, blockers, or warning signs. " +
                       "Rate the overall risk level as: LOW, MEDIUM, HIGH, CRITICAL. " +
                       "Provide a brief explanation of the main concerns.";
            
            case SUGGESTIONS:
                return "You are a productivity and project management consultant. " +
                       "Based on the weekly report, provide 3-5 actionable improvement suggestions. " +
                       "Focus on efficiency, communication, and goal achievement.";
            
            case PROGRESS_PREDICTION:
                return "You are a project progress analyst. Based on the current status and trends, " +
                       "predict the likelihood of meeting upcoming deadlines and goals. " +
                       "Provide a confidence level and key factors affecting progress.";
            
            default:
                return "You are a helpful AI assistant analyzing weekly reports.";
        }
    }
    
    /**
     * Build user prompt with content and context
     */
    private String buildUserPrompt(AIAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        if (request.getContext() != null && !request.getContext().trim().isEmpty()) {
            prompt.append("Context: ").append(request.getContext()).append("\n\n");
        }
        
        prompt.append("Please analyze the following weekly report content:\n\n");
        prompt.append(request.getContent());
        
        return prompt.toString();
    }
    
    /**
     * Make the actual API call to OpenAI
     */
    private OpenAIResponse callOpenAI(OpenAIRequest request, AIConfig.OpenAIConfig config) 
            throws AIServiceException {
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());
        
        HttpEntity<OpenAIRequest> entity = new HttpEntity<>(request, headers);
        String url = config.getBaseUrl() + CHAT_COMPLETIONS_ENDPOINT;
        
        try {
            ResponseEntity<OpenAIResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, OpenAIResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new AIServiceException("OpenAI API returned error: " + response.getStatusCode(),
                                           getProviderName(), "API_ERROR");
            }
            
        } catch (HttpClientErrorException e) {
            String errorMessage = extractErrorMessage(e.getResponseBodyAsString());
            throw new AIServiceException("OpenAI API error: " + errorMessage, e,
                                       getProviderName(), "HTTP_" + e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            throw new AIServiceException("Failed to connect to OpenAI API", e,
                                       getProviderName(), "CONNECTION_FAILED");
        }
    }
    
    /**
     * Process OpenAI response and convert to our format
     */
    private AIAnalysisResponse processResponse(OpenAIResponse openAIResponse, 
                                             AIAnalysisRequest.AnalysisType analysisType) {
        AIAnalysisResponse response = new AIAnalysisResponse();
        response.setAnalysisType(analysisType);
        
        if (openAIResponse.getChoices() != null && !openAIResponse.getChoices().isEmpty()) {
            String content = openAIResponse.getChoices().get(0).getMessage().getContent();
            response.setResult(content.trim());
            
            // Extract keywords if this was a keyword analysis or for any other type
            if (analysisType == AIAnalysisRequest.AnalysisType.KEYWORDS) {
                response.setKeywords(Arrays.asList(content.split(",\\s*")));
            } else {
                // Extract keywords from content using pattern matching
                response.setKeywords(extractKeywords(content));
            }
            
            // Set confidence based on finish reason
            String finishReason = openAIResponse.getChoices().get(0).getFinishReason();
            response.setConfidence("stop".equals(finishReason) ? 0.95 : 0.8);
        }
        
        // Add metadata
        Map<String, Object> metadata = new HashMap<>();
        if (openAIResponse.getUsage() != null) {
            metadata.put("tokensUsed", openAIResponse.getUsage().getTotalTokens());
            metadata.put("promptTokens", openAIResponse.getUsage().getPromptTokens());
            metadata.put("completionTokens", openAIResponse.getUsage().getCompletionTokens());
        }
        metadata.put("model", openAIResponse.getModel());
        metadata.put("openaiId", openAIResponse.getId());
        response.setMetadata(metadata);
        
        return response;
    }
    
    /**
     * Extract error message from OpenAI API response
     */
    private String extractErrorMessage(String responseBody) {
        try {
            // Simple extraction - could be improved with JSON parsing
            if (responseBody.contains("\"message\"")) {
                int start = responseBody.indexOf("\"message\"") + 10;
                int end = responseBody.indexOf("\"", start + 1);
                if (end > start) {
                    return responseBody.substring(start + 1, end);
                }
            }
            return responseBody;
        } catch (Exception e) {
            return "Unknown error";
        }
    }
    
    /**
     * Extract keywords from text using pattern matching
     */
    private List<String> extractKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        Matcher matcher = KEYWORD_PATTERN.matcher(text);
        
        while (matcher.find() && keywords.size() < 10) {
            String keyword = matcher.group().trim();
            if (keyword.length() > 2 && !keywords.contains(keyword)) {
                keywords.add(keyword);
            }
        }
        
        return keywords;
    }
}