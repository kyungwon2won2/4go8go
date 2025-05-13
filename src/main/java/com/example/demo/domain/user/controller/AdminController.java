package com.example.demo.domain.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {

	@GetMapping
	public String index() {
		log.info("[[[  /admin ]]]");
		return "admin/index";  // admin 폴더의 index.html을 명시적으로 지정
	}
}
