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
    private static final int DOWNLOAD_CONNECT_TIMEOUT_MS = 3000;
    private static final int DOWNLOAD_READ_TIMEOUT_MS = 5000;
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
    @Autowired
    private CvTextExtractionService cvTextExtractionService;

    public UserCvDTO getMyCv() {
        User currentUser = authContextService.requireCurrentUser();
        return toDto(currentUser);
    }

    public com.ttjobs.backend.dto.UserCvTextDTO getMyCvText() {
        User currentUser = authContextService.requireCurrentUser();
        return toTextDto(currentUser);
    }

    public com.ttjobs.backend.dto.UserCvTextDTO extractMyCvText() {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getCvUrl() == null || currentUser.getCvUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found");
        }
        byte[] data = downloadCvBytes(currentUser.getCvUrl());
        String cvText = cvTextExtractionService.extractText(data, null, currentUser.getCvUrl());
        currentUser.setCvText(cvText);
        return toTextDto(userRepository.save(currentUser));
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

            // Extract CV text once and store for later recommendation use.
            String cvText = cvTextExtractionService.extractText(
                    file.getBytes(),
                    file.getContentType(),
                    file.getOriginalFilename()
            );

            currentUser.setCvUrl(cvUrl);
            currentUser.setCvText(cvText);
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
        currentUser.setCvText(null);
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

    private com.ttjobs.backend.dto.UserCvTextDTO toTextDto(User user) {
        com.ttjobs.backend.dto.UserCvTextDTO dto = new com.ttjobs.backend.dto.UserCvTextDTO();
        dto.setUserId(user.getId());
        dto.setCvText(user.getCvText());
        return dto;
    }

    private byte[] downloadCvBytes(String cvUrl) {
        try {
            java.net.URLConnection connection = new java.net.URL(cvUrl).openConnection();
            connection.setConnectTimeout(DOWNLOAD_CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(DOWNLOAD_READ_TIMEOUT_MS);
            try (java.io.InputStream input = connection.getInputStream()) {
                return readLimited(input, MAX_CV_SIZE);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to download CV");
        }
    }

    private byte[] readLimited(java.io.InputStream input, long maxBytes) throws IOException {
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        long total = 0;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CV file size exceeds 5MB");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }
}
