package com.mysite.sbb.oauth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {

	public static void printCurrentUserRoles() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication != null && authentication.isAuthenticated()) {
			System.out.println("현재 사용자 권한 목록: ");
			for (GrantedAuthority authority : authentication.getAuthorities()) {
				System.out.println("- " + authority.getAuthority());
			}
		} else {
			System.out.println("로그인된 사용자가 없습니다.");
		}
	}
}
