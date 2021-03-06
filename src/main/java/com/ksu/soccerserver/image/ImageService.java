package com.ksu.soccerserver.image;

import com.ksu.soccerserver.account.*;
import com.ksu.soccerserver.config.JwtTokenProvider;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImageService {

    private final Path rootLocation;

    private final JwtTokenProvider jwtProvider;

    private final AccountRepository accountRepository;

    public ImageService(ImageProperties imageProperties, JwtTokenProvider jwtProvider,
                        AccountRepository accountRepository) {
        this.rootLocation = Paths.get(imageProperties.getLocation()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
        this.jwtProvider = jwtProvider;
        this.accountRepository = accountRepository;
    }

    public String Prefix(HttpServletRequest request){

        String prefix;
        if(request.getRequestURI().contains("accounts")) {
            prefix = "account";
        }
        else{
            prefix = "team";
        }
        return prefix;
    }

    public String saveImage(MultipartFile image, HttpServletRequest request) {
        String prefix = Prefix(request);
        ServletUriComponentsBuilder defaultPath = ServletUriComponentsBuilder.fromCurrentContextPath();
        String defaultImage = defaultPath.toUriString() + request.getRequestURI() + "/images/" + prefix +"_default.jpg";

        String imageName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
        String extension = FilenameUtils.getExtension(imageName);

        try {
            if(image.isEmpty()){
                return defaultImage;
            }

            if (!"jpg".equals(extension) && !"jpeg".equals(extension) && !"png".equals(extension)) {
                return defaultImage;
            }
            try (InputStream inputStream = image.getInputStream()) {
                Path targetLocation = this.rootLocation.resolve(imageName);
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

        }catch (IOException e){
            return defaultImage;
        }

        String email = jwtProvider.getUserPk(request.getHeader(HttpHeaders.AUTHORIZATION));
        Optional<Account> accountOptional = accountRepository.findByEmail(email);
        if (!accountOptional.isPresent()) {
            return defaultImage;
        }

        String requestUri = request.getRequestURI() + "/images/";
        String newImagePath = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(requestUri)
                .path(imageName)
                .toUriString();

        return newImagePath;
    }

    public ResponseEntity<?> loadAsResource(String imageName, HttpServletRequest request) {
        try {
            Path imagePath = load(imageName);
            UrlResource resource = new UrlResource(imagePath.toUri());
            // exists : 데이터 존재 확인, isReadable : Resource를 읽을 수 있는지 확인
            if (resource.exists() || resource.isReadable()) {
                String contentType = null;
                try {
                    contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
                } catch (IOException e) {
                    return new ResponseEntity<>("TypeError", HttpStatus.BAD_REQUEST);
                }
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                // 이미지 출력
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                // 데이터가 존재 하지 않을 경우 기본 이미지 주소 전달
                String prefix = Prefix(request);

                // 기본 이미지 출력
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + prefix + "_default.jpg" + "\"")
                        .body(resource);
            }
        } catch (MalformedURLException e) {
            return new ResponseEntity<>("findError", HttpStatus.BAD_REQUEST);
        }
    }
    // 유저 기본 이미지
    Path load(String imageName) {
        return this.rootLocation.resolve(imageName).normalize();
    }
    
}
