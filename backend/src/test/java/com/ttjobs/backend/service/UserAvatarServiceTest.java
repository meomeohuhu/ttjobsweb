package com.ttjobs.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.ttjobs.backend.dto.UserAvatarDTO;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAvatarServiceTest {

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

    @InjectMocks
    private UserAvatarService userAvatarService;

    @Test
    void uploadMyAvatar_shouldReturnServiceUnavailable_whenCloudinaryNotConfigured() {
        User user = user(1L);
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "dummy".getBytes()
        );

        when(authContextService.requireCurrentUser()).thenReturn(user);
        when(cloudinaryProvider.getIfAvailable()).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userAvatarService.uploadMyAvatar(file));

        assertEquals(503, ex.getStatusCode().value());
    }

    @Test
    void uploadMyAvatar_shouldSaveAvatarUrl_whenUploadSuccess() throws IOException {
        User user = user(2L);
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "hello".getBytes()
        );

        when(authContextService.requireCurrentUser()).thenReturn(user);
        when(cloudinaryProvider.getIfAvailable()).thenReturn(cloudinary);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/demo/image/upload/v1/ttjobs/avatar/file.png"));
        when(userRepository.save(user)).thenReturn(user);

        UserAvatarDTO result = userAvatarService.uploadMyAvatar(file);
        assertEquals(2L, result.getUserId());
        assertEquals("https://res.cloudinary.com/demo/image/upload/v1/ttjobs/avatar/file.png", result.getAvatarUrl());
    }

    @Test
    void deleteMyAvatar_shouldClearAvatarUrl() {
        User user = user(3L);
        user.setAvatarUrl("https://res.cloudinary.com/demo/image/upload/v1/ttjobs/avatar/file.png");

        when(authContextService.requireCurrentUser()).thenReturn(user);
        when(cloudinaryProvider.getIfAvailable()).thenReturn(cloudinary);
        when(cloudinary.uploader()).thenReturn(uploader);

        userAvatarService.deleteMyAvatar();

        assertNull(user.getAvatarUrl());
        verify(userRepository).save(user);
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("u" + id + "@mail.com");
        user.setRole(User.Role.CANDIDATE);
        return user;
    }
}
