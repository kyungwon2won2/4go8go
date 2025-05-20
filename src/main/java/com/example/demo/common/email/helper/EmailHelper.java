package com.example.demo.common.email.helper;

import com.example.demo.domain.coupon.model.BirthdayCoupon;
import com.example.demo.domain.user.model.Users;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.text.SimpleDateFormat;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailHelper {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 생일 축하 이메일 발송
    public boolean sendBirthdayEmail(Users user, BirthdayCoupon coupon) {
        try {
            // 이메일 메시지 생성
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 이메일 기본 정보 설정
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject(user.getNickname() + "님, 생일을 축하합니다! 🎂");

            // 이메일 템플릿 컨텍스트 설정
            Context context = new Context();
            context.setVariable("name", user.getNickname());
            context.setVariable("couponCode", coupon.getCouponCode());
            context.setVariable("discountAmount", coupon.getDiscountAmount());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일");
            context.setVariable("validTo", sdf.format(coupon.getValidTo()));

            // 이메일 내용 설정 (HTML)
            String htmlContent = generateBirthdayEmailContent(context);
            helper.setText(htmlContent, true);

            // 이메일 발송
            mailSender.send(message);
            log.info("생일 축하 이메일 발송 성공: {}", user.getEmail());
            return true;
        } catch (MessagingException e) {
            log.error("생일 축하 이메일 발송 실패: {}", user.getEmail(), e);
            return false;
        }
    }

    //생일 축하 이메일 내용 생성
    private String generateBirthdayEmailContent(Context context) {
        return templateEngine.process("email/birthday-email", context);
    }

}
