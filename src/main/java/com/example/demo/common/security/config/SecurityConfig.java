package com.example.demo.common.security.config;

import com.example.demo.common.oauth.handler.OAuth2AuthenticationFailureHandler;
import com.example.demo.common.oauth.handler.OAuth2LoginSuccessHandler;
import com.example.demo.common.oauth.service.CustomOAuth2UserService;
import com.example.demo.common.security.handler.CustomerAccessDeniedHandler;
import com.example.demo.common.security.handler.LoginSuccessHandler;
import com.example.demo.common.security.service.CustomerDetailService;
import com.example.demo.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
 */
@Configuration
@EnableWebSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

	private final DataSource datasource;
	private final CustomerDetailService customerDetailService;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
	private final UserService userService; // UserService 추가

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
				.requestMatchers("/account/recover").permitAll() // 계정 복구 페이지 접근 허용
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
	 * 인증 실패 핸들러 - 소셜 로그인과 동일한 복구 처리 로직 적용
	 */
	@Bean
	public AuthenticationFailureHandler authenticationFailureHandler() {
		return new AuthenticationFailureHandler() {
		    @Override
		    public void onAuthenticationFailure(HttpServletRequest request, 
		                                        HttpServletResponse response, 
		                                        AuthenticationException exception) throws java.io.IOException, jakarta.servlet.ServletException {
		        log.info("일반 로그인 인증 실패: {}", exception.getMessage());
		        
		        // 이메일 값 가져오기
		        String email = request.getParameter("email");
		        log.info("로그인 시도 이메일: {}", email);

		        // 탈퇴한 회원 처리 - 소셜 로그인과 동일한 로직 적용
		        if (exception instanceof InternalAuthenticationServiceException &&
		                exception.getMessage().contains("탈퇴한 회원")) {
		                
		            log.info("탈퇴한 회원 로그인 시도 - 이메일: {}", email);
		            
		            // 복구 가능 여부 확인 (소셜 로그인과 동일)
		            if (email != null && userService.isAccountRecoverable(email)) {
		                log.info("복구 가능한 계정 - 복구 페이지로 리다이렉트: {}", email);
		                response.sendRedirect("/account/recover?email=" + email);
		                return;
		            }
		            
		            // 복구 불가능한 경우
		            log.warn("복구 불가능한 계정 - 로그인 페이지로 리다이렉트: {}", email);
		            request.getSession().setAttribute("loginError", "탈퇴한 회원입니다.");
		            response.sendRedirect("/login?error=deleted");
		            
		        } else {
		            // 일반적인 로그인 실패
		            String errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
		            request.getSession().setAttribute("loginError", errorMessage);
		            response.sendRedirect("/login?error=true");
		        }
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