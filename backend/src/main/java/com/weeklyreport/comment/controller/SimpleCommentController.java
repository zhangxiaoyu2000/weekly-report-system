package com.weeklyreport.comment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SimpleCommentController {

    @GetMapping("/simple-test")
    public String simpleTest() {
        return "Simple controller works!";
    }
}