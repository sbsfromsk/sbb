package com.mysite.sbb;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysite.sbb.answer.AnswerMainDTO;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.oauth.AuthUtil;
import com.mysite.sbb.question.QuestionListDTO;
import com.mysite.sbb.question.QuestionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

	private final MainService mainService;
	private final QuestionService questionService;
	private final AnswerService answerService;
	
	@ModelAttribute("boardList")
	public List<Board> getSideBarItems() {
		
		List<Board> boardList = mainService.getSidebarItems(1);
		return boardList;
	}
	
	@GetMapping("/")
	public String root(Model model) {
		
		AuthUtil.printCurrentUserRoles();
		
		Integer page = 0;
		String kw = "";
		
		Page<QuestionListDTO> mainPosts = this.questionService.getList2(page, kw);
		
		List<AnswerMainDTO> mainReplies = this.answerService.getRepliesNewest(page, kw);
		
		model.addAttribute("mainPosts", mainPosts);
		model.addAttribute("mainReplies", mainReplies);
		
		return "main";
	}
	

	
	
}
