package com.mysite.sbb.passwordToken;

import java.time.LocalDateTime;

import com.mysite.sbb.user.SiteUser;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PasswordToken {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;
	
	@Column(nullable = false, unique = true)
	private String token;
	
	@OneToOne
	private SiteUser siteUser;
	
	@Column(nullable = false)
	private LocalDateTime expiryDate;
	
	public PasswordToken(String token, SiteUser siteUser, int expiryMinutes) {
		this.token = token;
		this.siteUser = siteUser;
		this.expiryDate = LocalDateTime.now().plusMinutes(expiryMinutes);
	}
	
	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiryDate);
	}
}
