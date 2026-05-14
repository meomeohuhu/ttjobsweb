package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.conversation.ConversationDTO;
import com.ttjobs.backend.dto.conversation.CreateConversationRequest;
import com.ttjobs.backend.entity.Conversation;
import com.ttjobs.backend.entity.ConversationMember;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.ConversationMemberRepository;
import com.ttjobs.backend.repository.ConversationRepository;
import com.ttjobs.backend.repository.UserRepository;
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
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationMemberRepository conversationMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthContextService authContextService;

    @InjectMocks
    private ConversationService conversationService;

    @Test
    void createConversation_shouldIncludeCurrentUser() {
        User current = user(1L);
        User other = user(2L);
        CreateConversationRequest request = new CreateConversationRequest();
        request.setMemberIds(List.of(2L));

        Conversation conversation = new Conversation();
        conversation.setId(10L);

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(userRepository.findAllById(List.of(2L, 1L))).thenReturn(List.of(other, current));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        ConversationDTO dto = conversationService.createConversation(request);
        assertEquals(10L, dto.getId());
        verify(conversationMemberRepository).saveAll(any());
    }

    @Test
    void createConversation_shouldReturnNotFound_whenMemberMissing() {
        User current = user(1L);
        CreateConversationRequest request = new CreateConversationRequest();
        request.setMemberIds(List.of(2L));

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(userRepository.findAllById(List.of(2L, 1L))).thenReturn(List.of(current));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> conversationService.createConversation(request));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void getMyConversations_shouldReturnEmpty_whenNoMembership() {
        User current = user(1L);
        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(conversationMemberRepository.findByIdUserId(1L)).thenReturn(List.of());

        List<ConversationDTO> result = conversationService.getMyConversations();
        assertEquals(0, result.size());
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("u" + id + "@mail.com");
        user.setName("User " + id);
        return user;
    }
}

