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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        verify(notificationService).createNotification(other, "New message", "You have a new message from User 1", "CHAT_MESSAGE");
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
