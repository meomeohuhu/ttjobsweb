package com.ttjobs.backend.service;

import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.InterviewSchedule;
import com.ttjobs.backend.entity.User;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
public class EmailService {

    private JavaMailSender mailSender;
    private TemplateEngine templateEngine;
    private boolean emailEnabled;
    private String fromAddress;
    private String appBaseUrl;

    @Autowired
    public EmailService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            @Value("${ttjobs.email.enabled:false}") boolean emailEnabled,
            @Value("${ttjobs.email.from:}") String fromAddress,
            @Value("${ttjobs.app.base-url:http://localhost:5173}") String appBaseUrl) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.emailEnabled = emailEnabled;
        this.fromAddress = fromAddress;
        this.appBaseUrl = appBaseUrl;
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

    public void sendApplicationStatusChanged(User candidate, Job job, String status) {
        if (candidate == null || job == null || status == null) {
            return;
        }
        String subject = "TTJobs - Application status updated";
        String body = "Your application for " + safe(job.getTitle()) + " is now " + safe(status) + ".";
        sendEmail(candidate.getEmail(), subject, body);
    }

    public void sendInterviewCreated(User candidate, InterviewSchedule interview) {
        if (candidate == null || interview == null) {
            return;
        }
        String subject = "TTJobs - Interview scheduled";
        String body = "Your interview is scheduled at " + interview.getScheduledAt()
                + ". Location: " + safe(interview.getLocation())
                + ". Meeting link: " + safe(interview.getMeetingLink()) + ".";
        sendEmail(candidate.getEmail(), subject, body);
    }

    public void sendJobAlertEmail(User user, List<Job> jobs) {
        if (user == null || jobs == null || jobs.isEmpty()) {
            return;
        }
        Context context = new Context();
        context.setVariable("userName", safe(user.getName()).isBlank() ? "bạn" : safe(user.getName()));
        context.setVariable("jobs", jobs);
        context.setVariable("appBaseUrl", appBaseUrl);
        context.setVariable("matchingUrl", appBaseUrl + "/user/matching");
        context.setVariable("settingsUrl", appBaseUrl + "/user/notifications");

        String html = templateEngine.process("emails/job-alert", context);
        sendHtmlEmail(user.getEmail(), "TTJobs - Việc làm mới phù hợp với bạn", html);
    }

    public void sendEmailChangeCode(String to, String code) {
        if (to == null || code == null) {
            return;
        }
        String subject = "TTJobs - Mã xác nhận đổi email";
        String body = "Mã xác nhận đổi email của bạn là: " + code
                + "\nMã có hiệu lực trong 10 phút. Nếu bạn không yêu cầu đổi email, hãy bỏ qua email này.";
        sendEmail(to, subject, body);
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

    private void sendHtmlEmail(String to, String subject, String html) {
        if (!emailEnabled) {
            return;
        }
        if (to == null || to.isBlank()) {
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress);
            }
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception ignored) {
            // Keep business flow working even if email provider fails.
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
