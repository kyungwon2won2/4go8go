package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.dto.TestDto;
import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import com.example.demo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/join")
	public String CreateUser(@ModelAttribute TestDto testDto) throws Exception {

		userService.createUser(testDto);
		return "redirect:/login";
	}

	@GetMapping
	public String index(Model model, Principal principal) {
		log.info("[[[[  /user ]]]]");
		Users user = userService.getUserById(principal.getName());
		model.addAttribute("user", user);
		return "user/index";
	}

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

	@GetMapping("/edit")
	public String editUserForm(Model model, Principal principal) {
		String email = principal.getName();
//		Users user = userService.login(email);
//		model.addAttribute("user", user);
		return "user/edit";
	}

	@PostMapping("/edit")
	public String updateUser(@ModelAttribute Users user, Date birthDate, RedirectAttributes ra) {
		user.setBirthDate(birthDate);

		try {
			int result = userService.updateUser(user);
			if(result > 0) {
				ra.addFlashAttribute("message", "회원정보가 성공적으로 수정되었습니다.");
			} else {
				ra.addFlashAttribute("error", "회원정보 수정에 실패했습니다.");
			}
		} catch (Exception e) {
			ra.addFlashAttribute("error", "처리 중 오류가 발생했습니다: " + e.getMessage());
		}
		return "redirect:/user";
	}

	// 회원 탈퇴
	@PostMapping("/delete")
	public String deleteUser(Principal principal) {
		userService.deleteUser(principal.getName());
		return "redirect:/logout";
	}

}
