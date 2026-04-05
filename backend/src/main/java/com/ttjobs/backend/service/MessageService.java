package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.MessageDTO;
import com.ttjobs.backend.dto.SendMessageRequest;
import com.ttjobs.backend.entity.Conversation;
import com.ttjobs.backend.entity.ConversationMember;
import com.ttjobs.backend.entity.Message;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.ConversationMemberRepository;
import com.ttjobs.backend.repository.ConversationRepository;
import com.ttjobs.backend.repository.MessageRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MessageService {

    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private ConversationMemberRepository conversationMemberRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private AuthContextService authContextService;
    @Autowired
    private NotificationService notificationService;

    public MessageDTO sendMessage(Long conversationId, SendMessageRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (!conversationMemberRepository.existsByIdConversationIdAndIdUserId(conversationId, currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this conversation");
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(currentUser);
        message.setContent(request.getContent());
        message.setType(request.getType() == null ? "text" : request.getType());
        message.setCreatedAt(LocalDateTime.now());
        Message savedMessage = messageRepository.save(message);

        // Notify other members about the new message.
        notifyOtherMembers(conversationId, currentUser, savedMessage);
        return toDto(savedMessage);
    }

    public List<MessageDTO> getMessages(Long conversationId, Integer page, Integer size) {
        User currentUser = authContextService.requireCurrentUser();
        if (!conversationMemberRepository.existsByIdConversationIdAndIdUserId(conversationId, currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this conversation");
        }

        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 20 : Math.max(size, 1);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private void notifyOtherMembers(Long conversationId, User sender, Message message) {
        List<ConversationMember> members = conversationMemberRepository.findByIdConversationId(conversationId);
        for (ConversationMember member : members) {
            if (member.getUser() == null || member.getUser().getId().equals(sender.getId())) {
                continue;
            }
            String title = "New message";
            String content = "You have a new message from " + sender.getName();
            notificationService.createNotification(member.getUser(), title, content, "CHAT_MESSAGE");
        }
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
        return dto;
    }
}
