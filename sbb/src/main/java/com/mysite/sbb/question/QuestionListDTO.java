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
public class QuestionListDTO {

	private Integer id;
	
	private String subject;
	
	private LocalDateTime createDate;
	
	private String username;
	
	private Long totalResponseCount;
	
	private Integer boardId;
	
	private String boardName;
}
