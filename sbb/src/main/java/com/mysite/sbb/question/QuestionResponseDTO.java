package com.mysite.sbb.question;

import java.time.LocalDateTime;

import com.mysite.sbb.user.SiteUser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponseDTO {
	private Integer id;
	private String subject;
	//private String content;
	private LocalDateTime createDate;
	private SiteUser author;
	private Long totalResponseCount;
}
