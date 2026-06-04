package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.conversation.MessageDTO;
import com.ttjobs.backend.dto.conversation.SendMessageRequest;
import com.ttjobs.backend.entity.Conversation;
import com.ttjobs.backend.entity.ConversationMember;
import com.ttjobs.backend.entity.Message;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.ConversationMemberRepository;
import com.ttjobs.backend.repository.ConversationRepository;
import com.ttjobs.backend.repository.MessageRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationMemberRepository conversationMemberRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private AuthContextService authContextService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private RealtimeEventPublisher realtimeEventPublisher;

    @InjectMocks
    private MessageService messageService;

    @Test
    void sendMessage_shouldReturnForbidden_whenNotMember() {
        User current = user(1L);
        Conversation conversation = new Conversation();
        conversation.setId(10L);
        SendMessageRequest request = new SendMessageRequest();
        request.setContent("Hi");

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(conversationRepository.findById(10L)).thenReturn(java.util.Optional.of(conversation));
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserId(10L, 1L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> messageService.sendMessage(10L, request));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void sendMessage_shouldNotifyOtherMembers() {
        User current = user(1L);
        User other = user(2L);
        Conversation conversation = new Conversation();
        conversation.setId(10L);
        SendMessageRequest request = new SendMessageRequest();
        request.setContent("Hi");

        Message savedMessage = new Message();
        savedMessage.setId(20L);
        savedMessage.setConversation(conversation);
        savedMessage.setSender(current);
        savedMessage.setContent("Hi");
        savedMessage.setType("text");

        ConversationMember memberCurrent = member(conversation, current);
        ConversationMember memberOther = member(conversation, other);

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(conversationRepository.findById(10L)).thenReturn(java.util.Optional.of(conversation));
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserId(10L, 1L)).thenReturn(true);
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(conversationMemberRepository.findByIdConversationId(10L)).thenReturn(List.of(memberCurrent, memberOther));

        MessageDTO dto = messageService.sendMessage(10L, request);
        assertEquals(20L, dto.getId());
        verify(realtimeEventPublisher).publish(eq("/topic/conversations/10"), any(MessageDTO.class));
        verify(notificationService).createNotification(
                eq(other),
                anyString(),
                contains("User 1"),
                eq("CHAT_MESSAGE"),
                eq("/recruiter/chat?conversationId=10"));
    }

    @Test
    void sendMessageWithAttachment_shouldRejectUnsupportedMimeType() {
        User current = user(1L);
        Conversation conversation = new Conversation();
        conversation.setId(10L);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "payload.exe",
                "application/x-msdownload",
                new byte[] {1, 2, 3});

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(conversationRepository.findById(10L)).thenReturn(java.util.Optional.of(conversation));
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserId(10L, 1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> messageService.sendMessageWithAttachment(10L, "", file));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void sendMessageWithAttachment_shouldRejectUnsafeFileName() {
        User current = user(1L);
        Conversation conversation = new Conversation();
        conversation.setId(10L);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "..\\payload.pdf",
                "application/pdf",
                "%PDF".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(conversationRepository.findById(10L)).thenReturn(java.util.Optional.of(conversation));
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserId(10L, 1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> messageService.sendMessageWithAttachment(10L, "", file));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void sendMessageWithAttachment_shouldRejectOversizedFile() {
        User current = user(1L);
        Conversation conversation = new Conversation();
        conversation.setId(10L);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                new byte[(10 * 1024 * 1024) + 1]);

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(conversationRepository.findById(10L)).thenReturn(java.util.Optional.of(conversation));
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserId(10L, 1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> messageService.sendMessageWithAttachment(10L, "", file));

        assertEquals(400, ex.getStatusCode().value());
    }

    private ConversationMember member(Conversation conversation, User user) {
        ConversationMember member = new ConversationMember();
        member.setConversation(conversation);
        member.setUser(user);
        return member;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("u" + id + "@mail.com");
        user.setName("User " + id);
        return user;
    }
}

