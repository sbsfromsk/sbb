package com.mysite.sbb.question;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerRepository;
import com.mysite.sbb.user.SiteUser;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class QuestionService {
	
	private final QuestionRepository questionRepository;
	private final AnswerRepository answerRepository;
	
	@PersistenceContext
	private EntityManager em;
	
	private Specification<Question> search(String kw) {
		return new Specification<>() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
				query.distinct(true); // 중복을 제거
				Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
				Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
				Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
				
				return cb.or(cb.like(q.get("subject"), "%" + kw + "%"), // 제목
						cb.like(q.get("content"), "%" + kw + "%"), // 내용
						cb.like(u1.get("username"), "%" + kw + "%"), // 질문 작성자
						cb.like(a.get("content"), "%" + kw + "%"), // 답변 내용
						cb.like(u2.get("username"), "%" + kw + "%")); // 답변 작성자
			}
		};
	}
	
	public Page<Question> getList(int page, String kw) {
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate")); // 정렬기준...
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		
		return this.questionRepository.findAllByKeyword(kw, pageable);
	}

	public Page<QuestionListDTO> getList2(int page, String kw) {
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate")); // 정렬기준...
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		
		//this.questionRepository.findAllByKeyword2(kw, pageable);
		/*
		Page<QuestionResponseDTO> question2 = question1.map(question -> {
		QuestionResponseDTO questionDTO = new QuestionResponseDTO();
		questionDTO.setId(question.getId());
		questionDTO.setSubject(question.getSubject());
		questionDTO.setAuthor(question.getAuthor());
		questionDTO.setCreateDate(question.getCreateDate());
		
		int answerCount = question.getAnswerList().size();
		int commentCount = question.getAnswerList().stream().mapToInt(answer -> answer.getCommentList().size()).sum();
		
		questionDTO.setTotalResponseCount(answerCount + commentCount);
		
		return null;
		
		
		});
		*/
		return this.questionRepository.findAllByKeyword2(kw, pageable);
	}
	
	public Page<QuestionListDTO> getList2(int page, String kw, Integer boardId) {
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate")); // 정렬기준...
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		
		//this.questionRepository.findAllByKeyword2(kw, pageable);
		/*
		Page<QuestionResponseDTO> question2 = question1.map(question -> {
		QuestionResponseDTO questionDTO = new QuestionResponseDTO();
		questionDTO.setId(question.getId());
		questionDTO.setSubject(question.getSubject());
		questionDTO.setAuthor(question.getAuthor());
		questionDTO.setCreateDate(question.getCreateDate());
		
		int answerCount = question.getAnswerList().size();
		int commentCount = question.getAnswerList().stream().mapToInt(answer -> answer.getCommentList().size()).sum();
		
		questionDTO.setTotalResponseCount(answerCount + commentCount);
		
		return null;
		
		
		});
		*/
		return this.questionRepository.findAllByKeyword2(kw, pageable, boardId);
	}
	
	/*
	public List<Question> getList() {
		return this.questionRepository.findAll();
		
		자바에서 리턴값이 다르다면, 메서드 오버라이딩을 할 수 없다!
	}
	*/
	
	public Question getQuestion(Integer id) {
		Optional<Question> question = this.questionRepository.findById(id);
		
		if (question.isPresent()) {
			return question.get();
		} else {
			throw new DataNotFoundException("question not found");	    
		}
	}
	
	public Page<Question> getQuestionsBySiteUser(SiteUser siteUser, Integer page) {
		
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate"));
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		
		return this.questionRepository.findAllByAuthor(siteUser, pageable);
	}
	
	public Question getQuestion(Integer id, String sort, int page) {
		Optional<Question> question = this.questionRepository.findById(id);
		
		if (question.isPresent()) {
			
			Question q = question.get();
			/*
			Page<Answer> sortedAnswers;
			Pageable pageable;
			List<Sort.Order> sorts = new ArrayList<>();
			
			if("ranking".equals(sort)) {
				pageable = PageRequest.of(page, 10);
				sortedAnswers = this.answerRepository.findByQuestionIdOrderByVoterCountDesc(q.getId(), pageable);
			} else { // default
				sorts.add(Sort.Order.desc("createDate"));
				pageable = PageRequest.of(page, 10);
				sortedAnswers = this.answerRepository.findByQuestionIdOrderByCreateDateDesc(q.getId(), pageable);
			}
			
			q.setAnswerList(sortedAnswers.getContent());
			*/
			
	
			
			
			return q;
		} else {
			throw new DataNotFoundException("question not found");	    
		}
	}
	
	@Transactional
	public void increaseViewCount(Integer id) {
		
		
		log.info("조회수 중가: questionId = {}", id);
		
		this.questionRepository.increaseViewCount(id);
		em.flush();
		em.clear();
		
		log.info("조회수 중가 완료: questionId = {}", id);
	}

	public void create(String subject, String content, SiteUser user, Integer boardId) {
		Question q = new Question();
		q.setSubject(subject);
		q.setContent(content);
		q.setCreateDate(LocalDateTime.now());
		q.setAuthor(user);
		q.setBoardId(boardId);
		this.questionRepository.save(q);
	}
	
	public void modify(Question question, String subject, String content) {
		question.setSubject(subject);
		question.setContent(content);
		question.setModifyDate(LocalDateTime.now());
		this.questionRepository.save(question);
	}
	
	public void delete(Question question) {
		this.questionRepository.delete(question);
	}
	
	public void vote(Question question, SiteUser siteUser) {
		question.getVoter().add(siteUser);
		
		this.questionRepository.save(question);
	}

	public void deleteByUserId(List<Integer> postIds, Long id) {
		
		for (Integer postId  : postIds) {
			Question post = this.questionRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시물 없음"));
			
			if(!post.getAuthor().getId().equals(id)) {
				throw new AccessDeniedException("본인의 게시물만 삭제할 수 있습니다.");
			}
			
			this.questionRepository.delete(post);
		}
		
		
		
	}
	
	/*
	public Page<QuestionListDTO> getFromAllBoards(Integer page, String kw) {
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate")); // 정렬기준...
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		
		return this.questionRepository.findAllByKeyword2(kw, pageable);
		
	}
	*/
}
