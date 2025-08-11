package com.mydiet.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class MyErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        
        log.error("에러 발생 - 상태코드: {}, 예외: {}, URI: {}", 
                status, 
                exception != null ? exception.toString() : "없음",
                request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "redirect:/auth.html?error=page_not_found";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "redirect:/auth.html?error=server_error";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "redirect:/auth.html?error=access_denied";
            }
        }
        
        return "redirect:/auth.html?error=unknown_error";
    }
}