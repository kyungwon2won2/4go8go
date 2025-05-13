package com.example.demo.common.batchscheduler.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class SampleJobLauncher extends QuartzJobBean {

    private final JobLauncher jobLauncher;
    private final Job birthdayEmailJob;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("생일 이메일 배치 작업 시작: {}", LocalDateTime.now());
        
        try {
            // 배치 작업을 구분하기 위한 파라미터 설정
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("scheduledTime", LocalDateTime.now().toString())
                    .toJobParameters();

            // Spring Batch Job 실행
            jobLauncher.run(birthdayEmailJob, jobParameters);

            log.info("생일 이메일 배치 작업 성공적으로 완료");

        } catch (Exception e) {
            log.error("생일 이메일 배치 작업 실행 중 오류 발생", e);
            throw new JobExecutionException("생일 이메일 배치 작업 실패: " + e.getMessage(), e);
        }
    }
}