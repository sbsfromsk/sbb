package com.mysite.sbb.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResetPasswordForm {

	@Size(min = 8, max = 20)
	@NotNull(message = "비밀번호를 입력해주세요.")
	@NotEmpty(message = "비밀번호를 입력해주세요.")
	private String password1;
	
	@Size(min = 8, max = 20)
	@NotNull(message = "비밀번호를 입력해주세요.")
	@NotEmpty(message = "비밀번호 확인을 입력해주세요.")
	private String password2;
}
