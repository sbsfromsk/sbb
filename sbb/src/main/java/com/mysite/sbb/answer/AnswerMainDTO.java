package com.mysite.sbb.answer;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnswerMainDTO {

	private Integer level;
	
	private Integer boardId;
	private String boardKr;
	
	private Integer questionId;
	private String questionSubject;
	private Long questionAuthorId;
	private String questionAuthor;
	private LocalDateTime questionCreateDate;
	
	private Integer replyId;
	private String replyContent;
	private Long replyAuthorId;
	private String replyAuthor;
	private LocalDateTime replyCreateDate;
	
	private Long totalResponseCount;
	
	public AnswerMainDTO(Integer level, Integer boardId, String boardKr, Integer questionId, String questionSubject,
			Long questionAuthorId, String questionAuthor, Timestamp questionCreateDate, Integer replyId,
			String replyContent, Long replyAuthorId, String replyAuthor, Timestamp replyCreateDate) {
		this.level = level;
		this.boardId = boardId;
		this.boardKr = boardKr;
		this.questionId = questionId;
		this.questionSubject = questionSubject;
		this.questionAuthorId = questionAuthorId;
		this.questionAuthor = questionAuthor;
		this.questionCreateDate = (questionCreateDate != null ) ? questionCreateDate.toLocalDateTime() : null;
		this.replyId = replyId;
		this.replyContent = replyContent;
		this.replyAuthorId = replyAuthorId;
		this.replyAuthor = replyAuthor;
		this.replyCreateDate = (replyCreateDate != null) ? replyCreateDate.toLocalDateTime() : null;
	}
	
	
}
