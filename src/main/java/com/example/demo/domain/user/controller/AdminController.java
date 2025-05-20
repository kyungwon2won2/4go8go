package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.model.Users;
import com.example.demo.domain.user.service.AdminService;
import com.example.demo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminController {

	private final AdminService adminService;
	private final UserService userService;

	// 관리자 대시보드
	@GetMapping({"", "/"})
	public String adminDashboard(Model model) {
		// 대시보드에 표시할 데이터 로딩
		List<Users> users = userService.getAllUsers();
		model.addAttribute("users", users);
		model.addAttribute("totalUsers", users.size());

		// 향후 추가 데이터를 여기에 추가 (상품 수, 주문 수 등)

		return "admin/index";
	}

	// 사용자 관리 페이지
	@GetMapping("/users")
	public String userManagement(Model model) {
		List<Users> users = userService.getAllUsers();
		model.addAttribute("users", users);
		return "admin/users";
	}

	// 사용자 상세 정보 페이지
	@GetMapping("/user/{id}")
	public String userDetail(@PathVariable Integer id, Model model) {
		Users user = adminService.getUserById(id);
		if (user == null) {
			return "redirect:/admin/users?error=user-not-found";
		}
		model.addAttribute("user", user);
		return "admin/user-detail";
	}

	// 사용자 상태 변경
	@PostMapping("/user/status")
	public String updateUserStatus(@RequestParam Integer userId,
								   @RequestParam String status) {
		log.info("사용자 상태 변경 요청: id={}, status={}", userId, status);
		adminService.updateUserStatus(userId, status);
		return "redirect:/admin/users";
	}

	// 상품 관리 페이지
	@GetMapping("/products")
	public String productManagement(Model model) {
		// 상품 관련 데이터를 모델에 추가
		return "admin/products";
	}

	// 주문 관리 페이지
	@GetMapping("/orders")
	public String orderManagement(Model model) {
		// 주문 관련 데이터를 모델에 추가
		return "admin/orders";
	}

	// 통계 페이지
	@GetMapping("/statistics")
	public String statistics(Model model) {
		// 통계 데이터를 모델에 추가
		return "admin/statistics";
	}
}