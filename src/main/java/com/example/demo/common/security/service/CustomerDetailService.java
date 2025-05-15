package com.example.demo.common.security.service;

import com.example.demo.domain.user.model.CustomerUser;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//UserDetailsService는 **"사용자 이름(username)을 받아 사용자 정보(UserDetails)를 반환하는 인터페이스"**입니다.
//인증에 대한 처리 개발자가 원하는 대로 ...UserDetailsService  재정의 여러분 마음 : mybatis , jpa , 원하는 방법 제공
// loadUserByUsername 재정의

/*
 사용자가 로그인 시도 (/login POST)
 스프링 시큐리티는 내부적으로 UserDetailsService.loadUserByUsername() 호출
 이 메서드를 통해 DB에서 사용자 정보를 가져옴
 반환된 UserDetails 객체의 비밀번호, 권한 등을 기준으로 인증 진행
 */
@Service
@RequiredArgsConstructor
public class CustomerDetailService implements UserDetailsService {

	private final UserMapper userMapper;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		Users user = userMapper.login(username);
		if(user == null) {
			throw new UsernameNotFoundException("요청하신 ID 가 없습니다 " + username);
		}

		return new CustomerUser(user);
	}

}












