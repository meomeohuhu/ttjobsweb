package com.ttjobs.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ttjobs.backend.dto.user.UserAvatarDTO;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.UserRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
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
        validateImageSignature(file);

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

    private void validateImageSignature(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(12);
            String contentType = file.getContentType();
            boolean valid = switch (contentType == null ? "" : contentType) {
                case "image/jpeg" -> header.length >= 3
                        && (header[0] & 0xFF) == 0xFF
                        && (header[1] & 0xFF) == 0xD8
                        && (header[2] & 0xFF) == 0xFF;
                case "image/png" -> header.length >= 8
                        && (header[0] & 0xFF) == 0x89
                        && header[1] == 0x50
                        && header[2] == 0x4E
                        && header[3] == 0x47;
                case "image/webp" -> header.length >= 12
                        && header[0] == 0x52
                        && header[1] == 0x49
                        && header[2] == 0x46
                        && header[3] == 0x46
                        && header[8] == 0x57
                        && header[9] == 0x45
                        && header[10] == 0x42
                        && header[11] == 0x50;
                default -> false;
            };
            if (!valid) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid avatar file content");
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid avatar file content");
        }
    }
}

