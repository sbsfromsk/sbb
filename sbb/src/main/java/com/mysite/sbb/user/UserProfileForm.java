package com.mysite.sbb.user;

import org.springframework.web.multipart.MultipartFile;

import com.mysite.sbb.oauth.LoginType;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileForm {
	private String password1;
	
	private String password2;
	
	@NotEmpty(message = "이메일을 입력해주세요.")
	private String email;
	
	private String profilePictureUrl;  // 서버 -> 뷰
	
	private MultipartFile profilePicture; // 뷰 -> 서버
	
	private LoginType loginType;
}
