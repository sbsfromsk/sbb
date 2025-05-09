package com.mysite.sbb.answer;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateTimeConverter;

import jakarta.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnswerTotalDTO {
	
	private Integer level;					
	private Integer id;
	private String content;
	private Long authorId;
	private LocalDateTime createDate;
	private LocalDateTime modifyDate;
	private Integer parentId;
	private String username;
	private Long answerVoteCount;
	private String answerAuthorPicture;
	
	public AnswerTotalDTO(Integer level, Integer id, String content, Long authorId, Timestamp createDate, Timestamp modifyDate, Integer parentId, String username, Long answerVoteCount) {
		this.level = level;
		this.id = id;
		this.content = content;
		this.authorId = authorId;
		this.createDate = (createDate != null) ? createDate.toLocalDateTime() : null;
		this.modifyDate = (modifyDate != null) ? modifyDate.toLocalDateTime() : null;
		this.parentId = parentId;
		this.username = username;
		this.answerVoteCount = answerVoteCount;
	}
}
