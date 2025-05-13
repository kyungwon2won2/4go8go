package com.example.demo.domain.coupon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 생일 쿠폰 모델 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BirthdayCoupon {
    private int couponId;
    private String couponCode;
    private String couponName;
    private int discountAmount;
    private Date validFrom;
    private Date validTo;
    private int userId;
    private boolean used;
    private Date createdAt;
}
