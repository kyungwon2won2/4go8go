package com.example.demo.domain.user.controller;

import com.example.demo.common.security.service.UserService;
import com.example.demo.domain.user.model.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Date;

@Slf4j
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	@GetMapping
	public String index() {
		log.info("[[[[  /user ]]]]");
		return "user/index";  // 명시적으로 user/index.html을 호출
	}


	private final UserService userService;

	// 사용자 목록 조회 (관리자만 접근 가능)
	@GetMapping("/list")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public String userList(Model model) {
		model.addAttribute("users", userService.getAllUsers());
		return "user/list";
	}

	// 사용자 상세 정보
	@GetMapping("/profile")
	public String userProfile(Model model, Principal principal) {
		Users user = userService.getUserById(principal.getName());
		model.addAttribute("user", user);
		return "user/profile";
	}

	// 사용자 정보 수정 폼
	@GetMapping("/edit")
	public String editForm(Model model, Principal principal) {
		Users user = userService.getUserById(principal.getName());
		model.addAttribute("user", user);
		return "user/edit";
	}

	// 사용자 정보 수정 처리
	@PostMapping("/edit")
	public String editSubmit(@ModelAttribute Users user, Principal principal) {
		// 본인 정보만 수정 가능하도록 검증
		if (!principal.getName().equals(user.getUserId())) {
			return "redirect:/exception";
		}
		userService.updateUser(user);
		return "redirect:/user/profile";
	}

	// 회원 탈퇴
	@PostMapping("/delete")
	public String deleteUser(Principal principal) {
		userService.deleteUser(principal.getName());
		return "redirect:/logout";
	}

}
