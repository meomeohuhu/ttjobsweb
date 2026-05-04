package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.ToolSessionRequest;
import com.ttjobs.backend.dto.ToolSessionResponse;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.entity.UserToolSession;
import com.ttjobs.backend.repository.UserToolSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ToolSessionService {
    private static final int MAX_HISTORY_PER_TOOL = 20;

    @Autowired
    private AuthContextService authContextService;

    @Autowired
    private UserToolSessionRepository userToolSessionRepository;

    @Transactional
    public ToolSessionResponse saveSession(ToolSessionRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        UserToolSession session = new UserToolSession();
        session.setUser(currentUser);
        session.setToolSlug(request.getToolSlug().trim());
        session.setInputJson(request.getInputJson());
        session.setResultJson(request.getResultJson());
        UserToolSession saved = userToolSessionRepository.save(session);
        trimHistory(currentUser.getId(), saved.getToolSlug());
        return toDto(saved);
    }

    public List<ToolSessionResponse> getMySessions(String toolSlug) {
        User currentUser = authContextService.requireCurrentUser();
        List<UserToolSession> sessions = toolSlug == null || toolSlug.isBlank()
                ? userToolSessionRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                : userToolSessionRepository.findByUserIdAndToolSlugOrderByCreatedAtDesc(currentUser.getId(), toolSlug.trim());
        return sessions.stream().map(this::toDto).toList();
    }

    @Transactional
    public void deleteMySession(Long id) {
        User currentUser = authContextService.requireCurrentUser();
        UserToolSession session = userToolSessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tool session not found"));
        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete this tool session");
        }
        userToolSessionRepository.delete(session);
    }

    private void trimHistory(Long userId, String toolSlug) {
        List<UserToolSession> sessions = userToolSessionRepository.findByUserIdAndToolSlugOrderByCreatedAtDesc(userId, toolSlug);
        if (sessions.size() <= MAX_HISTORY_PER_TOOL) {
            return;
        }
        sessions.stream().skip(MAX_HISTORY_PER_TOOL).forEach(userToolSessionRepository::delete);
    }

    private ToolSessionResponse toDto(UserToolSession session) {
        ToolSessionResponse dto = new ToolSessionResponse();
        dto.setId(session.getId());
        dto.setToolSlug(session.getToolSlug());
        dto.setInputJson(session.getInputJson());
        dto.setResultJson(session.getResultJson());
        dto.setCreatedAt(session.getCreatedAt());
        return dto;
    }
}
