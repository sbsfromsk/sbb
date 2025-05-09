package com.mysite.sbb.passwordToken;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.mysite.sbb.user.SiteUser;

import jakarta.transaction.Transactional;

public interface PasswordTokenRepository extends JpaRepository<PasswordToken, Long>{
	
	Optional<PasswordToken> findByToken(String token);
	
	@Modifying
	@Transactional
	void deleteBySiteUser(SiteUser user);
	

}
