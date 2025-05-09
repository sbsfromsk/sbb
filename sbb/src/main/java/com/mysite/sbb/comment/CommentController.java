package com.mysite.sbb.comment;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerForm;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/comment")
@Controller
@RequiredArgsConstructor
public class CommentController {
	
	private final AnswerService answerService;
	private final CommentService commentService;
	private final UserService userService;

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{id}")
	public String createComment(Model model, @PathVariable("id") Integer id
			, @Valid AnswerForm answerForm, BindingResult bindingResult, Principal principal
			, RedirectAttributes redirectAttributes
			, @RequestParam(value="page", defaultValue="0")int page, @RequestParam(value = "sort", defaultValue = "newest") String sort) {
		
		Answer answer = this.answerService.getAnswer(id);
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		Integer level;
		
		if (bindingResult.hasErrors()) {
			
			redirectAttributes.addFlashAttribute("commentErr", bindingResult.getAllErrors());
			redirectAttributes.addFlashAttribute("targetId", id);
			
			level = 1;
			
			page = this.answerService.findNewPage(answer.getQuestion().getId(), answer.getId(), sort, level);
			
			return String.format("redirect:/question/%s/detail/%s?sort=%s&page=%s#answer_%s", answer.getQuestion().getBoardId(), answer.getQuestion().getId(), sort, page, id);
		}
		
		Comment comment = commentService.create(answer, answerForm.getContent(), siteUser);
		
		// :/question/detail/%s?sort=%s&page=%s#answer_%s
		/*
		 * 		page = this.answerService.findNewPage(answer.getQuestion().getId(), answer.getId(), sort);
		return String.format("redirect:/question/detail/%s?sort=%s&page=%s#answer_%s", id, sort, page, answer.getId());
		 * 
		 */
		
		level = 2;
		page = this.answerService.findNewPage(answer.getQuestion().getId(), comment.getId(), sort, level);
		return String.format("redirect:/question/%s/detail/%s?sort=%s&page=%s#comment_%s", answer.getQuestion().getBoardId(), answer.getQuestion().getId(), sort, page, comment.getId());
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String commentModify(Model model, AnswerForm answerForm,
								@PathVariable("id") Integer id, Principal principal,
								@RequestParam(value="page", defaultValue="0") int page, 
								@RequestParam(value="sort", defaultValue="newest") String sort
			) {
		
		Comment comment = this.commentService.getComment(id);
		
		if (!comment.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
		}
		
		answerForm.setContent(comment.getContent());
		
		model.addAttribute("page", page);
		model.addAttribute("sort", sort);
		return "answer_form";
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String commentModify(@Valid AnswerForm answerForm, BindingResult bindingResult,
								@PathVariable("id") Integer id, Principal principal,
								@RequestParam(value="page", defaultValue ="0") Integer page,
								@RequestParam(value="sort", defaultValue = "newest") String sort,
								Model model) {
		
		if(bindingResult.hasErrors()) {
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			model.addAttribute("page", page);
			model.addAttribute("sort", sort);
			
			return "answer_form";
		}
		
		Comment comment = this.commentService.getComment(id);
		
		if (!comment.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
		}
		
		this.commentService.modify(comment, answerForm.getContent());
		
		Integer level = 2;
		
		page = this.answerService.findNewPage(comment.getAnswer().getQuestion().getId(), comment.getId(), sort, level);
		
		return String.format("redirect:/question/%s/detail/%s?sort=%s&page=%s#comment_%s",comment.getAnswer().getQuestion().getBoardId(), comment.getAnswer().getQuestion().getId(), sort, page, id);
	}
	
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String commentDelete(Principal principal, @PathVariable("id") Integer id) {
		
		Comment comment = this.commentService.getComment(id);
		
		if(!comment.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
		}
		
		this.commentService.delete(comment);
		
		return String.format("redirect:/question/%s/detail/%s", comment.getAnswer().getQuestion().getBoardId(), comment.getAnswer().getQuestion().getId());
	}
}
