package com.ttjobs.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ttjobs.backend.dto.UserAvatarDTO;
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
public class UserAvatarService {

    private static final long MAX_AVATAR_SIZE = 3L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Autowired
    private AuthContextService authContextService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectProvider<Cloudinary> cloudinaryProvider;

    public UserAvatarDTO getMyAvatar() {
        User currentUser = authContextService.requireCurrentUser();
        return toDto(currentUser);
    }

    public UserAvatarDTO uploadMyAvatar(MultipartFile file) {
        User currentUser = authContextService.requireCurrentUser();

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file is required");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file size exceeds 3MB");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG/PNG/WEBP files are allowed");
        }

        Cloudinary cloudinary = requireCloudinary();
        removeExistingAvatarIfPossible(cloudinary, currentUser.getAvatarUrl());

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "ttjobs/avatar",
                            "resource_type", "image",
                            "public_id", "user-" + currentUser.getId() + "-" + System.currentTimeMillis(),
                            "overwrite", true
                    )
            );

            String avatarUrl = (String) result.get("secure_url");
            if (avatarUrl == null || avatarUrl.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloud upload failed");
            }

            currentUser.setAvatarUrl(avatarUrl);
            return toDto(userRepository.save(currentUser));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloud upload failed");
        }
    }

    public void deleteMyAvatar() {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getAvatarUrl() == null || currentUser.getAvatarUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar not found");
        }

        Cloudinary cloudinary = requireCloudinary();
        removeExistingAvatarIfPossible(cloudinary, currentUser.getAvatarUrl());
        currentUser.setAvatarUrl(null);
        userRepository.save(currentUser);
    }

    private Cloudinary requireCloudinary() {
        Cloudinary cloudinary = cloudinaryProvider.getIfAvailable();
        if (cloudinary == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cloudinary is not configured");
        }
        return cloudinary;
    }

    private void removeExistingAvatarIfPossible(Cloudinary cloudinary, String existingAvatarUrl) {
        String publicId = extractPublicId(existingAvatarUrl);
        if (publicId == null) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (Exception ignored) {
            // Keep upload flow resilient if old file cleanup fails.
        }
    }

    // Convert Cloudinary secure url back to public_id so we can remove old files.
    private String extractPublicId(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return null;
        }
        int uploadMarker = avatarUrl.indexOf("/upload/");
        if (uploadMarker < 0) {
            return null;
        }

        String afterUpload = avatarUrl.substring(uploadMarker + "/upload/".length());
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

    private UserAvatarDTO toDto(User user) {
        UserAvatarDTO dto = new UserAvatarDTO();
        dto.setUserId(user.getId());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }
}
