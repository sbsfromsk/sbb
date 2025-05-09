package com.mysite.sbb.image;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Controller
@Slf4j
@RequestMapping("/image")
public class ImageController {

	private final ImageService imageService;
	
	@GetMapping("/profile-picture")
	public ResponseEntity<Resource> getProfilePicture(Principal principal) {
		
		log.info("여기여기여기2ㅎㅎㅎ");
		
		
		return imageService.getProfilePicture(principal.getName());
		
	}
	
	@GetMapping("/profile-picture2")
	public ResponseEntity<Resource> getProfilePicture(@RequestParam("username") String username) {
		
		log.info("username: {} ", username);
		
		
		return imageService.getProfilePicture(username);
		
	}
}
