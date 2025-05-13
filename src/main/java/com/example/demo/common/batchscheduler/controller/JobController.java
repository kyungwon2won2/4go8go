package com.example.demo.common.batchscheduler.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job sampleJob;

    @GetMapping("/run-job")
    public String runJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("manualTrigger", "true")
                    .toJobParameters();

            jobLauncher.run(sampleJob, jobParameters);
            return "Job triggered successfully. Check console for execution details.";
        } catch (Exception e) {
            return "Error occurred: " + e.getMessage();
        }
    }
}