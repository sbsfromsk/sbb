package com.mysite.sbb.answer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.reply.ReplyDTO;
import com.mysite.sbb.user.SiteUser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AnswerService {

	private final AnswerRepository answerRepository;
	
	public Answer create(Question question, String content, SiteUser author) {
		Answer answer = new Answer();
		
		answer.setContent(content);
		
		answer.setCreateDate(LocalDateTime.now());
		
		answer.setQuestion(question);
		
		answer.setAuthor(author);
		
		this.answerRepository.save(answer);
		
		return answer;
	}
	
	public Answer getAnswer(Integer id) {
		Optional<Answer> answer = this.answerRepository.findById(id);
		
		if (answer.isPresent()) {
			return answer.get();
		} else {
			throw new DataNotFoundException("answer not found");
		}
	}
	
	public void modify(Answer answer, String content) {
		answer.setContent(content);
		answer.setModifyDate(LocalDateTime.now());
		this.answerRepository.save(answer);
	}
	
	public void delete(Answer answer) {
		this.answerRepository.delete(answer);
	}
	
	public void vote(Answer answer, SiteUser siteUser) {
		answer.getVoter().add(siteUser);
		this.answerRepository.save(answer);
	}

	public Integer answerTotalCount(Integer id) {
		return this.answerRepository.totalCount(id);
	}

	public Integer answerPageCount(Integer id) {
		Integer answerTotalCount = answerTotalCount(id);
		Integer answerPageCount;
		
		answerPageCount = Math.max(0, (int) (Math.ceil(answerTotalCount / 10.0) - 1));
		return answerPageCount;
	}

	public Page<AnswerTotalDTO> getTotalAnswers(Integer id, String sort, int page) {
		
		List<AnswerTotalDTO> sortedAnswers;
		
		Integer offset = page * 10;
		Integer fetch = 10;
		
		if("ranking".equals(sort)) {
			sortedAnswers = this.answerRepository.findAllCommentsByQuestionIdOrderByRankDesc(id, offset, fetch);
		} else { // default
			sortedAnswers = this.answerRepository.findAllCommentsByQuestionIdOrderByCreateDateDesc(id, offset, fetch);
		}
		
		Integer total = answerTotalCount(id);
		
		
		
		sortedAnswers.stream().map(answer ->  {
			String url = "/image/profile-picture2?username=" + answer.getUsername();
			answer.setAnswerAuthorPicture(url);
			return answer;}
		).collect(Collectors.toList());
		
		return new PageImpl<>(sortedAnswers, PageRequest.of(page, 10), total);
	}

	public Integer findNewPage(Integer id, Integer answerId, String sort, Integer level) {
		
		Integer rank;
		
		if ("ranking".equals(sort)) {
			rank = this.answerRepository.findNewPagebyVoteCount(id, answerId, level);
		} else { // default
			rank = this.answerRepository.findNewPagebyCreateDate(id, answerId, level);
		}
		
		Integer newPage = Math.max(0, (int) (Math.ceil(rank / 10.0) -1));
		
		return newPage;
	}

	public Page<ReplyDTO> getTotalAnswersById(SiteUser siteUser, Integer page) {
		List<Sort.Order> sorts = new ArrayList<>();
		//sorts.add(Sort.Order.desc("create_date"));
		Pageable pageable = PageRequest.of(page, 10);
		
		// TODO Auto-generated method stub
		
		List<ReplyDTO> replies = this.answerRepository.findRepliesByAuthor(siteUser.getId(), pageable);
		
		/*
		Page<ReplyDTO> updatedReplies = replies.map(reply -> {
				Integer newPage = this.findNewPage(reply.getParentId(), reply.getId(), null, reply.getLevel());
				reply.setPageNumber(newPage);
				
				return reply;
		});
		*/
		
		List<ReplyDTO> updatedReplies = replies.stream().map(reply -> {
			Integer newPage = this.findNewPage(reply.getParentId(), reply.getId(), null, reply.getLevel());
			reply.setPageNumber(newPage);
			
			return reply;
	}).collect(Collectors.toList());
		
		Integer total = countRepliesByAuthor(siteUser);
		
		return new PageImpl<>(updatedReplies, PageRequest.of(page, 10), total);
	}

	public Integer countRepliesByAuthor(SiteUser siteUser) {
		
		Integer total = this.answerRepository.countRepliesByAuthor(siteUser);
		
		return total;
	}

	public void deleteByUserId(List<Integer> answerIds, Long id) {
		
		for (Integer answerId : answerIds) {
			Answer answer = this.answerRepository.findById(answerId).orElseThrow(() -> new IllegalArgumentException("게시물 없음"));
			
			if(!answer.getAuthor().getId().equals(id)) {
				throw new AccessDeniedException("본인의 게시물만 삭제할 수 있습니다.");
			}
			
			this.answerRepository.delete(answer);
		}
		
	}

	public List<AnswerMainDTO> getRepliesNewest(Integer page, String kw) {
		List<AnswerMainDTO> sortedAnswers = null;
	
		
		Pageable pageable = PageRequest.of(page, 10);
		
		sortedAnswers = this.answerRepository.getRepliesNewest(pageable);
		
		List<AnswerMainDTO> updatedSortedAnswers = sortedAnswers.stream().map(answer -> {
				Long totalResponseCount = this.answerRepository.getTotalResponseCount(answer.getQuestionId());
				
				answer.setTotalResponseCount(totalResponseCount);
			return answer;
		}).collect(Collectors.toList());
		return updatedSortedAnswers; 
	}

}
