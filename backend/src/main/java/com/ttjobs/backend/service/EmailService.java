package com.ttjobs.backend.service;

import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private JavaMailSender mailSender;
    private boolean emailEnabled;
    private String fromAddress;

    @Autowired
    public EmailService(
            JavaMailSender mailSender,
            @Value("${ttjobs.email.enabled:false}") boolean emailEnabled,
            @Value("${ttjobs.email.from:}") String fromAddress) {
        this.mailSender = mailSender;
        this.emailEnabled = emailEnabled;
        this.fromAddress = fromAddress;
    }

    public void sendApplicationSubmitted(User candidate, Job job) {
        // Notify candidate by email when application is submitted.
        if (candidate == null || job == null) {
            return;
        }
        String subject = "TTJobs - Application submitted";
        String body = "You have successfully applied to " + safe(job.getTitle()) + ".";
        sendEmail(candidate.getEmail(), subject, body);
    }

    public void sendNewApplication(User recruiter, User candidate, Job job) {
        // Notify recruiter/owner by email when a new application arrives.
        if (recruiter == null || candidate == null || job == null) {
            return;
        }
        String subject = "TTJobs - New application received";
        String body = safe(candidate.getName()) + " applied to " + safe(job.getTitle()) + ".";
        sendEmail(recruiter.getEmail(), subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            return;
        }
        if (to == null || to.isBlank()) {
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (Exception ignored) {
            // Keep business flow working even if email provider fails.
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
