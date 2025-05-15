package com.example.demo.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

	@GetMapping({"","/"})
	public String home(Model model, Principal principal) {
		boolean isLoggedIn = principal != null;
		String loginId = isLoggedIn ? principal.getName() : "guest";
		model.addAttribute("loginId", loginId);
		model.addAttribute("isLoggedIn", isLoggedIn);
		return "index";
	}
}
