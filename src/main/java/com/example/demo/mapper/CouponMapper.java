package com.example.demo.mapper;

import com.example.demo.domain.coupon.model.BirthdayCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CouponMapper {

    // 생일 쿠폰 저장
    int insertBirthdayCoupon(BirthdayCoupon coupon);
    
    // 사용자의 활성 생일 쿠폰 조회
    BirthdayCoupon findActiveBirthdayCouponByUserId(int userId);
    
    // 쿠폰 코드로 쿠폰 조회
    BirthdayCoupon findByCouponCode(@Param("couponCode") String couponCode);
    
    // 쿠폰 사용 처리
    int updateCouponUsed(@Param("couponId") int couponId);
}
