package com.example.demo.domain.user.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/*
Spring Security에서 UserDetails를 구현하는 것은 [사용자 정보를 직접 정의]하는 것
*/
public class CustomerUser implements UserDetails{

	//사용자 만든  DTO
	private Users user;
	
	public CustomerUser(Users user) {
		this.user = user;
	}
	
	
	//현재 로그인한 사용자의 권한(Role)정보 추출
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		return user.getAuthList()
			   .stream()   //Java Stream API 시작
			   .map((auth) -> new SimpleGrantedAuthority(auth.getAuth())) 
			   //각 권한(auth) 객체에서 문자열을 꺼내 SimpleGrantedAuthority로 변환
			   .collect(Collectors.toList());  //변환된 권한 객체들을 리스트로 수집하여 반환
			   // [0 방에 .UserAuth][1 방에 .UserAuth][UserAuth][UserAuth]
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return user.getUserPw();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return user.getUserId();
	}

	

}
