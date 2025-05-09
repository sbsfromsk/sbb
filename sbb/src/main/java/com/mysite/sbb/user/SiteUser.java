package com.mysite.sbb.user;

import com.mysite.sbb.oauth.LoginType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SiteUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true)
	private String username;
	
	private String password;
	
	@Column(unique = true)
	private String email;
	
	private String profilePicture;
	
	@Enumerated(EnumType.STRING)
	private LoginType loginType;
	
	public String getProfilePictureUrl() {
		return (this.profilePicture != null) ? "D:/sbb/" + profilePicture : "D:/sbb/default-profile.png";
	}
}
