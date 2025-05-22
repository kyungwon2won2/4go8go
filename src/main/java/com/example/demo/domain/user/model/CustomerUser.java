package com.example.demo.domain.user.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomerUser implements UserDetails, OAuth2User {

	private Users user;
	private Map<String, Object> attributes;

	// 일반 로그인용 생성자
	public CustomerUser(Users user) {
		this.user = user;
	}

	// OAuth2 로그인용 생성자
	public CustomerUser(Users user, Map<String, Object> attributes) {
		this.user = user;
		this.attributes = attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return user.getRoleList()
				.stream()
				.map((role) -> new SimpleGrantedAuthority(role.getRoleName()))
				.collect(Collectors.toList());
	}

	public Users getUser(){
		return user;
	}

	public int getUserId(){
		return user.getUserId();
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	public String getNickname(){ return user.getNickname(); }

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	// OAuth2User 메서드 구현
	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public String getName() {
		return user.getName() != null ? user.getName() : user.getEmail();
	}

	// 관리자 게시글 접근권한 확인용
	public boolean hasRole(String roleName){
		return getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals(roleName));
	}

}