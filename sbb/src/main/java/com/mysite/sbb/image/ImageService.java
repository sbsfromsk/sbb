package com.mysite.sbb.image;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserRepository;
import com.mysite.sbb.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class ImageService {

	private final UserService userService;

	private final Path profilePictureDir = Paths.get("D:/sbb");

	public void saveImage(SiteUser siteUser, MultipartFile profilePicture) {

		try {
			if (!Files.exists(profilePictureDir)) {
				Files.createDirectories(profilePictureDir);
			}

			// 파일 이름을 무작위이름.확장자명 으로 변경
			String fileName = generateUniqueFileName(siteUser.getId(), profilePicture.getOriginalFilename());
			Path filePath = profilePictureDir.resolve(fileName);
			// 주어진 경로를 기준에서 추가

			if (siteUser.getProfilePicture() != null) {
				deletePreviousImages(siteUser.getProfilePicture());
			}
			// inputStream 생성, 파일 경로,
			Files.copy(profilePicture.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			/*
			 * StandardCopyOption.COPY_ATTRIBUTES Files.copy(InputStream, Path,
			 * CopyOption...) 방식에서는 COPY_ATTRIBUTES을 사용할 수 없음.
			 */

			siteUser.setProfilePicture(fileName);

			userService.setSiteUser(siteUser);

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("파일 저장 실패", e);
		}

	}

	private String generateUniqueFileName(Long id, String originalFilename) {
		// 마지막의 확장자를 가져오기 위해 originalFilenname가져옴...
		String extenstion = originalFilename.substring(originalFilename.lastIndexOf(".")); // abc.jpg -> lastIndexOf의 값은 3 fileName[abc.jpg].subString(3) 0부터 시작하는 인덱스에서 인덱스 3부터 끝까지 보여주세요.
		return id + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + extenstion;

	}

	private void deletePreviousImages(String profilePicture) throws IOException {
		Files.deleteIfExists(profilePictureDir.resolve(profilePicture));
	}

	public void resetProfilePicture(SiteUser siteUser) {
		String profilePicture = siteUser.getProfilePicture();

		try {
			deletePreviousImages(profilePicture);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("파일 삭제 실패");
		}

		// DB에서도 지우기.
		siteUser.setProfilePicture(null);
		userService.setSiteUser(siteUser);

	}

	public ResponseEntity<Resource> getProfilePicture(String username) {
		log.info("유저네임0509: {}",username);
		SiteUser siteUser = userService.getUser(username);
		String profilePicturePath = siteUser.getProfilePictureUrl();
		log.info("프로필 이미지: {}", profilePicturePath);
		Path path = Paths.get(profilePicturePath);
		log.info("프로필 이미지2: {}", path);
		try {
			Resource resource = new UrlResource(path.toUri());
			String contentType = Files.probeContentType(path);
			MediaType mediaType = MediaType.IMAGE_JPEG;

			if ("image/png".equals(contentType)) {
				mediaType = MediaType.IMAGE_PNG;
			} else if ("image/gif".equals(contentType)) {
				mediaType = MediaType.IMAGE_GIF;
			}

			if (!resource.exists()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok().contentType(mediaType).body(resource);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}
