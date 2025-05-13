package com.example.demo.common.batchscheduler.batch;

import com.example.demo.common.email.EmailService;
import com.example.demo.domain.coupon.model.BirthdayCoupon;
import com.example.demo.domain.coupon.service.CouponService;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.CouponMapper;
import com.example.demo.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.List;

@Configuration
@Slf4j
public class SampleJobConfig {

    @Bean
    public Job birthdayEmailJob(JobRepository jobRepository, Step birthdayEmailStep) {
        return new JobBuilder("birthdayEmailJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(birthdayEmailStep)
                .build();
    }

    @Bean
    public Step birthdayEmailStep(JobRepository jobRepository, 
                            PlatformTransactionManager transactionManager,
                            ItemReader<Users> birthdayUserReader,
                            ItemProcessor<Users, Users> birthdayUserProcessor,
                            ItemWriter<Users> birthdayUserWriter) {
        
        return new StepBuilder("birthdayEmailStep", jobRepository)
                .<Users, Users>chunk(100, transactionManager)
                .reader(birthdayUserReader)
                .processor(birthdayUserProcessor)
                .writer(birthdayUserWriter)
                .build();
    }
    
    @Bean
    @StepScope
    public ItemReader<Users> birthdayUserReader(UserMapper userMapper) {
        List<Users> birthdayUsers = userMapper.findUsersByBirthdayToday();
        log.info("오늘 생일인 사용자 수: {}", birthdayUsers.size());
        return new ListItemReader<>(birthdayUsers);
    }
    
    @Bean
    @StepScope
    public ItemProcessor<Users, Users> birthdayUserProcessor() {
        return user -> {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                log.info("생일 이메일 처리 대상: {}님, 이메일: {}", user.getName(), user.getEmail());
                return user;
            }
            log.warn("이메일 정보가 없는 사용자: {}", user.getName());
            return null;
        };
    }
    
    @Bean
    @Transactional
    public ItemWriter<Users> birthdayUserWriter(EmailService emailService, CouponService couponService, CouponMapper couponMapper) {
        return users -> {
            for (Users user : users) {
                try {

                    BirthdayCoupon birthdayCoupon = couponService.createBirthdayCoupon(user);
                    int result = couponMapper.insertBirthdayCoupon(birthdayCoupon);

                    if (result > 0) {
                        boolean emailSent = emailService.sendBirthdayEmail(user, birthdayCoupon);
                        if (emailSent) {
                            log.info("생일 축하 이메일 발송 완료: {}님, 이메일: {}", user.getName(), user.getEmail());
                        } else {
                            log.warn("생일 축하 이메일 발송 실패: {}님, 이메일: {}", user.getName(), user.getEmail());
                        }
                    } else {
                        log.error("생일 쿠폰 저장 실패: {}님", user.getName());
                    }
                } catch (Exception e) {
                    log.error("생일 쿠폰/이메일 처리 실패: {} - 오류: {}", user.getEmail(), e.getMessage(), e);
                }
            }
            log.info("생일 이메일 및 쿠폰 발송 배치 완료");
        };
    }
}