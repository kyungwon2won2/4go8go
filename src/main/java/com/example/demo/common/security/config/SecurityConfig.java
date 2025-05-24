package com.example.demo.common.security.config;

import com.example.demo.common.oauth.handler.OAuth2AuthenticationFailureHandler;
import com.example.demo.common.oauth.handler.OAuth2LoginSuccessHandler;
import com.example.demo.common.oauth.service.CustomOAuth2UserService;
import com.example.demo.common.security.handler.CustomerAccessDeniedHandler;
import com.example.demo.common.security.handler.LoginSuccessHandler;
import com.example.demo.common.security.service.CustomerDetailService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.security.config.Customizer;

import javax.sql.DataSource;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 보안 설정 클래스
 * 순환 참조 해결을 위해 생성자 주입 대신 Setter 주입 사용
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

	@Autowired
	private DataSource datasource;
	
	@Autowired
	private CustomerDetailService customerDetailService;
	
	@Setter(onMethod_ = @Autowired)
	private CustomOAuth2UserService customOAuth2UserService;
	
	@Autowired
	private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

	/**
	 * 보안 필터 체인 설정
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// 권한 설정
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/ws/**").authenticated()	//웹소켓 경로 검증 허용
				.requestMatchers("/chat/**").authenticated()	//chat 경로 검증 허용
				.requestMatchers("/api/**").authenticated()	//알림
				.requestMatchers("/email/**").permitAll()	// 테스트용
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
				.anyRequest().permitAll());

		// 로그아웃 설정
		http.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/")
				.deleteCookies("JSESSIONID")
				.invalidateHttpSession(true)
				.permitAll()
		);

		// 폼 로그인 설정
		http.formLogin(form -> form
				.loginPage("/login")
				.loginProcessingUrl("/loginPro")
				.defaultSuccessUrl("/")
				.usernameParameter("email")
				.passwordParameter("password")
				.successHandler(authenticationSuccessHandler())
				.failureHandler(authenticationFailureHandler())
				.permitAll()
		)
				.csrf(Customizer.withDefaults());

		// OAuth2 로그인 설정
		http.oauth2Login(oauth2 -> oauth2
				.loginPage("/login")
				.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
				.successHandler(oAuth2AuthenticationSuccessHandler())
				.failureHandler(oAuth2AuthenticationFailureHandler)
		);

		// 예외 처리
		http.exceptionHandling(exception -> exception
				.accessDeniedHandler(accessDeniedHandler())
		);

		// 자동 로그인 설정
		http.rememberMe(rememberMe -> rememberMe
				.key("uniqueAndSecret")
				.tokenValiditySeconds(86400)
				.userDetailsService(customerDetailService)
				.rememberMeParameter("remember-me")
		);

		// CORS 설정
		http.cors(withDefaults());

		return http.build();
	}

	/**
	 * 인증 실패 핸들러
	 */
	@Bean
	public AuthenticationFailureHandler authenticationFailureHandler() {
		return (request, response, exception) -> {
			String errorMessage;

			// 탈퇴한 회원 처리
			if (exception instanceof InternalAuthenticationServiceException &&
					exception.getMessage().contains("탈퇴한 회원")) {
				errorMessage = "탈퇴한 회원입니다.";
				request.getSession().setAttribute("loginError", errorMessage);
				response.sendRedirect("/login?error=deleted");
			} else {
				errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
				request.getSession().setAttribute("loginError", errorMessage);
				response.sendRedirect("/login?error=true");
			}
		};
	}

	/**
	 * HiddenHttpMethodFilter 등록
	 */
	@Bean
	public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
		return new HiddenHttpMethodFilter();
	}

	/**
	 * 접근 거부 핸들러
	 */
	@Bean
	public AccessDeniedHandler accessDeniedHandler(){
		return new CustomerAccessDeniedHandler();
	}

	/**
	 * 인증 매니저
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	/**
	 * 비밀번호 인코더
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * 일반 로그인 성공 핸들러
	 */
	@Bean
	public AuthenticationSuccessHandler authenticationSuccessHandler(){
		return new LoginSuccessHandler();
	}

	/**
	 * OAuth2 로그인 성공 핸들러
	 */
	@Bean
	public AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() { 
		return new OAuth2LoginSuccessHandler();
	}
}