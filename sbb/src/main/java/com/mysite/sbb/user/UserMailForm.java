package com.mysite.sbb.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMailForm {
	
	@NotEmpty(message= "아이디를 입력해주세요.")
	private String username;
	
	@NotEmpty(message = "이메일을 입력해주세요.")
	@Email
	private String email;
}
