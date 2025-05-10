package com.example.demo.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {
	
	@GetMapping({"","/"})
	public String home(Model model, Principal principal) {
		String loginId = principal !=null ? principal.getName() : "guest";
		model.addAttribute(loginId, loginId);
		return "index";
	}
}
