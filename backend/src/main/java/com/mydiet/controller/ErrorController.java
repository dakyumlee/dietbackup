package com.mydiet.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
public class ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        log.error("=== OAuth 에러 발생 ===");
        
        Object status = request.getAttribute("javax.servlet.error.status_code");
        Object exception = request.getAttribute("javax.servlet.error.exception");
        Object message = request.getAttribute("javax.servlet.error.message");
        
        log.error("Status: {}", status);
        log.error("Exception: {}", exception);
        log.error("Message: {}", message);
        
        if (exception != null) {
            log.error("Exception details: ", (Throwable) exception);
        }
        
        return "redirect:/auth.html?error=oauth_failed";
    }
}