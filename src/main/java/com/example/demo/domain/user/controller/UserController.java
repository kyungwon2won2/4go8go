package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.model.Users;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {

	@GetMapping
	public void index() {
		log.info("[[[[  /user ]]]]");
	}
}
