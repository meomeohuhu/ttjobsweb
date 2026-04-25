package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.ConversationDTO;
import com.ttjobs.backend.dto.CreateConversationRequest;
import com.ttjobs.backend.entity.Conversation;
import com.ttjobs.backend.entity.ConversationMember;
import com.ttjobs.backend.entity.Message;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.ConversationMemberRepository;
import com.ttjobs.backend.repository.ConversationRepository;
import com.ttjobs.backend.repository.MessageRepository;
import com.ttjobs.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private ConversationMemberRepository conversationMemberRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private AuthContextService authContextService;

    public ConversationDTO createConversation(CreateConversationRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        List<Long> memberIds = request.getMemberIds();
        if (memberIds == null || memberIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "memberIds is required");
        }

        // Ensure current user is always part of the conversation.
        if (!memberIds.contains(currentUser.getId())) {
            memberIds = new ArrayList<>(memberIds);
            memberIds.add(currentUser.getId());
        }

        List<User> members = userRepository.findAllById(memberIds);
        if (members.size() != memberIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Some members not found");
        }

        Conversation conversation = new Conversation();
        conversation.setCreatedAt(LocalDateTime.now());
        Conversation savedConversation = conversationRepository.save(conversation);

        // Create membership records for all participants.
        List<ConversationMember> savedMembers = new ArrayList<>();
        for (User member : members) {
            ConversationMember conversationMember = new ConversationMember();
            conversationMember.setConversation(savedConversation);
            conversationMember.setUser(member);
            conversationMember.setCreatedAt(LocalDateTime.now());
            conversationMember.setLastReadAt(savedConversation.getCreatedAt());
            savedMembers.add(conversationMember);
        }
        conversationMemberRepository.saveAll(savedMembers);

        return toDto(savedConversation, members.stream().map(User::getId).toList(), null, savedMembers);
    }

    public List<ConversationDTO> getMyConversations() {
        User currentUser = authContextService.requireCurrentUser();
        List<ConversationMember> myMemberships = conversationMemberRepository.findByIdUserId(currentUser.getId());
        if (myMemberships.isEmpty()) {
            return List.of();
        }

        List<Long> conversationIds = myMemberships.stream()
                .map(member -> member.getId().getConversationId())
                .distinct()
                .toList();

        List<Conversation> conversations = conversationRepository.findByIdInOrderByCreatedAtDesc(conversationIds);
        List<ConversationMember> allMembers = conversationMemberRepository.findByIdConversationIdIn(conversationIds);
        Map<Long, List<Long>> memberMap = allMembers.stream()
                .collect(Collectors.groupingBy(
                        member -> member.getId().getConversationId(),
                        Collectors.mapping(member -> member.getId().getUserId(), Collectors.toList())
                ));
        Map<Long, ConversationMember> myMembershipMap = myMemberships.stream()
                .collect(Collectors.toMap(member -> member.getId().getConversationId(), member -> member));

        return conversations.stream()
                .map(conversation -> {
                    List<Long> memberIds = memberMap.getOrDefault(conversation.getId(), List.of());
                    return toDto(
                            conversation,
                            memberIds,
                            myMembershipMap.get(conversation.getId()),
                            allMembers.stream()
                                    .filter(member -> member.getId().getConversationId().equals(conversation.getId()))
                                    .collect(Collectors.toList())
                    );
                })
                .collect(Collectors.toList());
    }

    public void ensureMember(Long conversationId, Long userId) {
        if (!conversationMemberRepository.existsByIdConversationIdAndIdUserId(conversationId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this conversation");
        }
    }

    private ConversationDTO toDto(Conversation conversation, List<Long> memberIds, ConversationMember myMembership, List<ConversationMember> conversationMembers) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setMemberIds(memberIds);
        Message lastMessage = messageRepository.findTopByConversationIdOrderByCreatedAtDesc(conversation.getId()).orElse(null);
        if (lastMessage != null) {
            dto.setLastMessageAt(lastMessage.getCreatedAt());
            dto.setLastMessagePreview(buildPreview(lastMessage));
        }
        long unreadCount = 0L;
        long unreadByOthersCount = 0L;
        LocalDateTime myLastReadAt = myMembership == null ? null : myMembership.getLastReadAt();
        if (myLastReadAt != null) {
            unreadCount = messageRepository.countByConversationIdAndSenderIdNotAndCreatedAtAfter(
                    conversation.getId(),
                    myMembership.getId().getUserId(),
                    myLastReadAt
            );
        }

        ConversationMember otherMember = conversationMembers.stream()
                .filter(member -> myMembership == null || !member.getId().getUserId().equals(myMembership.getId().getUserId()))
                .findFirst()
                .orElse(null);
        if (otherMember != null && lastMessage != null && myMembership != null) {
            LocalDateTime otherLastReadAt = otherMember.getLastReadAt();
            if (otherLastReadAt != null) {
                unreadByOthersCount = messageRepository.countByConversationIdAndSenderIdAndCreatedAtAfter(
                        conversation.getId(),
                        myMembership.getId().getUserId(),
                        otherLastReadAt
                );
            }
        }
        dto.setUnreadCount(unreadCount);
        dto.setUnreadByOthersCount(unreadByOthersCount);
        return dto;
    }

    private String buildPreview(Message message) {
        if (message.getContent() != null && !message.getContent().isBlank()) {
            return message.getContent().length() > 80 ? message.getContent().substring(0, 80) + "..." : message.getContent();
        }
        if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
            String fileName = message.getAttachments().get(0).getFileName();
            return fileName == null ? "Đã gửi tệp đính kèm" : "Đã gửi file: " + fileName;
        }
        return "Tin nhắn mới";
    }
}
