package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.dto.UpdateUserDTO;
import com.example.demo.domain.user.model.Users;
<<<<<<< Updated upstream
import com.example.demo.domain.user.service.UserService;
import jakarta.validation.Valid;
import com.example.demo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;import org.springframework.security.access.prepost.PreAuthorize;
=======
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.access.prepost.PreAuthorize;
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream

	// 사용자 정보 조회
	@GetMapping("/profile")
	public String userProfile(Model model, Principal principal) {
=======
	private final UserMapper userMapper;

	@GetMapping
	public String index(Model model, Principal principal) {
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
=======
	// 사용자 정보
	@GetMapping("/profile")
	public String profile(Model model, Principal principal) {
		// 현재 로그인한 사용자 정보 가져오기
		String email = principal.getName();
		Users user = userMapper.getUserByEmail(email);
		model.addAttribute("user", user);
		return "user/profile";
	}

	// 문자열 -> Date 변환을 위한 바인더 설정
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}

>>>>>>> Stashed changes
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