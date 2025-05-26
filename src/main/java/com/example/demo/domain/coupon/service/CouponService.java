package com.example.demo.domain.coupon.service;

import com.example.demo.domain.coupon.model.BirthdayCoupon;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.CouponMapper;
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponMapper couponMapper;
    private final UserMapper userMapper;

    public BirthdayCoupon createBirthdayCoupon(Users user) {
        String couponCode = UUID.randomUUID().toString().substring(0, 10);
        
        // 현재 날짜 기준으로 쿠폰 유효기간 설정 (7일)
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        Date expiryDate = calendar.getTime();
        
        // 할인 금액
        int discountAmount = 3000;
        
        // 생일 쿠폰 생성
        return BirthdayCoupon.builder()
                .couponCode(couponCode)
                .couponName(user.getNickname() + "님의 생일 쿠폰")
                .discountAmount(discountAmount)
                .validFrom(currentDate)
                .validTo(expiryDate)
                .userId(user.getUserId())
                .used(false)
                .createdAt(currentDate)
                .build();
    }
    
    /**
     * 쿠폰 코드 검증
     * @param couponCode 쿠폰 코드
     * @return 유효한 쿠폰인 경우 쿠폰 정보 반환, 아니면 null
     */
    public BirthdayCoupon validateCoupon(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return null;
        }
        
        // 쿠폰 코드로 쿠폰 조회
        return couponMapper.findByCouponCode(couponCode);
    }
    
    /**
     * 쿠폰 사용 처리
     * @param couponCode 쿠폰 코드
     * @param userId 사용자 ID
     * @return 성공 시 true, 실패 시 false
     */
    @Transactional
    public boolean useCoupon(String couponCode, int userId) {
        // 쿠폰 검증
        BirthdayCoupon coupon = validateCoupon(couponCode);
        
        // 쿠폰이 유효하지 않거나, 해당 사용자의 쿠폰이 아닌 경우
        if (coupon == null || coupon.getUserId() != userId) {
            return false;
        }
        
        // 쿠폰 사용 처리
        int result = couponMapper.updateCouponUsed(coupon.getCouponId());
        userMapper.updatePoint(userId, coupon.getDiscountAmount());
        return result > 0;
    }
}
