package com.ttjobs.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ttjobs.backend.dto.UserCvDTO;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.UserRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
public class UserCvService {

    private static final long MAX_CV_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    @Autowired
    private AuthContextService authContextService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectProvider<Cloudinary> cloudinaryProvider;

    public UserCvDTO getMyCv() {
        User currentUser = authContextService.requireCurrentUser();
        return toDto(currentUser);
    }

    public UserCvDTO uploadMyCv(MultipartFile file) {
        User currentUser = authContextService.requireCurrentUser();

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CV file is required");
        }
        if (file.getSize() > MAX_CV_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CV file size exceeds 5MB");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF/DOC/DOCX files are allowed");
        }

        Cloudinary cloudinary = requireCloudinary();
        removeExistingCvIfPossible(cloudinary, currentUser.getCvUrl());

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "ttjobs/cv",
                            "resource_type", "raw",
                            "public_id", "user-" + currentUser.getId() + "-" + System.currentTimeMillis(),
                            "overwrite", true
                    )
            );

            String cvUrl = (String) result.get("secure_url");
            if (cvUrl == null || cvUrl.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloud upload failed");
            }

            currentUser.setCvUrl(cvUrl);
            return toDto(userRepository.save(currentUser));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloud upload failed");
        }
    }

    public void deleteMyCv() {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getCvUrl() == null || currentUser.getCvUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found");
        }

        Cloudinary cloudinary = requireCloudinary();
        removeExistingCvIfPossible(cloudinary, currentUser.getCvUrl());
        currentUser.setCvUrl(null);
        userRepository.save(currentUser);
    }

    private Cloudinary requireCloudinary() {
        Cloudinary cloudinary = cloudinaryProvider.getIfAvailable();
        if (cloudinary == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cloudinary is not configured");
        }
        return cloudinary;
    }

    private void removeExistingCvIfPossible(Cloudinary cloudinary, String existingCvUrl) {
        String publicId = extractPublicId(existingCvUrl);
        if (publicId == null) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
        } catch (Exception ignored) {
            // Keep upload flow resilient if old file cleanup fails.
        }
    }

    // Convert Cloudinary secure url back to public_id so we can remove old files.
    private String extractPublicId(String cvUrl) {
        if (cvUrl == null || cvUrl.isBlank()) {
            return null;
        }
        int uploadMarker = cvUrl.indexOf("/upload/");
        if (uploadMarker < 0) {
            return null;
        }

        String afterUpload = cvUrl.substring(uploadMarker + "/upload/".length());
        String[] parts = afterUpload.split("/", 2);
        if (parts.length < 2) {
            return null;
        }

        String path = parts[0].matches("v\\d+") ? parts[1] : afterUpload;
        int lastSlash = path.lastIndexOf('/');
        int lastDot = path.lastIndexOf('.');
        if (lastDot > lastSlash) {
            path = path.substring(0, lastDot);
        }
        return path;
    }

    private UserCvDTO toDto(User user) {
        UserCvDTO dto = new UserCvDTO();
        dto.setUserId(user.getId());
        dto.setCvUrl(user.getCvUrl());
        return dto;
    }
}
