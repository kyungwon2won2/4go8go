package com.example.demo.common.batchscheduler.batch;

import com.example.demo.common.email.helper.EmailHelper;
import com.example.demo.common.email.service.EmailService;
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
import java.util.ArrayList;
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
                log.info("생일 이메일 처리 대상: {}님, 이메일: {}", user.getNickname(), user.getEmail());
                return user;
            }
            log.warn("이메일 정보가 없는 사용자: {}", user.getNickname());
            return null;
        };
    }
    
    @Bean
    @Transactional
    public ItemWriter<Users> birthdayUserWriter(EmailHelper emailHelper, CouponService couponService, CouponMapper couponMapper) {
        List<BirthdayCoupon> birthdayCoupons = new ArrayList<>();
        return users -> {
            for (Users user : users) {
                try {
                    // 쿠폰 생성
                    BirthdayCoupon birthdayCoupon = couponService.createBirthdayCoupon(user);

                    // 쿠폰 전송
                    emailHelper.sendBirthdayEmail(user, birthdayCoupon);

                    // 쿠폰 List 에 add
                    birthdayCoupons.add(birthdayCoupon);
                } catch (Exception e) {
                    log.error("생일 쿠폰 생성 실패: {} - 오류: {}", user.getNickname(), e.getMessage(), e);
                }
            }
            
            // Bulk Insert 로 한 번에 DB에 저장
            if (!birthdayCoupons.isEmpty()) {
                try {
                    couponMapper.bulkInsertBirthdayCoupons(birthdayCoupons);
                    log.info("여기 도달은 함");
                } catch (Exception e) {
                    log.error("생일 쿠폰 bulk insert 실패: {}", e.getMessage(), e);
                }
            }
        };
    }
}