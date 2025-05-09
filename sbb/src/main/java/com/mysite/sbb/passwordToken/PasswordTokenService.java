package com.mysite.sbb.passwordToken;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mysite.sbb.user.SiteUser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PasswordTokenService {

	private final PasswordTokenRepository tokenRepository;
	
	// 토큰 생성 및 저장
	public String createPasswordResetToken(SiteUser siteUser) {
		// 1. 하나의 유저는 하나의 유효한 토큰만 가져야 함
		tokenRepository.deleteBySiteUser(siteUser);
		
		// 2. 토큰 생성
		String token = UUID.randomUUID().toString();
		PasswordToken passwordToken = new PasswordToken(token, siteUser, 30);
		tokenRepository.save(passwordToken);
		
		
		return token;
		}
	
	// 토큰 검증
	public boolean isValidToken(String token) {
		return tokenRepository.findByToken(token)
				.map(t -> !t.isExpired())
				.orElse(false);
	}
	
	// 토큰으로 사용자 찾기
	public SiteUser getUserByToken(String token) {
		return tokenRepository.findByToken(token)
				.map(PasswordToken::getSiteUser)
				.orElseThrow(() -> new RuntimeException("유효하지 않은 토큰입니다."));
	}
	
	// 토큰 삭제
	public void DeleteToken(String token) {
		tokenRepository.findByToken(token).ifPresent(tokenRepository::delete);
	}
}
