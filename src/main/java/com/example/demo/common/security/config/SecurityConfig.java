package com.example.demo.common.security.config;

import com.example.demo.common.oauth.CustomOAuth2UserService;
import com.example.demo.common.security.handler.CustomerAccessDeniedHandler;
import com.example.demo.common.security.handler.LoginSuccessHandler;
import com.example.demo.common.security.handler.OAuth2AuthenticationFailureHandler;
import com.example.demo.common.security.handler.OAuth2LoginSuccessHandler;
import com.example.demo.common.security.service.CustomerDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

	private final DataSource datasource;

	private final CustomerDetailService customerDetailService;

	private final CustomOAuth2UserService customOAuth2UserService;

	private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;


	//권한처리
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		//인증 처리
		//3단계 사용자 정의 (Mybatis , jpa)
		http
				.authorizeHttpRequests(auth -> auth
				.requestMatchers("/ws/**").authenticated()	//웹소켓 경로 검증 허용
				.requestMatchers("/chat/**").authenticated()	//chat 경로 검증 허용
				.requestMatchers("/api/**").authenticated()	//알림
				.requestMatchers("/email/**").permitAll()	// 테스트용
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
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
				.failureHandler(authenticationFailureHandler())
				.permitAll()
		)
				.csrf(Customizer.withDefaults());

		// OAuth2 로그인 설정
		http.oauth2Login(oauth2 -> oauth2
				.loginPage("/login")
				.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
				.successHandler(oAuth2AuthenticationSuccessHandler())
				.failureHandler((request, response, exception) -> {
					log.error("소셜 로그인 실패: {}", exception.getMessage());
					String errorMessage = exception.getMessage();

					// 탈퇴한 회원인 경우
					// NullPointerException 방지를 위한 null 체크
					if (errorMessage != null && errorMessage.contains("탈퇴한 회원")) {
						response.sendRedirect("/login?error=deleted");
					}
					// 추가 정보 필요한 경우
					else if (request.getSession().getAttribute("requireAdditionalInfo") != null) {
						response.sendRedirect("/oauth2/signup/additional-info");
					} else {
						request.getSession().setAttribute("loginError", "소셜 로그인 중 오류가 발생했습니다.");
						response.sendRedirect("/login?error=social");
					}
				})
		);

		//예외처리
		http.exceptionHandling(exception -> exception
				.accessDeniedHandler(accessDeniedHandler())
		);

		//remember me
		http
				.rememberMe(rememberMe -> rememberMe
						.key("uniqueAndSecret")
						.tokenValiditySeconds(86400)
						.userDetailsService(customerDetailService)
						.rememberMeParameter("remember-me")
				);

		//교차 도메인 허용
		http.cors(withDefaults());

		return http.build();
	}

	@Bean
	public AuthenticationFailureHandler authenticationFailureHandler() {
		return (request, response, exception) -> {
			String errorMessage;

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

	// HiddenHttpMethodFilter 빈만 별도 등록
	@Bean
	public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
		return new HiddenHttpMethodFilter();
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

	@Bean
	public AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() { return new OAuth2LoginSuccessHandler(); }

}