package com.mysite.sbb.question;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.mysite.sbb.user.SiteUser;

public interface QuestionRepository extends JpaRepository<Question, Integer>{
	Question findBySubject(String subject);
	Question findBySubjectAndContent(String subject, String content);
	List<Question> findBySubjectLike(String subject);
	List<Question> findBySubjectLikeOrderByIdAsc(String subject);
	Page<Question> findAll(Pageable pageable);
	Page<Question> findAll(Specification<Question> spec, Pageable pageable);
	
	@Query("select "
			+ "distinct q "
			+ "from Question q "
			+ "left outer join SiteUser u1 on q.author = u1 "
			+ "left outer join Answer a on a.question = q "
			+ "left outer join SiteUser u2 on a.author = u2 "
			+ "where "
			+ "	q.subject like %:kw% "
			+ " or q.content like %:kw% "
			+ " or u1.username like %:kw% "
			+ " or a.content like %:kw% "
			+ " or u2.username like %:kw% "
			)
	Page<Question> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
	
	@Query("select new com.mysite.sbb.question.QuestionListDTO ( "
			+ "q.id, "
			+ "q.subject, "
			+ "q.createDate, "
			+ "q.author.username, "
			+ "(select count(a) from Answer a where q=a.question) "
			+ "+ (select count(c) from Comment c where c.answer in (select a from Answer a where a.question = q)), "
			+ "q.boardId, "
			+ "(select b.boardKr from Board b where b.id = q.boardId) as boardName) "
			+ "from Question q "
			+ "left join SiteUser u1 on q.author = u1 "
			+ "left join Answer a on a.question = q "
			+ "left join SiteUser u2 on a.author = u2 "
			+ "left join Comment c on c.answer = a "
			+ "left join SiteUser u3 on c.author = u3 "
			+ "where ("
			+ "q.subject like %:kw% "
			+ "or q.content like %:kw% "
			+ "or u1.username like %:kw% "
			+ "or a.content like %:kw% "
			+ "or u2.username like %:kw% "
			+ "or c.content like %:kw% "
			+ "or u3.username like %:kw%) "
			+ "and q.boardId = :boardId "
			+ "group by q.id, q.subject, q.createDate, q.author.id, q.boardId"
			)
	Page<QuestionListDTO> findAllByKeyword2(@Param("kw") String kw, Pageable pageable, @Param("boardId") Integer boardId);
	
	@Query("select new com.mysite.sbb.question.QuestionListDTO ( "
			+ "q.id, "
			+ "q.subject, "
			+ "q.createDate, "
			+ "q.author.username, "
			+ "(select count(a) from Answer a where q=a.question) "
			+ " + (select count(c) from Comment c where c.answer in (select a from Answer a where a.question = q)), "
			+ "q.boardId, "
			+ "(select b.boardKr from Board b where b.id = q.boardId) as boardName) "
			+ "from Question q "
			+ "left join SiteUser u1 on q.author = u1 "
			+ "left join Answer a on a.question = q "
			+ "left join SiteUser u2 on a.author = u2 "
			+ "left join Comment c on c.answer = a "
			+ "left join SiteUser u3 on c.author = u3 "
			+ "where "
			+ "q.subject like %:kw% "
			+ "or q.content like %:kw% "
			+ "or u1.username like %:kw% "
			+ "or a.content like %:kw% "
			+ "or u2.username like %:kw% "
			+ "or c.content like %:kw% "
			+ "or u3.username like %:kw% "
			+ "group by q.id, q.subject, q.createDate, q.author.id, q.boardId"
			)
	Page<QuestionListDTO> findAllByKeyword2(@Param("kw") String kw, Pageable pageable);
	
	Page<Question> findAllByAuthor(SiteUser author, Pageable pageable);
	
	@Modifying
	@Transactional
	@Query("update Question q set q.viewCount = q.viewCount + 1 where q.id = :id")
	void increaseViewCount(@Param("id") Integer id);
}
