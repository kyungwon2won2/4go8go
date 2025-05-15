package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.dto.UpdateUserDTO;
import com.example.demo.domain.user.model.Users;
import com.example.demo.domain.user.service.UserService;
import jakarta.validation.Valid;
import com.example.demo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	// 사용자 정보 조회
	@GetMapping("/profile")
	public String userProfile(Model model, Principal principal) {
		log.info("[[[[  /user ]]]]");
		Users user = userService.getUserById(principal.getName());
		model.addAttribute("user", user);
		return "user/profile";
	}

	// 사용자 목록 조회 (관리자만 접근 가능)
	@GetMapping("/list")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public String userList(Model model) {
		model.addAttribute("users", userService.getAllUsers());
		return "user/list";
	}

	@GetMapping("/edit")
	public String editUserForm(Model model, Principal principal) {
		Users user = userService.getUserByEmail(principal.getName());
		model.addAttribute("user", user);
		return "user/edit";
	}

	@PostMapping("/edit")
	public String updateUser(@Valid UpdateUserDTO dto) {
		userService.updateUser(dto);
		return "redirect:/user";
	}

	// 회원 탈퇴
	@PostMapping("/delete")
	public String deleteUser(Principal principal) {
		userService.deleteUser(principal.getName());
		return "redirect:/";
	}

}