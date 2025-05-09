package com.mysite.sbb.answer;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mysite.sbb.comment.CommentService;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/answer")
@RequiredArgsConstructor
@Controller
@Slf4j
public class AnswerController {

	private final QuestionService questionService;
	private final AnswerService answerService;
	private final CommentService commentService;
	private final UserService userService;
	
	private Integer level = 1;
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{id}")
	public String createAnswer(Model model, @PathVariable("id") Integer id
			, @Valid AnswerForm answerForm, BindingResult bindingResult, Principal principal
			, RedirectAttributes redirectAttributes
			, @RequestParam(value="page", defaultValue="0")int page, @RequestParam(value = "sort", defaultValue = "newest") String sort
	) {
		Question question = this.questionService.getQuestion(id);
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		if(bindingResult.hasErrors()) {
			//model.addAttribute("question", question);
			
			redirectAttributes.addFlashAttribute("answerErr", bindingResult.getAllErrors());
			
			//"redirect:/question/detail/%s?sort=%s&page=%s#answer_%s"
			return String.format("redirect:/question/%s/detail/%s?sort=%s&page=%s#answer_box", question.getBoardId(), question.getId(), sort, page); // answerForm도 View로 전송될 것.
		}

		Answer answer = this.answerService.create(question, answerForm.getContent(), siteUser);
		
		// sort: newest
		// page: 0페이지 (댓글이 순식간에 많이 달릴 수 있으니끼... 페이지를 찾을까?
		// :/question/detail/%s?sort=%s&page=%s#answer_%s
		
		// 여기서는 id가 question_id
		//page = this.answerService.findNewPage(answer.getQuestion().getId(), answer.getId(), sort, level);
		return String.format("redirect:/question/%s/detail/%s", question.getBoardId(), id);
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String answerModify(Model model, AnswerForm answerForm, @PathVariable("id") Integer id, Principal principal,
								@RequestParam(value="page", defaultValue="0")int page,
								@RequestParam(value = "sort", defaultValue = "newest") String sort
			) {
		Answer answer = this.answerService.getAnswer(id);
		
		if (!answer.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
		}
		
		answerForm.setContent(answer.getContent()); // answerForm 스프링 모델 객체 자동 바인딩에 의해 자동으로 생성되었음.
		
		// page, sort 분류해서!
		// 여기서 id는 answer_id를 의미 --> 아래 PostMapping에서 처리해야 함
		
		model.addAttribute("page", page);
		model.addAttribute("sort", sort);
		return "answer_form"; // answer_form.html
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String answerModify(@Valid AnswerForm answerForm, BindingResult bindingResult,
								@PathVariable("id") Integer id, Principal principal,
								@RequestParam(value="page", defaultValue="0")int page,
								@RequestParam(value = "sort", defaultValue = "newest") String sort,
								Model model) {
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			model.addAttribute("page", page);
			model.addAttribute("sort", sort);
			return "answer_form";
		}
		
		Answer answer = this.answerService.getAnswer(id);
		
		if (!answer.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
		}
		
		this.answerService.modify(answer, answerForm.getContent());
		
		page = this.answerService.findNewPage(answer.getQuestion().getId(), id, sort, level);
		
		// id: answer_id
		// page, sort는... 위에서 가져와야 하네?
		return String.format("redirect:/question/%s/detail/%s?sort=%s&page=%s#answer_%s", answer.getQuestion().getBoardId(), answer.getQuestion().getId(), sort, page, id);
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String answerDelete(Principal principal, @PathVariable("id") Integer id) {
		
		Answer answer = this.answerService.getAnswer(id);
		
		if (!answer.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
		}
		
		this.answerService.delete(answer);
		
		return String.format("redirect:/question/%s/detail/%s", answer.getQuestion().getBoardId(), answer.getQuestion().getId());
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/vote/{id}")
	public String answerVote(Principal principal, @PathVariable("id") Integer id,
							@RequestParam(value="page", defaultValue="0")int page, @RequestParam(value = "sort", defaultValue = "newest") String sort) {
		
		// 1. 추천 기능 구현하기
		Answer answer = this.answerService.getAnswer(id);
		SiteUser siteUser = this.userService.getUser(principal.getName());
		this.answerService.vote(answer, siteUser);
		
		// :/question/detail/343?sort=newest&page=0#answer_371
		// 2. question_id 구하기: pathVariable은 Answer_id임!! --> answer.getQuestion().getId()
		// 3. sort 구하기: requestParam
		// 4. page 구하기 --> NEW!!
		page = this.answerService.findNewPage(answer.getQuestion().getId(), id, sort, level);
		// :/question/detail/%s?sort=%s&page=%s#answer_%s
		// %s#answer_%s
		return String.format("redirect:/question/%s/detail/%s?sort=%s&page=%s#answer_%s", answer.getQuestion().getBoardId(), answer.getQuestion().getId(), sort, page, answer.getId());
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/deleteReplies")
	@ResponseBody
	public ResponseEntity<Map<String, String>> deleteReplies(@RequestBody Map<String, List<Integer>> body, Principal principal, BindingResult bindingResult, Model model) {
		
		List<Integer> answerIds = body.get("answerIds");
		List<Integer> commentIds = body.get("commentIds");
		
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		log.info("답글: {}", answerIds);
		log.info("댓글: {}", commentIds);
		
		try {
			if(answerIds.size() != 0) {
				this.answerService.deleteByUserId(answerIds, siteUser.getId());
			}
			
			if(commentIds.size() != 0) {
				this.commentService.deleteByUserId(commentIds, siteUser.getId());
			}
			
			return ResponseEntity.ok(Map.of("redirectUrl", "/user/replies"));
		} catch (AccessDeniedException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "알 수 없는 오류가 발생했습니다."));
		}
		
	}
}
