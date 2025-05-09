package com.mysite.sbb.email;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mysite.sbb.passwordToken.PasswordTokenService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserResetPasswordForm;
import com.mysite.sbb.user.UserService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Controller
public class EmailController {

	private final EmailService emailService;
	private final PasswordTokenService passwordTokenService;
	private final UserService userService;
	
	@GetMapping("/send-email-password")
	public String sendEmailPassword(@ModelAttribute("siteUser") SiteUser siteUser) throws MessagingException {
		
		// 1. 토큰 생성 요청
		String token = this.passwordTokenService.createPasswordResetToken(siteUser);
		
		// 2. 이메일 전송 요청 (준비물: url, 토큰, 사용자)
		String resetLink = "http://localhost:8080/user/resetPassword?token=" + token;
		this.emailService.sendEmail(siteUser, resetLink);
		
		return "passwordMail_form";
	}
	

	

}
