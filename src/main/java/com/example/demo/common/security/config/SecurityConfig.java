package com.example.demo.common.security.config;

import com.example.demo.common.security.handler.CustomerAccessDeniedHandler;
import com.example.demo.common.security.handler.LoginSuccessHandler;
import com.example.demo.common.security.service.CustomerDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.sql.DataSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

	private final DataSource datasource;

	private final CustomerDetailService customerDetailService;

	//권한처리
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		//인증 처리
		//3단계 사용자 정의 (Mybatis , jpa)
		http
				.authorizeHttpRequests(auth -> auth
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
				.requestMatchers("/test/**").hasRole("**")
				.anyRequest().permitAll());

		http.logout(logout -> logout
						.logoutUrl("/logout")           					// 로그아웃 요청을 받을 URL
						.logoutSuccessUrl("/")          					// 로그아웃 성공 후 이동할 URL
						.deleteCookies("JSESSIONID")       // 로그아웃 시 삭제할 쿠키 설정 (선택 사항)
						.invalidateHttpSession(true)    					// 세션 무효화
						.permitAll()
		);

		http.formLogin(form -> form
				.loginPage("/login")
				.loginProcessingUrl("/loginPro")
				.defaultSuccessUrl("/")
				.usernameParameter("email")
				.passwordParameter("password")
				.successHandler(authenticationSuccessHandler())
				.permitAll()
		);

		http.userDetailsService(customerDetailService); //사용자 정의 인증 방식 (mybatis 연동)
		http.exceptionHandling(exceptions -> exceptions.accessDeniedHandler(accessDeniedHandler()));

		return http.build();
	}

	//권한접근 제어 처리
	@Bean
	public AccessDeniedHandler accessDeniedHandler(){
		return  new CustomerAccessDeniedHandler();
	}


	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}


	//암호화 방식 (BCrypt)
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	//로그인 성공시 필요한 부분 처리
	@Bean
	public AuthenticationSuccessHandler authenticationSuccessHandler(){
		return new LoginSuccessHandler();
	}

}





