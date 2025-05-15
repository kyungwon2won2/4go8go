package com.example.demo.domain.coupon.service;

import com.example.demo.domain.coupon.model.BirthdayCoupon;
import com.example.demo.domain.user.model.Users;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Service
public class CouponService {

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
}
