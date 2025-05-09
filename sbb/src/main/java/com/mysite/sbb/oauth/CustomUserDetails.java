package com.mysite.sbb.oauth;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;

import com.mysite.sbb.user.SiteUser;

public class CustomUserDetails implements CustomPrincipal {

	private final SiteUser siteUser;
	private final List<GrantedAuthority> authorities;
	
	public CustomUserDetails(SiteUser siteUser, List<GrantedAuthority> authorities) {
		this.siteUser = siteUser;
		this.authorities = authorities;
	}
	
	@Override
	public SiteUser getSiteUser() {
		return this.siteUser;
	}
	
	@Override
	public String getUsername() {
		return siteUser.getUsername();
	}
	
	@Override
	public String getPassword() {
		return siteUser.getPassword();
	}
	
	@Override
	public String getName() {
		return siteUser.getUsername();
	}
	
	@Override
	public Map<String, Object> getAttributes() {
		return Map.of();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}
	
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

	








}
