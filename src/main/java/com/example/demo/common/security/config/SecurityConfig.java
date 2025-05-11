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
				.anyRequest().permitAll())
			.formLogin(withDefaults()) // 기본 폼 로그인 사용
			.logout(withDefaults());   // 기본 로그아웃 사용


		http.logout(logout -> logout
						.logoutUrl("/logout")           // 로그아웃 요청을 받을 URL
						.logoutSuccessUrl("/")          // 로그아웃 성공 후 이동할 URL
						.deleteCookies("JSESSIONID")    // 로그아웃 시 삭제할 쿠키 설정 (선택 사항)
						.invalidateHttpSession(true)    // 세션 무효화
				//.permitAll()
		);

		http.formLogin(form -> form
				.loginPage("/login")                            // 커스텀 로그인 페이지 요청 경로
				.loginProcessingUrl("/loginPro")                // 커스텀 로그인 처리 경로 지정
								// Controller 따로 만들 필요 없음
								// loginPro는 Security 내부적으로 필터(UsernamePasswordAuthenticationFilter)가 처리하기 때문에,
								// 이 URL에 대한 Controller를 만들 필요 없습니다.
				.defaultSuccessUrl("/")                         // 로그인 성공 시 이동할 경로
				.usernameParameter("id")                        // 사용자 이름 파라미터 설정
				.passwordParameter("pw")                        // 패스워드 파라미터 설정
				.successHandler(authenticationSuccessHandler()) // 성공 시 핸들러 설정
				.permitAll()                                    // 모든 사용자에게 로그인 페이지 접근 허용

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





