package com.mysite.sbb.comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.user.SiteUser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CommentService {
	
	private final CommentRepository commentRepository;
	
	public Comment create(Answer answer, String content, SiteUser author) {
		Comment comment = new Comment();
		
		comment.setContent(content);
		comment.setCreateDate(LocalDateTime.now());
		comment.setAnswer(answer);
		comment.setAuthor(author);
		
		commentRepository.save(comment);
		
		return comment;
	}

	public Comment getComment(Integer id) {
		
		Optional<Comment> comment = this.commentRepository.findById(id);
		
		if(comment.isPresent()) {
			return comment.get();
		} else {
			throw new DataNotFoundException("no comment data");
		}
		
		
	}

	public void modify(Comment comment, String content) {
		comment.setContent(content);
		comment.setModifyDate(LocalDateTime.now());
		this.commentRepository.save(comment);
	}

	public void delete(Comment comment) {
		this.commentRepository.delete(comment);
		
	}

	public void deleteByUserId(List<Integer> commentIds, Long id) {
		
		// comment의 경우 answer이 삭제될 경우, 동시에 삭제되기 때문에.. error처리는 하지 않음
		for (Integer commentId : commentIds) {
			Comment comment = this.commentRepository.findById(commentId).orElse(null);
			
			if (comment != null) {
				
				if(!comment.getAuthor().getId().equals(id)) {
					throw new AccessDeniedException("본인의 게시물만 삭제할 수 있습니다.");
				}
				
				this.commentRepository.delete(comment);
			}
		}
		// TODO Auto-generated method stub
		
	}
}
