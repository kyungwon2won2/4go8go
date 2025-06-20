package com.example.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan(basePackages = "com.example.demo.mapper")
@EnableScheduling // 스케줄링 활성화
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
