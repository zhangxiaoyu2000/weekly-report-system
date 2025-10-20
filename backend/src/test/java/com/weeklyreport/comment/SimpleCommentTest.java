package com.weeklyreport.comment;

import com.weeklyreport.comment.dto.CommentCreateRequest;
import com.weeklyreport.comment.dto.CommentResponse;
import com.weeklyreport.comment.service.WeeklyReportCommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class SimpleCommentTest {

    @Autowired
    private WeeklyReportCommentService commentService;

    @Test
    public void testServiceBeanExists() {
        System.out.println("🧪 测试服务Bean是否存在...");
        assertNotNull(commentService, "WeeklyReportCommentService应该存在");
        System.out.println("✅ WeeklyReportCommentService Bean存在");
    }

    @Test
    public void testCreateCommentRequest() {
        System.out.println("🧪 测试创建评论请求DTO...");
        
        CommentCreateRequest request = new CommentCreateRequest();
        request.setWeeklyReportId(1L);
        request.setContent("测试评论");
        
        assertNotNull(request, "请求对象不应为空");
        assertEquals(1L, request.getWeeklyReportId());
        assertEquals("测试评论", request.getContent());
        
        System.out.println("✅ CommentCreateRequest DTO工作正常");
    }

    @Test
    public void testCommentResponse() {
        System.out.println("🧪 测试评论响应DTO...");
        
        CommentResponse response = new CommentResponse();
        response.setId(1L);
        response.setContent("测试响应");
        response.setUserId(1L);
        
        assertNotNull(response, "响应对象不应为空");
        assertEquals(1L, response.getId());
        assertEquals("测试响应", response.getContent());
        assertEquals(1L, response.getUserId());
        
        System.out.println("✅ CommentResponse DTO工作正常");
    }
}