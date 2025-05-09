package com.mysite.sbb.user;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.query.NativeQuery.ReturnableResultNode;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mysite.sbb.Board;
import com.mysite.sbb.Category;
import com.mysite.sbb.MainService;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.answer.AnswerTotalDTO;
import com.mysite.sbb.image.ImageService;
import com.mysite.sbb.oauth.CustomOAuth2User;
import com.mysite.sbb.oauth.CustomOAuth2UserService;
import com.mysite.sbb.passwordToken.PasswordTokenService;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.reply.ReplyDTO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

	private final UserService userService;
	private final MainService mainService;
	private final PasswordTokenService passwordTokenService;
	private final QuestionService questionService;
	private final AnswerService answerService;
	private final ImageService imageService;
	
	@ModelAttribute("boardList")
	public List<Board> getSideBarItems() {
		
		List<Board> boardList = mainService.getSidebarItems(2);
		return boardList;
	}
	
	@GetMapping("/signup")
	public String signup(UserCreateForm userCreateForm) {
		
		return "signup_form";
	}
	
	@PostMapping("/signup")
	public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult, Model model) {
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			return "signup_form";
		}
		
		if(!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
			bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 비밀번호가 일치하지 않습니다.");
			
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			
			return "signup_form";
		}
		
		try {
			userService.create(userCreateForm.getUserName(), userCreateForm.getEmail(), userCreateForm.getPassword1());
		} catch(DataIntegrityViolationException e) {
			e.printStackTrace();
			bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
			
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			
			return "signup_form";
		} catch(Exception e) {
			e.printStackTrace();
			bindingResult.reject("sigupFailed", "e.getMessage()");
			
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			
			return "signup_form";
		}
		
		
		return "redirect:/";
	}
	
	@GetMapping("/login")
	public String login() {
		return "login_form";
	}
	
	@GetMapping("/findPassword")
	public String findPassword(UserMailForm userMailForm) {
		return "password_form";
	}
	
	@PostMapping("/findPassword")
	public String findPassword(@Valid UserMailForm userMailForm, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			return "password_form";
		}
		
		try {
			
			SiteUser siteUser = this.userService.getUser(userMailForm.getUsername());
			
			if (!siteUser.getEmail().equals(userMailForm.getEmail())) {
				bindingResult.reject("findPasswordFailed", "일치하는 사용자가 없습니다.");
				model.addAttribute("answerErr", bindingResult.getAllErrors());
				
				return "password_form";
				
				
			}
			
			redirectAttributes.addFlashAttribute("siteUser", siteUser);
			return "redirect:/send-email-password"; // 성공 시
			
		} catch(Exception e) {
			e.printStackTrace();
			bindingResult.reject("findPasswordFailed", e.getMessage());
			
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			
			return "password_form";
			
		}
	}
	
	@GetMapping("/resetPassword")
	public String resetPassword(@RequestParam("token") String token, Model model,
								HttpSession session) {
		if(!passwordTokenService.isValidToken(token)) {
			model.addAttribute("error", "유효하지 않은 토큰입니다.");
			
			return "/error/page_error";
		}
		
		session.setAttribute("token", token);
		
		return "/password/resetPassword";                                                                                                                                                                                                                                                                                                                                                                         
		
	}
	
	@PostMapping("/resetPassword")
	public String resetPassword(Model model, 
								RedirectAttributes redirectAttributes,
								HttpSession session,
								@Valid UserResetPasswordForm userResetpasswordForm,
								BindingResult bindingResult) {
		
		String token = session.getAttribute("token").toString();
		
		if(!passwordTokenService.isValidToken(token)) {
			return "/error/page_error";
		}
		
		if(bindingResult.hasErrors()) {
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			model.addAttribute("token", token);
			return "/password/resetPassword";
		}
		
		if(!userResetpasswordForm.getPassword1().equals(userResetpasswordForm.getPassword2())) {
			bindingResult.reject("passwordError", "비밀번호가 일치하지 않습니다.");
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			model.addAttribute("token", token);
			return "/password/resetPassword";
		}
		
		try {
			
			SiteUser siteUser = this.passwordTokenService.getUserByToken(token);
			
			this.userService.changePassword(siteUser, userResetpasswordForm.getPassword1(), token);
			
			return "redirect:/";
			
		} catch(Exception e) {
			e.printStackTrace();
			bindingResult.reject("resetPasswordFailed", e.getMessage());
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			
			return "/password/resetPassword";
		}
		
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/profile")
	public String userProfile(UserProfileForm userProfileForm, Principal principal) {
		log.info("프린시팔? {}", principal);
		log.info("사용자의 아이디: {}", principal.getName());
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		userProfileForm.setEmail(siteUser.getEmail());
		userProfileForm.setLoginType(siteUser.getLoginType());
		log.info("사용자의 로그인타입 : {}", userProfileForm.getLoginType());
		log.info("여기여기여기");

		userProfileForm.setProfilePictureUrl("/image/profile-picture");
		log.info("여기여기33: {}", userProfileForm.getProfilePictureUrl());
		return "/user/profile";
	}
	

	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/profile")
	public String userProfile(@Valid UserProfileForm userProfileForm, BindingResult bindingResult,
							  Model model, Principal principal, RedirectAttributes redirectAttributes) {
		
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		if(bindingResult.hasErrors()) {
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			return "/user/profile";
		}
		
		if(!userProfileForm.getPassword1().equals(userProfileForm.getPassword2())) {
			bindingResult.reject("password2", "2개의 비밀번호가 일치하지 않습니다.");
			
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			
			return "/user/profile";
		}
		
		try {
			
			if(!userProfileForm.getPassword1().isEmpty()) {
				
				
				// 두 개로 나누는 게 좋을듯 ...
				
				this.userService.setPasswordEmail(userProfileForm.getPassword1(), userProfileForm.getEmail(), principal.getName());
				

			} 
			
			if (!userProfileForm.getEmail().equals(siteUser.getEmail())) {
				
				this.userService.setEmail(userProfileForm.getEmail(), principal.getName());
				
			}
			
			if(!userProfileForm.getProfilePicture().isEmpty()) {  // 사진에 대한 원본 이름, 변경 후 이름
				
				
				// input 값이 null이면 사진 변경하지 않은 것, input 값이 ???로 설정되면 사진을 내린 것...

				this.imageService.saveImage(siteUser, userProfileForm.getProfilePicture());
				
				
			}
			
			redirectAttributes.addFlashAttribute("message", "수정 완료!");
			return "redirect:/user/profile";
		} catch(Exception e) {
			e.printStackTrace();
			bindingResult.reject("noUser", e.getMessage());
			model.addAttribute("answerErr", bindingResult.getAllErrors());
			
			return "/user/profile";
		}
		
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/posts")
	public String userPosts(@RequestParam(value="page", defaultValue = "0") Integer page, Principal principal, Model model) {
		
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		Page<Question> userPosts = this.questionService.getQuestionsBySiteUser(siteUser, page);
		
		model.addAttribute("posts", userPosts);
		
		return "/user/posts";
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/replies")
	public String userReplies(@RequestParam(value="page", defaultValue= "0") Integer page, Principal principal, Model model) {
		
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		Page<ReplyDTO> replies = this.answerService.getTotalAnswersById(siteUser, page);
		
		model.addAttribute("replies", replies);
		
		return "/user/replies";
	}
	
	/*
	@GetMapping("/profile-picture")
	public ResponseEntity<Resource> getProfilePicture(Principal principal) {
		
		
		log.info("여기여기여기2");
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		String profilePicturePath = siteUser.getProfilePictureUrl();
		
		Path path = Paths.get(profilePicturePath);
		try {
				Resource resource = new UrlResource(path.toUri());
				String contentType = Files.probeContentType(path);
				MediaType mediaType = MediaType.IMAGE_JPEG;
				
				if("image/png".equals(contentType)) {
					mediaType = MediaType.IMAGE_PNG;
				} else if ("image/gif".equals(contentType)) {
					mediaType = MediaType.IMAGE_GIF;
				}
				
				
				if(!resource.exists()) {
					return ResponseEntity.notFound().build();
				}
				return ResponseEntity.ok().contentType(mediaType).body(resource);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	*/
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/resetProfilePicture")
	@ResponseBody
	public ResponseEntity<Map<String, String>> resetProfilePicture(Principal principal, @RequestBody Map<String, String> body) {
		
		String username = body.get("username");
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		// 로그인 한 사용자, 아이디 일치 여부
		
		try {
		if(!siteUser.getUsername().equals(username)) {
			throw new AccessDeniedException("본인의 게시물만 삭제할 수 있습니다.");
			
		}
		
		log.info("삭제를 시작합니당... {}", username);
		
		
		
		// 삭제 시작
		this.imageService.resetProfilePicture(siteUser); // TODO
		
		Map<String, String> response = new HashMap<>();
		response.put("message", "success");
		
		return ResponseEntity.ok(response);
		
		} catch (AccessDeniedException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "알 수 없는 오류가 발생.."));
		}
	}
	
	@GetMapping("/google/login")
	public String index() {
		return "redirect:/oauth2/authorization/google";
	}
	
}
