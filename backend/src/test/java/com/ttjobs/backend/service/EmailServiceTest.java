package com.ttjobs.backend.service;

import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    private EmailService emailService;

    @Test
    void sendApplicationSubmitted_shouldSend_whenEnabled() {
        // Build service with email enabled to verify send flow.
        emailService = buildService(true);

        User candidate = user(1L, "candidate@mail.com");
        Job job = new Job();
        job.setTitle("Backend Dev");

        emailService.sendApplicationSubmitted(candidate, job);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendApplicationSubmitted_shouldSkip_whenDisabled() {
        // Build service with email disabled to verify skip flow.
        emailService = buildService(false);

        User candidate = user(1L, "candidate@mail.com");
        Job job = new Job();
        job.setTitle("Backend Dev");

        emailService.sendApplicationSubmitted(candidate, job);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    private EmailService buildService(boolean enabled) {
        return new EmailService(mailSender, templateEngine, enabled, "noreply@ttjobs.local", "http://localhost:5173");
    }

    private User user(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName("User " + id);
        return user;
    }
}
