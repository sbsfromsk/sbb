package com.mysite.sbb.question;

import java.awt.print.Printable;
import java.net.http.HttpRequest;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.Board;
import com.mysite.sbb.Category;
import com.mysite.sbb.MainService;
import com.mysite.sbb.answer.AnswerForm;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.answer.AnswerTotalDTO;
import com.mysite.sbb.oauth.AuthUtil;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping(value="/question")
@RequiredArgsConstructor
@Controller
@Slf4j
public class QuestionController {

	private final QuestionService questionService;
	private final UserService userService;
	private final AnswerService answerService;
	private final MainService mainService;
	
	@ModelAttribute("boardList")
	public List<Board> getSideBarItems() {
		
		List<Board> boardList = mainService.getSidebarItems(1);
		return boardList;
	}
	
	/*
	@ModelAttribute
	public void addBoardToModel(@PathVariable("boardId") Integer boardId, Model model) {
		model.addAttribute("boardId", boardId);
	}
	*/
	
	@GetMapping("/{boardId}/list")
	public String list(Model model, @PathVariable("boardId") Integer boardId, 
					   @RequestParam(value="page", defaultValue="0") int page, @RequestParam(value = "kw", defaultValue = "") String kw) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		Page<QuestionListDTO> paging;
	
		paging = this.questionService.getList2(page, kw, boardId);
		
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		
		
		stopWatch.stop();
		log.info("총 실행 시간... {} ms", stopWatch.getTotalTimeMillis());
		return "question_list";
	}
	
	@GetMapping(value = "/{boardId}/detail/{id}")
	public String detail(Model model, 
						 @PathVariable("boardId")String boardId, @PathVariable("id") Integer id, 
						 AnswerForm answerForm, 
						 @RequestParam(value= "page", defaultValue="0") int page, @RequestParam(value = "sort", defaultValue = "newest") String sort,
						 Principal principal, HttpServletRequest request, HttpSession session
						 ) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		Question question = this.questionService.getQuestion(id, sort, page);
		
		// 조회 수 증가
		// 1. 로그인 유저 정보 확인
		String loginUsername = (principal != null) ? principal.getName() : null;
		
		log.info("로그인 사용자 : {}", loginUsername);
		// 2. 본인 글일 경우 조회수 증가 x
		if (loginUsername == null || !loginUsername.equals(question.getAuthor().getUsername())) {
			log.info("what??");
		// 3. 세션 조회 기록
			String viewKey = "viewed_question_" + id;
			log.info("세션...{}", viewKey);
			if(session.getAttribute(viewKey) == null) {
				log.info("업다...");
				this.questionService.increaseViewCount(question.getId());
				session.setAttribute(viewKey, true);
			}
		}
		
		// 4. 최신 조회수로 불러오기
		question = this.questionService.getQuestion(id, sort, page);
		log.info("최종 조회수... = {}", question.getViewCount());
		
		
		// 사진 보여주기
		QuestionAuthorPictureDTO authorPicture = new QuestionAuthorPictureDTO();
		String url = "/image/profile-picture2?username=" + question.getAuthor().getUsername();
		authorPicture.setQuestionAuthorPicture(url);
		Page<AnswerTotalDTO> answerTotal = this.answerService.getTotalAnswers(id, sort, page);
		Integer answerTotalCount = this.answerService.answerTotalCount(id);
		Integer answerPageCount = this.answerService.answerPageCount(id);
		
		//Question question = this.questionService.getQuestion(id, sort);
		
		model.addAttribute("answerTotal", answerTotal);
		model.addAttribute("answerTotalCount", answerTotalCount);
		model.addAttribute("answerPageCount", answerPageCount);
		model.addAttribute("question", question);
		model.addAttribute("authorPicture", authorPicture);
		model.addAttribute("sort", sort);
		model.addAttribute("page", page);
		
		stopWatch.stop();
		log.info("총 실행 시간... {} ms", stopWatch.getTotalTimeMillis());
		return "question_detail";
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/{boardId}/create")
	public String questionCreate(QuestionForm questionForm, @PathVariable("boardId") Integer boardId) {
		return "question_form";
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/{boardId}/create")
	public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal, @PathVariable("boardId") Integer boardId, Model model) {
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			return "question_form"; //
		}
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AuthUtil.printCurrentUserRoles();
		boolean isAdmin = auth.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
		
		if (boardId == 1 && !isAdmin) {
			model.addAttribute("roleErr", "권한이 없습니다.");
			return "question_form"; //
		}
		
		SiteUser siteUser = this.userService.getUser(principal.getName());
		this.questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser, boardId);
		
		return String.format("redirect:/question/%s/list", boardId); // 질문 저장 후 질문 목록으로 이동
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/{boardId}/modify/{id}")
	public String questionModify(QuestionForm questionForm, @PathVariable("id") Integer id, Principal principal) {
		Question question = this.questionService.getQuestion(id);
		
		if(!question.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
		}
		
		questionForm.setSubject(question.getSubject());
		questionForm.setContent(question.getContent());
		
		return "question_form";
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/{boardId}/modify/{id}")
	public String questionModify(@PathVariable("boardId") Integer boardId,
			@Valid QuestionForm questionform, BindingResult bindingResult, Principal principal, @PathVariable("id") Integer id, Model model) {
		
		if(bindingResult.hasErrors()) {
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			return "question_form";
		}
		
		Question question = this.questionService.getQuestion(id);
		if (!question.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
		}
		
		this.questionService.modify(question, questionform.getSubject(), questionform.getContent());
		
		return String.format("redirect:/question/%s/detail/%s",boardId ,id);
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/{boardId}/delete/{id}")
	public String questionDelete(Principal principal,
								@PathVariable("boardId") Integer boardId,
								@PathVariable("id") Integer id) {
		
		Question question = this.questionService.getQuestion(id);
		if (!question.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
		}
		
		this.questionService.delete(question);
		
		return String.format("redirect:/question/%s/list", boardId);
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/{boardId}/vote/{id}")
	public String questionVote(@PathVariable("boardId") Integer boardId, Principal principal, @PathVariable("id") Integer id) {
		
		Question question = this.questionService.getQuestion(id);
		SiteUser siteUser = this.userService.getUser(principal.getName());
		this.questionService.vote(question, siteUser);
		
		return String.format("redirect:/question/%s/detail/%s", boardId, id);
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/deletePosts")
	@ResponseBody
	public ResponseEntity<Map<String, String>> questionDeletes(@RequestBody Map<String, List<Integer>> body, Principal principal, BindingResult bindingResult, Model model) {
		
		List<Integer> postIds = body.get("postIds");
		SiteUser siteUser = this.userService.getUser(principal.getName());
		log.info("포스트아이디: {} ", postIds);
		
		try {
			this.questionService.deleteByUserId(postIds, siteUser.getId());
			
			return ResponseEntity.ok(Map.of("redirectUrl", "/user/posts"));
		} catch (AccessDeniedException e){
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
