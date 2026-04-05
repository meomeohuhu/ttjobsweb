package com.ttjobs.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.ttjobs.backend.dto.UserCvDTO;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCvServiceTest {

    @Mock
    private AuthContextService authContextService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ObjectProvider<Cloudinary> cloudinaryProvider;
    @Mock
    private Cloudinary cloudinary;
    @Mock
    private Uploader uploader;
    @Mock
    private CvTextExtractionService cvTextExtractionService;

    @InjectMocks
    private UserCvService userCvService;

    @Test
    void uploadMyCv_shouldReturnServiceUnavailable_whenCloudinaryNotConfigured() {
        User user = user(1L);
        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf", "dummy".getBytes()
        );

        when(authContextService.requireCurrentUser()).thenReturn(user);
        when(cloudinaryProvider.getIfAvailable()).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userCvService.uploadMyCv(file));

        assertEquals(503, ex.getStatusCode().value());
    }

    @Test
    void uploadMyCv_shouldSaveCvUrl_whenUploadSuccess() throws IOException {
        User user = user(2L);
        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf", "hello".getBytes()
        );

        when(authContextService.requireCurrentUser()).thenReturn(user);
        when(cloudinaryProvider.getIfAvailable()).thenReturn(cloudinary);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/demo/raw/upload/v1/ttjobs/cv/file.pdf"));
        when(cvTextExtractionService.extractText(any(byte[].class), any(String.class), any(String.class)))
                .thenReturn("text");
        when(userRepository.save(user)).thenReturn(user);

        UserCvDTO result = userCvService.uploadMyCv(file);
        assertEquals(2L, result.getUserId());
        assertEquals("https://res.cloudinary.com/demo/raw/upload/v1/ttjobs/cv/file.pdf", result.getCvUrl());
        assertNotNull(user.getCvText());
    }

    @Test
    void deleteMyCv_shouldClearCvUrl() {
        User user = user(3L);
        user.setCvUrl("https://res.cloudinary.com/demo/raw/upload/v1/ttjobs/cv/file.pdf");
        user.setCvText("text");

        when(authContextService.requireCurrentUser()).thenReturn(user);
        when(cloudinaryProvider.getIfAvailable()).thenReturn(cloudinary);
        when(cloudinary.uploader()).thenReturn(uploader);

        userCvService.deleteMyCv();

        assertNull(user.getCvUrl());
        assertNull(user.getCvText());
        verify(userRepository).save(user);
    }

    @Test
    void extractMyCvText_shouldReturnBadGateway_whenDownloadFails() {
        User user = user(4L);
        user.setCvUrl("http://invalid-host/cv.pdf");

        when(authContextService.requireCurrentUser()).thenReturn(user);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userCvService.extractMyCvText());

        assertEquals(502, ex.getStatusCode().value());
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("u" + id + "@mail.com");
        user.setRole(User.Role.CANDIDATE);
        return user;
    }
}
