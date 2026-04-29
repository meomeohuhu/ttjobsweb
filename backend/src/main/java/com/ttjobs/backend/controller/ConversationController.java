package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.ConversationDTO;
import com.ttjobs.backend.dto.CreateConversationRequest;
import com.ttjobs.backend.dto.MessageDTO;
import com.ttjobs.backend.dto.SendMessageRequest;
import com.ttjobs.backend.service.ConversationService;
import com.ttjobs.backend.service.MessageService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;
    @Autowired
    private MessageService messageService;

    @PostMapping
    public ConversationDTO createConversation(@Valid @RequestBody CreateConversationRequest request) {
        return conversationService.createConversation(request);
    }

    @GetMapping
    public List<ConversationDTO> getMyConversations() {
        return conversationService.getMyConversations();
    }

    @PostMapping("/{conversationId}/messages")
    public MessageDTO sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        return messageService.sendMessage(conversationId, request);
    }

    @PostMapping(value = "/{conversationId}/messages/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MessageDTO sendMessageWithAttachment(
            @PathVariable Long conversationId,
            @RequestParam(required = false) String content,
            @RequestPart(required = false) MultipartFile file) {
        return messageService.sendMessageWithAttachment(conversationId, content, file);
    }

    @GetMapping("/{conversationId}/messages")
    public List<MessageDTO> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return messageService.getMessages(conversationId, page, size);
    }

    @GetMapping("/{conversationId}/attachments/{attachmentId}/download")
    public void downloadAttachment(
            @PathVariable Long conversationId,
            @PathVariable Long attachmentId,
            HttpServletResponse response) {
        messageService.downloadAttachment(conversationId, attachmentId, response);
    }
}
