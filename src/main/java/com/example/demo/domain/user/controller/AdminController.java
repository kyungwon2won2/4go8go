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
// @PreAuthorize("hasRole('ROLE_ADMIN')") // 임시로 주석 처리
@RequiredArgsConstructor
public class AdminController {

	private final AdminService adminService;
	private final UserService userService;

	// 관리자 대시보드
	@GetMapping({"", "/"})
	public String adminDashboard(Model model) {
		// 새로운 통계 시스템 사용
		Map<String, Object> userStats = adminService.getUserStatistics();
		
		// 기본 통계 정보
		model.addAttribute("totalUsers", userStats.get("totalUsers"));
		model.addAttribute("activeUsers", userStats.get("activeUsers"));
		model.addAttribute("suspendedUsers", userStats.get("suspendedUsers"));
		
		// 기간별 가입자 수
		model.addAttribute("todayRegistrations", userStats.get("todayRegistrations"));
		model.addAttribute("thisWeekRegistrations", userStats.get("thisWeekRegistrations"));
		model.addAttribute("thisMonthRegistrations", userStats.get("thisMonthRegistrations"));
		
		// 가입 방식별 통계
		model.addAttribute("normalUsers", userStats.get("normalUsers"));
		model.addAttribute("googleUsers", userStats.get("googleUsers"));
		model.addAttribute("naverUsers", userStats.get("naverUsers"));
		
		// 이메일 인증 통계
		model.addAttribute("emailVerifiedUsers", userStats.get("emailVerifiedUsers"));
		model.addAttribute("emailUnverifiedUsers", userStats.get("emailUnverifiedUsers"));
		
		// 연령대별 통계
		model.addAttribute("ageGroups", userStats.get("ageGroups"));

		// 기존 사용자 목록 (사용자 관리용)
		List<Users> users = userService.getAllUsers();
		model.addAttribute("users", users);

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

	// AJAX - 통계 데이터 API
	@GetMapping("/api/statistics/users")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getUserStatisticsApi() {
		try {
			Map<String, Object> stats = adminService.getUserStatistics();
			return ResponseEntity.ok(stats);
		} catch (Exception e) {
			log.error("통계 데이터 조회 중 오류 발생", e);
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("error", "통계 데이터 조회에 실패했습니다.");
			return ResponseEntity.internalServerError().body(errorResponse);
		}
	}
}