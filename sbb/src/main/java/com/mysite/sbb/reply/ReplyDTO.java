package com.mysite.sbb.reply;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReplyDTO {

	private Integer id;
	private Integer level;
	private String mainContent;
	private String questionSubject;
	private String answerContent;
	private LocalDateTime createDate;
	private LocalDateTime modifyDate;
	private Integer parentId;
	private Integer boardId;
	private Integer pageNumber;
	
	public ReplyDTO(Integer id, Integer level, String mainContent, String questionSubject, String answerContent,
			 Timestamp createDate, Timestamp modifyDate, Integer parentId, Integer boardId) {
		this.id = id;
		this.level = level;
		this.mainContent = mainContent;
		this.questionSubject = questionSubject;
		this.answerContent = answerContent;
		this.createDate = (createDate != null) ? createDate.toLocalDateTime() : null;
		this.modifyDate = (modifyDate != null) ? modifyDate.toLocalDateTime() : null;
		this.parentId = parentId;
		this.boardId = boardId;
	}
}