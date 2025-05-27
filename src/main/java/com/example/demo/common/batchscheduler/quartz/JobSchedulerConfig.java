package com.example.demo.common.batchscheduler.quartz;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobSchedulerConfig {

    @Bean
    public JobDetail birthdayJobDetail() {
        return JobBuilder.newJob(SampleJobLauncher.class)
                .withIdentity("birthdayEmailJobDetail")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger birthdayJobTrigger() {
        // 매일 오전 9시에 실행하는 스케줄 설정
        return TriggerBuilder.newTrigger()
                .forJob(birthdayJobDetail())
                .withIdentity("birthdayEmailJobTrigger")
//                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 9 * * ?"))    // 오전 9시 실행 
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/5 * * * ?")) // 5분마다 실행
//                .withSchedule(CronScheduleBuilder.cronSchedule("0/10 * * * * ?")) // 10초마다 실행
                .build();
    }
}