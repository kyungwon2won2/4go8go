package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.model.Users;
import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.service.AdminService;
import com.example.demo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

		// 통계 데이터 계산
		long activeUsers = users.stream().filter(u -> "ACTIVE".equals(u.getStatus()) || u.getStatus() == null).count();
		long suspendedUsers = users.stream().filter(u -> "SUSPENDED".equals(u.getStatus())).count();
		long normalUsers = users.stream().filter(u -> u.getSocialType() == null).count();
		long googleUsers = users.stream().filter(u -> "GOOGLE".equals(u.getSocialType())).count();
		long naverUsers = users.stream().filter(u -> "NAVER".equals(u.getSocialType())).count();

		model.addAttribute("activeUsers", activeUsers);
		model.addAttribute("suspendedUsers", suspendedUsers);
		model.addAttribute("newUsersToday", 0); // 향후 구현
		model.addAttribute("normalUsers", normalUsers);
		model.addAttribute("googleUsers", googleUsers);
		model.addAttribute("naverUsers", naverUsers);

		// 권한 관리 데이터
		Map<String, Long> roleStats = adminService.getRoleStatistics();
		model.addAttribute("userRoleCount", roleStats.getOrDefault("ROLE_USER", 0L));
		model.addAttribute("adminRoleCount", roleStats.getOrDefault("ROLE_ADMIN", 0L));

		// 관리자 사용자 목록
		List<Users> adminUsers = adminService.getAdminUsers();
		model.addAttribute("adminUsers", adminUsers);

		// 현재 사용자 ID (자기 자신의 권한은 회수할 수 없도록)
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = auth.getName();
		Users currentUser = userService.getUserByEmail(currentUsername);
		if (currentUser != null) {
			model.addAttribute("currentUserId", currentUser.getUserId());
		}

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
		return "redirect:/admin";
	}

	// AJAX - 사용자 검색
	@GetMapping("/user/search")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> searchUser(@RequestParam String email) {
		Map<String, Object> response = new HashMap<>();
		
		try {
			Users user = adminService.getUserByEmail(email);
			if (user != null) {
				// 사용자의 권한 정보도 함께 조회
				List<UserRole> roles = adminService.getUserRoles(user.getUserId());
				user.setRoleList(roles);
				
				response.put("success", true);
				response.put("user", user);
			} else {
				response.put("success", false);
				response.put("message", "사용자를 찾을 수 없습니다.");
			}
		} catch (Exception e) {
			log.error("사용자 검색 중 오류 발생", e);
			response.put("success", false);
			response.put("message", "검색 중 오류가 발생했습니다.");
		}
		
		return ResponseEntity.ok(response);
	}

	// 권한 부여
	@PostMapping("/role/assign")
	public String assignRole(@RequestParam String userEmail,
							 @RequestParam String roleName) {
		log.info("권한 부여 요청: email={}, role={}", userEmail, roleName);
		
		try {
			boolean result = adminService.assignRole(userEmail, roleName);
			if (result) {
				return "redirect:/admin?success=role-assigned";
			} else {
				return "redirect:/admin?error=user-not-found";
			}
		} catch (Exception e) {
			log.error("권한 부여 중 오류 발생", e);
			return "redirect:/admin?error=role-assign-failed";
		}
	}

	// 권한 회수
	@PostMapping("/role/revoke")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> revokeRole(@RequestParam Integer userId,
														  @RequestParam String roleName) {
		Map<String, Object> response = new HashMap<>();
		
		try {
			boolean result = adminService.revokeRole(userId, roleName);
			response.put("success", result);
			
			if (result) {
				response.put("message", "권한이 성공적으로 회수되었습니다.");
			} else {
				response.put("message", "권한 회수에 실패했습니다.");
			}
		} catch (Exception e) {
			log.error("권한 회수 중 오류 발생", e);
			response.put("success", false);
			response.put("message", "권한 회수 중 오류가 발생했습니다.");
		}
		
		return ResponseEntity.ok(response);
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