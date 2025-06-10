package com.example.demo.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class CustomErrorPageController {

    @RequestMapping("/errorPage")
    public String handleErrorPage(Model model, HttpServletRequest request) {
        model.addAttribute("status", request.getAttribute("javax.servlet.error.status_code"));
        model.addAttribute("message", request.getAttribute("javax.servlet.error.message"));
        model.addAttribute("exception", request.getAttribute("javax.servlet.error.exception"));
        model.addAttribute("trace", request.getAttribute("javax.servlet.error.exception"));
        model.addAttribute("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("path", request.getAttribute("javax.servlet.error.request_uri"));
        model.addAttribute("code", "ERROR_GENERIC");
        return "errorPage";
    }
}
