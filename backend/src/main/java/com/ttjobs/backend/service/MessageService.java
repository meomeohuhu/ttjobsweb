package com.ttjobs.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ttjobs.backend.dto.MessageAttachmentDTO;
import com.ttjobs.backend.dto.MessageDTO;
import com.ttjobs.backend.dto.SendMessageRequest;
import com.ttjobs.backend.entity.Conversation;
import com.ttjobs.backend.entity.ConversationMember;
import com.ttjobs.backend.entity.Message;
import com.ttjobs.backend.entity.MessageAttachment;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.ConversationMemberRepository;
import com.ttjobs.backend.repository.ConversationRepository;
import com.ttjobs.backend.repository.MessageAttachmentRepository;
import com.ttjobs.backend.repository.MessageRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MessageService {

    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private ConversationMemberRepository conversationMemberRepository;
    @Autowired
    private MessageAttachmentRepository messageAttachmentRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private AuthContextService authContextService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ObjectProvider<Cloudinary> cloudinaryProvider;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public MessageDTO sendMessage(Long conversationId, SendMessageRequest request) {
        String content = request == null ? null : request.getContent();
        String type = request == null ? null : request.getType();
        return createMessage(conversationId, content, type == null ? "text" : type, null);
    }

    public MessageDTO sendMessageWithAttachment(Long conversationId, String content, MultipartFile file) {
        if ((content == null || content.isBlank()) && (file == null || file.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content or file is required");
        }
        String normalizedContent = content == null ? "" : content.trim();
        return createMessage(conversationId, normalizedContent, file == null ? "text" : "file", file);
    }

    public List<MessageDTO> getMessages(Long conversationId, Integer page, Integer size) {
        User currentUser = authContextService.requireCurrentUser();
        ConversationMember currentMembership = conversationMemberRepository
                .findByIdConversationIdAndIdUserId(conversationId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this conversation"));

        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 20 : Math.max(size, 1);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        List<MessageDTO> result = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        markConversationAsRead(currentMembership, result);
        return result;
    }

    public void downloadAttachment(Long conversationId, Long attachmentId, HttpServletResponse response) {
        User currentUser = authContextService.requireCurrentUser();
        if (!conversationMemberRepository.existsByIdConversationIdAndIdUserId(conversationId, currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this conversation");
        }

        MessageAttachment attachment = messageAttachmentRepository
                .findByIdAndMessageConversationId(attachmentId, conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));

        streamAttachment(attachment, response);
    }

    private MessageDTO createMessage(Long conversationId, String content, String type, MultipartFile file) {
        User currentUser = authContextService.requireCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (!conversationMemberRepository.existsByIdConversationIdAndIdUserId(conversationId, currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this conversation");
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(currentUser);
        message.setContent(content == null ? "" : content.trim());
        message.setType(type == null ? "text" : type);
        message.setCreatedAt(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            message.getAttachments().add(uploadAttachment(message, file));
            if (message.getContent() == null || message.getContent().isBlank()) {
                message.setContent(file.getOriginalFilename() == null ? "Đã gửi file" : "Đã gửi file: " + file.getOriginalFilename());
            }
            message.setType("file");
        }

        Message savedMessage = messageRepository.save(message);
        notifyOtherMembers(conversationId, currentUser, savedMessage);
        MessageDTO dto = toDto(savedMessage);
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, dto);
        }
        return dto;
    }

    private void markConversationAsRead(ConversationMember currentMembership, List<MessageDTO> messages) {
        LocalDateTime latestMessageAt = messages.stream()
                .map(MessageDTO::getCreatedAt)
                .filter(value -> value != null)
                .findFirst()
                .orElse(LocalDateTime.now());
        currentMembership.setLastReadAt(latestMessageAt);
        conversationMemberRepository.save(currentMembership);
    }

    private void notifyOtherMembers(Long conversationId, User sender, Message message) {
        List<ConversationMember> members = conversationMemberRepository.findByIdConversationId(conversationId);
        for (ConversationMember member : members) {
            if (member.getUser() == null || member.getUser().getId().equals(sender.getId())) {
                continue;
            }
            String title = "Tin nhắn mới";
            String content = "Bạn có tin nhắn mới từ " + displayName(sender);
            String targetUrl = chatTargetUrl(member.getUser(), conversationId);
            notificationService.createNotification(member.getUser(), title, content, "CHAT_MESSAGE", targetUrl);
        }
    }

    private String chatTargetUrl(User recipient, Long conversationId) {
        if (recipient != null && recipient.getRole() == User.Role.CANDIDATE) {
            return "/messages?conversationId=" + conversationId;
        }
        return "/recruiter/chat?conversationId=" + conversationId;
    }

    private String displayName(User user) {
        if (user == null) {
            return "Người dùng";
        }
        if (user.getName() != null && !user.getName().isBlank()) {
            return user.getName().trim();
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail().trim();
        }
        return "Người dùng";
    }

    private MessageAttachment uploadAttachment(Message message, MultipartFile file) {
        Cloudinary cloudinary = requireCloudinary();
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "raw",
                    "folder", "ttjobs/messages",
                    "use_filename", true,
                    "unique_filename", true
            ));
            MessageAttachment attachment = new MessageAttachment();
            attachment.setMessage(message);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileUrl(String.valueOf(result.get("secure_url")));
            attachment.setPublicId(String.valueOf(result.get("public_id")));
            attachment.setMimeType(file.getContentType());
            attachment.setFileSize(file.getSize());
            attachment.setCreatedAt(LocalDateTime.now());
            return attachment;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot upload attachment", ex);
        }
    }

    private Cloudinary requireCloudinary() {
        Cloudinary cloudinary = cloudinaryProvider.getIfAvailable();
        if (cloudinary == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cloudinary is not configured");
        }
        return cloudinary;
    }

    private MessageDTO toDto(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        if (message.getConversation() != null) {
            dto.setConversationId(message.getConversation().getId());
        }
        if (message.getSender() != null) {
            dto.setSenderId(message.getSender().getId());
        }
        dto.setContent(message.getContent());
        dto.setType(message.getType());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setAttachments(message.getAttachments() == null ? List.of() : message.getAttachments().stream().map(this::toDto).collect(Collectors.toList()));
        return dto;
    }

    private MessageAttachmentDTO toDto(MessageAttachment attachment) {
        MessageAttachmentDTO dto = new MessageAttachmentDTO();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileUrl(attachment.getFileUrl());
        dto.setPublicId(attachment.getPublicId());
        dto.setMimeType(attachment.getMimeType());
        dto.setFileSize(attachment.getFileSize());
        dto.setCreatedAt(attachment.getCreatedAt());
        return dto;
    }

    private void streamAttachment(MessageAttachment attachment, HttpServletResponse response) {
        String fileUrl = attachment.getFileUrl();
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found");
        }

        String safeName = sanitizeDownloadName(attachment.getFileName(), attachment.getMimeType());
        try {
            java.net.URLConnection connection = new java.net.URL(fileUrl).openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(5000);

            String contentType = attachment.getMimeType();
            if (contentType == null || contentType.isBlank()) {
                contentType = connection.getContentType();
            }
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            response.setContentType(contentType);
            if (attachment.getFileSize() != null && attachment.getFileSize() > 0) {
                response.setContentLengthLong(attachment.getFileSize());
            }
            response.setHeader("Content-Disposition", buildAttachmentDisposition(safeName));

            try (java.io.InputStream input = connection.getInputStream();
                 java.io.OutputStream output = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
            }
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to download attachment", ex);
        }
    }

    private String buildAttachmentDisposition(String fileName) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"" + fileName.replace("\"", "") + "\"; filename*=UTF-8''" + encoded;
    }

    private String sanitizeDownloadName(String fileName, String mimeType) {
        String safeName = (fileName == null || fileName.isBlank()) ? "attachment" : fileName.trim();
        if (!safeName.contains(".") && mimeType != null) {
            String extension = guessExtension(mimeType);
            if (!extension.isBlank()) {
                safeName = safeName + extension;
            }
        }
        return safeName;
    }

    private String guessExtension(String mimeType) {
        return switch (mimeType) {
            case "application/pdf" -> ".pdf";
            case "application/msword" -> ".doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx";
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> "";
        };
    }
}
