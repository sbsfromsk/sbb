package com.mysite.sbb.oauth;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.mysite.sbb.user.SiteUser;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public class CustomOAuth2User implements CustomPrincipal {

	private final SiteUser siteUser;
	private final Map<String, Object> attributes;
	
    public CustomOAuth2User(SiteUser siteUser, Map<String, Object> attributes) {
        this.siteUser = siteUser;
        this.attributes = attributes;
    }
	
    
    @Override
	public SiteUser getSiteUser() {
		return this.siteUser;
	}    
    
	@Override
	public String getName() {
		return siteUser.getUsername();
	}
	
	@Override
	public String getUsername() {
		return siteUser.getUsername();
	}
	
	@Override
	public String getPassword() {
		return "";
	}
	
	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of();
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
