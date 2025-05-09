package com.mysite.sbb.oauth;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
		
		OAuth2User user = super.loadUser(request);
		Map<String, Object> attributes = user.getAttributes();
		
		String email = (String) attributes.get("email");
		String username = (String) attributes.get("email"); // username은 email과 같게 함
		
		SiteUser siteUser = userRepository.findByEmail(email)
				
				.orElseGet(() -> {
			SiteUser newUser = new SiteUser();
			newUser.setUsername(username);
			newUser.setEmail(email);
			newUser.setLoginType(LoginType.GOOGLE);
			return userRepository.save(newUser);
		});
		log.info("사이트 유저: {}", siteUser);
		
		if (siteUser.getLoginType() == LoginType.LOCAL) {
			
			OAuth2Error oauth2Error = new OAuth2Error(
					"duplicate_email",
					"이미 해당 이메일로 sbb에 가입한 계정이 존재합니다.",
					null);
			
			throw new OAuth2AuthenticationException(oauth2Error);
		}
		
		return new CustomOAuth2User(siteUser, attributes);
	}
}
