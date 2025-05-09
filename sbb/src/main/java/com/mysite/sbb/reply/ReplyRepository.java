package com.mysite.sbb.reply;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sbb.answer.Answer;

public interface ReplyRepository extends JpaRepository<Answer, Integer> {

}
