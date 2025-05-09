package com.mysite.sbb.oauth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.mysite.sbb.user.SiteUser;

public interface CustomPrincipal extends OAuth2User, UserDetails {
	SiteUser getSiteUser();
}
