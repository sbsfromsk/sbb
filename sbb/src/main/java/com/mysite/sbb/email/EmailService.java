package com.mysite.sbb.email;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.mysite.sbb.passwordToken.PasswordTokenService;
import com.mysite.sbb.user.SiteUser;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender javaMailSender;
	//private final PasswordTokenService passwordTokenService;
	
	public void sendEmail(SiteUser siteUser, String resetLink) throws MessagingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
		
		helper.setTo(siteUser.getEmail());
		helper.setSubject("SBB 비밀번호 찾기");
		helper.setText(String.format("%s님, 비밀번호를 찾으세요. 하단의 링크를 클릭하세요. %n%s", siteUser.getUsername(), resetLink), true);

		javaMailSender.send(message);
	}
}
