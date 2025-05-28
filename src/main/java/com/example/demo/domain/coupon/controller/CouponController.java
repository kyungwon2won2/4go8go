package com.example.demo.domain.coupon.controller;

import com.example.demo.domain.coupon.model.BirthdayCoupon;
import com.example.demo.domain.coupon.service.CouponService;
import com.example.demo.domain.user.model.CustomerUser;
import com.example.demo.domain.user.model.Users;
import com.example.demo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final UserService userService;
    
    // 쿠폰 적용 API
    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyCoupon(
            @RequestParam("couponCode") String couponCode,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 사용자 정보 가져오기
            CustomerUser customerUser = (CustomerUser) authentication.getPrincipal();
            String email = customerUser.getUsername();
            Users user = userService.getUserByEmail(email);
            
            // 쿠폰 검증
            BirthdayCoupon coupon = couponService.validateCoupon(couponCode);
            
            if (coupon == null) {
                response.put("success", false);
                response.put("message", "유효하지 않은 쿠폰 코드입니다.");
                return ResponseEntity.ok(response);
            }
            
            // 쿠폰 소유자 확인
            if (coupon.getUserId() != user.getUserId()) {
                response.put("success", false);
                response.put("message", "해당 쿠폰을 사용할 수 없습니다.");
                return ResponseEntity.ok(response);
            }
            
            // 쿠폰 사용 처리
            boolean result = couponService.useCoupon(couponCode, user.getUserId());
            
            if (result) {
                response.put("success", true);
                response.put("message", "쿠폰이 성공적으로 적용되었습니다.");
                response.put("discount", coupon.getDiscountAmount());
                response.put("couponName", coupon.getCouponName());
            } else {
                response.put("success", false);
                response.put("message", "쿠폰 적용에 실패했습니다. 다시 시도해주세요.");
            }
            
        } catch (Exception e) {
            log.error("쿠폰 적용 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "쿠폰 적용 중 오류가 발생했습니다.");
        }
        
        return ResponseEntity.ok(response);
    }
}
