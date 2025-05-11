package com.example.demo.domain.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {

	@GetMapping
	public void index() {
		log.info("[[[[  /user ]]]]");
	}

}
