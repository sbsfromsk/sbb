package com.mysite.sbb.answer;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mysite.sbb.reply.ReplyDTO;
import com.mysite.sbb.user.SiteUser;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {

	@Query("select a "
			+"from Answer a "
			+"left join a.voter av1 "
			+"where a.question.id = :id "
			+ "group by a "
			+ "order by count(av1) desc "
			  )
	Page<Answer> findByQuestionIdOrderByVoterCountDesc(@Param("id") Integer id, Pageable pageable);

	Page<Answer> findByQuestionIdOrderByCreateDateDesc(Integer id, Pageable pageable);
	
	@Query("select "
			+ "(select count(a) "
			+ "from Answer a "
			+ "where a.question.id = :id) "
			+ " + "
			+ "(select count(c) "
			+ "from Comment c "
			+ "join c.answer a "
			+ "where a.question.id = :id)")
	Integer totalCount(@Param("id") Integer id);
	
	@Query(value="with recursive total_comment (id, level, content, author_id, create_date, modify_date, parent_id) "
			+ "as (select a.id as id, "
			+ "1 as level, "
			+ "a.content, "
			+ "a.author_id, "
			+ "a.create_date, "
			+ "a.modify_date, "
			+ "a.id as parent_id "
			+ "from answer a "
			+ "where a.question_id = :id "
			+ "union all "
			+ "select c.id id, "
			+ "t.level + 1 as level, "
			+ "c.content, "
			+ "c.author_id, "
			+ "c.create_date, "
			+ "c.modify_date, "
			+ "t.parent_id "
			+ "from comment c "
			+ "join total_comment t on c.answer_id = t.id) "
			+ "select t.level, "
			+ "t.id, "
			+ "t.content, "
			+ "t.author_id, "
			+ "t.create_date,  "
			+ "t.modify_date, "
			+ "t.parent_id, "
			+ "u.username, "
			+ "count(av.answer_id) as answer_vote_count "
			+ "from total_comment  t "
			+ "left join site_user u on u.id = t.author_id "
			+ "left join answer_voter av on av.answer_id = t.parent_id and t.level = 1 "
			+ "group by t.level, t.id, t.content, t.author_id, t.create_date, t.modify_date, t.parent_id, u.username "
			+ "order by t.parent_id desc, "
			+ "level asc, "
			+ "create_date asc "
			+ "offset :offset row "
			+ "fetch first :fetch rows only ", nativeQuery = true)
	List<AnswerTotalDTO> findAllCommentsByQuestionIdOrderByCreateDateDesc(@Param("id")Integer id, @Param("offset")Integer offset, @Param("fetch")Integer fetch);
	
	
	@Query(value="with recursive total_comment (id, level, content, author_id, create_date, modify_date, parent_id) "
			+ "as (select a.id as id, "
			+ "1 as level, "
			+ "a.content, "
			+ "a.author_id, "
			+ "a.create_date, "
			+ "a.modify_date, "
			+ "a.id as parent_id "
			+ "from answer a "
			+ "where a.question_id = :id "
			+ "union all "
			+ "select c.id, "
			+ "t.level + 1 as level, "
			+ "c.content, "
			+ "c.author_id, "
			+ "c.create_date, "
			+ "c.modify_date, "
			+ "t.parent_id "
			+ "from comment c "
			+ "join total_comment t on c.answer_id = t.id) "
			+ "select t.level, "
			+ "t.id, "
			+ "t.content, "
			+ "t.author_id, "
			+ "t.create_date,  "
			+ "t.modify_date, "
			+ "t.parent_id, "
			+ "u.username, "
			+ "count(av.answer_id) as answer_vote_count "
			+ "from total_comment  t "
			+ "left join site_user u on u.id = t.author_id "
			+ "left join answer_voter av on av.answer_id = t.parent_id "
			+ "group by t.level, t.id, t.content, t.author_id, t.create_date, t.modify_date, t.parent_id, u.username "
			+ "order by answer_vote_count desc, "
			+ "parent_id desc, "
			+ "level asc, "
			+ "create_date asc "
			+ "offset :offset row "
			+ "fetch first :fetch rows only ", nativeQuery = true)
	List<AnswerTotalDTO> findAllCommentsByQuestionIdOrderByRankDesc(@Param("id")Integer id, @Param("offset")Integer offset, @Param("fetch")Integer fetch);
	
	@Query(value="with recursive total_comment (id, level, content, author_id, create_date, modify_date, parent_id) "
			+ "as (select a.id as id, "
			+ "1 as level, "
			+ "a.content, "
			+ "a.author_id, "
			+ "a.create_date, "
			+ "a.modify_date, "
			+ "a.id as parent_id "
			+ "from answer a "
			+ "where a.question_id = :id "
			+ "union all "
			+ "select c.id id, "
			+ "t.level + 1 as level, "
			+ "c.content, "
			+ "c.author_id, "
			+ "c.create_date, "
			+ "c.modify_date, "
			+ "t.parent_id "
			+ "from comment c "
			+ "join total_comment t on c.answer_id = t.id), "
			+ "ranked_total_count(level, id, content, author_id, create_date, modify_date, parent_id, username, answer_vote_count, ranked as ("
			+ "select t.level, "
			+ "t.id, "
			+ "t.content, "
			+ "t.author_id, "
			+ "t.create_date, "
			+ "t.modify_date, "
			+ "t.parent_id, "
			+ "u.username, "
			+ "count(av.asnwer_id) as answer_vote_count, "
			+ "rank() over(order by t.parent_id desc, level asc, creat_date asc) as ranked "
			+ "from total_comment t "
			+ "left join site_user u on u.id = t.author.id "
			+ "left join answer_voter av on av.answer_id = t.parent_id and t.level = 1 "
			+ "group by t.level, t.id, t.content, t.author_id, t.create_date, t.modify_date, t.parent_id, u.username "
			+ "order by ranked) "
			+ "select * from ranked_total_count "
			+ "offset (select case when ceil(rtc.ranked/10.0)-1 = -1 then 0 "
			+ "else (ceil(rtc.ranked/10.0) -1) * 10 end "
			+ "from ranked_total_count rtc "
			+ "where rtc.id = :answerId and rtc.level=1) row"
			+ "fetch first :fetch rows only ", nativeQuery = true)
	List<AnswerTotalDTO> findAllCommentsByQuestionIdOrderByCreateDateDescRedirect(@Param("id")Integer id, @Param("answerId")Integer answerId, @Param("fetch")Integer fetch);
	
	@Query(value="with recursive total_comment (id, level, author_id, create_date, parent_id) "
			+ "as (select a.id as id, "
			+ "1 as level, "
			+ "a.author_id, "
			+ "a.create_date, "
			+ "a.id as parent_id "
			+ "from answer a "
			+ "where a.question_id = :id "
			+ "union all "
			+ "select c.id id, "
			+ "t.level + 1 as level, "
			+ "c.author_id, "
			+ "c.create_date, "
			+ "t.parent_id "
			+ "from comment c "
			+ "join total_comment t on c.answer_id = t.id), "
			+ "ranked_total_count(level, id, author_id, create_date, parent_id, answer_vote_count, ranked) as ( "
			+ "select t.level, "
			+ "t.id, "
			+ "t.author_id, "
			+ "t.create_date, "
			+ "t.parent_id, "
			+ "count(av.answer_id) as answer_vote_count, "
			+ "rank() over(order by t.parent_id desc, level asc, create_date asc) as ranked "
			+ "from total_comment t "
			+ "left join answer_voter av on av.answer_id = t.parent_id and t.level = 1 "
			+ "group by t.level, t.id, t.author_id, t.create_date, t.parent_id "
			+ "order by ranked) "
			+ "select rtc.ranked from ranked_total_count rtc "
			+ "where rtc.id=:answerId and rtc.level=:level ", nativeQuery = true)
	Integer findNewPagebyCreateDate(@Param("id")Integer id, @Param("answerId") Integer answerId, @Param("level") Integer level);
	
	@Query(value="with recursive total_comment (id, level, author_id, create_date, parent_id) "
			+ "as (select a.id as id, "
			+ "1 as level, "
			+ "a.author_id, "
			+ "a.create_date, "
			+ "a.id as parent_id "
			+ "from answer a "
			+ "where a.question_id = :id "
			+ "union all "
			+ "select c.id id, "
			+ "t.level + 1 as level, "
			+ "c.author_id, "
			+ "c.create_date, "
			+ "t.parent_id "
			+ "from comment c "
			+ "join total_comment t on c.answer_id = t.id), "
			+ "ranked_total_count(level, id, author_id, create_date, parent_id, answer_vote_count, ranked) as ( "
			+ "select t.level, "
			+ "t.id, "
			+ "t.author_id, "
			+ "t.create_date, "
			+ "t.parent_id, "
			+ "count(av.answer_id) as answer_vote_count, "
			+ "rank() over(order by count(av.answer_id) desc, t.parent_id desc, level asc, create_date asc) as ranked "
			+ "from total_comment t "
			+ "left join answer_voter av on av.answer_id = t.parent_id "
			+ "group by t.level, t.id, t.author_id, t.create_date, t.parent_id "
			+ "order by ranked) "
			+ "select rtc.ranked from ranked_total_count rtc "
			+ "where rtc.id=:answerId and rtc.level=:level", nativeQuery=true)
	Integer findNewPagebyVoteCount(@Param("id")Integer id, @Param("answerId")Integer answerId, @Param("level")Integer level);

	@Query(value="select "
			+ "a.id, 1, a.content, q.subject, null, a.create_Date, a.modify_Date, q.id, q.board_Id "
			+ "from Answer a join Question q on a.question_id = q.id "
			+ "where a.author_id = :authorId "
			+ "union all "
			+ "select c.id, 2, c.content, q.subject, a.content, c.create_Date, c.modify_Date, q.id, q.board_Id "
			+ "from Comment c join Answer a on a.id = c.answer_id "
			+ "join Question q on a.question_id = q.id where c.author_id = :authorId "
			+ "order by create_date desc ", nativeQuery=true
            )
List<ReplyDTO> findRepliesByAuthor(@Param("authorId") Long authorId, Pageable pageable);
	
	@Query(value="select(select count(a) from Answer a where a.author = :author)"
			+ " + "
			+ "(select count(c) from Comment c where c.author = :author)"
			)
	Integer countRepliesByAuthor(@Param("author")SiteUser author);
	
	@Query(value="select 1 as LEVEL, "
			
			+ "q.board_Id, "
			+ "b.board_kr, "

			+ "q.id, "
			+ "q.subject, "
			+ "q.author_id, "
			+ "question_user.username, "
			+ "q.create_date, "
			
			+ "a.id, "
			+ "a.content, "
			+ "a.author_id, "
			+ "answer_user.username, "
			+ "a.create_date as reply_create_date "


			+ "from answer a "
			+ "join question q on q.id = a.question_id "
			+ "join board b on b.id = q.board_id "
			+ "join site_user answer_user on answer_user.id = a.author_id "
			+ "join site_user question_user on question_user.id = q.author_id "
			
			+ "union all "
			
			+ "select 2 as level, "
			+ "q.board_Id, "
			+ "b.board_kr, "

			+ "q.id, "
			+ "q.subject, "
			+ "c.author_id, "
			+ "question_user.username, "
			+ "q.create_date, "
			
			+ "c.id, "
			+ "c.content, "

			+ "c.author_id, "
			+ "comment_user.username, "
			+ "c.create_date as reply_create_date "
			+ "from comment c "
			+ "join answer a on a.id = c.answer_id "
			+ "join question q on q.id = a.question_id "
			+ "join board b on b.id = q.board_id "
			+ "join site_user comment_user on comment_user.id = c.author_id "
			+ "join site_user question_user on question_user.id = q.author_id "
			+ "order by reply_create_date desc", nativeQuery=true)
	List<AnswerMainDTO> getRepliesNewest(Pageable pageable);
	
	@Query(value="select "
			+ "(select count(a) from Answer a where a.question.id = :id) "
			+ "+ "
			+ "(select count(c) from Comment c join Answer a on a.id = c.answer.id where a.question.id = :id)")
	Long getTotalResponseCount(@Param("id")Integer questionId);
}
