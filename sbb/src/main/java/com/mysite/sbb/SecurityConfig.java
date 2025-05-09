package com.mysite.sbb;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.mysite.sbb.oauth.CustomOAuth2UserService;
import com.mysite.sbb.oauth.CustomUserDetailsService;
import com.mysite.sbb.oauth.CustomizeAuthenticationFailureHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig{

	private final CustomOAuth2UserService customOAuth2UserService;
	private final CustomUserDetailsService customUserDetailsService;
	private final CustomizeAuthenticationFailureHandler customizeAuthenticationFailureHandler;
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		
		http
			.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
					.requestMatchers(new AntPathRequestMatcher("/**"))
					.permitAll()
					.anyRequest().authenticated())
			.csrf((csrf) -> csrf
					.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
					)
			.headers((headers) -> headers
					.addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter
							.XFrameOptionsMode.SAMEORIGIN)))
			.formLogin((formLogin) -> 
					formLogin
					.loginPage("/user/login")
					.loginProcessingUrl("/user/do-login")
					.defaultSuccessUrl("/")
					.failureHandler(customizeAuthenticationFailureHandler)
					.permitAll()
					)
			.logout((logout) -> logout
					.logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
					.logoutSuccessUrl("/")
					.invalidateHttpSession(true))
			.oauth2Login((oauth2) -> oauth2
					.loginPage("/user/google/login")
					.userInfoEndpoint((userInfo) -> userInfo
							.userService(customOAuth2UserService)
							)
					.defaultSuccessUrl("/", true)
					.failureHandler(customizeAuthenticationFailureHandler)
					)
			;
		return http.build();
	}
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	/*
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		
		return 
		// return authenticationConfiguration.getAuthenticationManager();
	}
	*/
	
	@Bean
	AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		
		AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
		
		authManagerBuilder.parentAuthenticationManager(null);
		authManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
		
		log.info("왓!?");
		return authManagerBuilder.build();
	}
}
