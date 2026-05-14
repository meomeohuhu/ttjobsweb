package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.admin.AdminAuditLogDTO;
import com.ttjobs.backend.entity.AdminAuditLog;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.AdminAuditLogRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class AdminAuditLogService {

    @Autowired
    private AdminAuditLogRepository adminAuditLogRepository;
    @Autowired
    private AuthContextService authContextService;

    public void log(String action, String targetType, Long targetId, String reason, String metadata) {
        User actor = authContextService.getCurrentUserOptional().orElse(null);
        AdminAuditLog log = new AdminAuditLog();
        log.setActor(actor);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setReason(reason);
        log.setMetadata(metadata);
        adminAuditLogRepository.save(log);
    }

    public List<AdminAuditLogDTO> getRecent(Integer size) {
        int safeSize = size == null ? 100 : Math.min(Math.max(size, 1), 500);
        return adminAuditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, safeSize))
                .stream()
                .map(this::toDto)
                .toList();
    }

    private AdminAuditLogDTO toDto(AdminAuditLog log) {
        AdminAuditLogDTO dto = new AdminAuditLogDTO();
        dto.setId(log.getId());
        dto.setActorId(log.getActor() == null ? null : log.getActor().getId());
        dto.setActorName(log.getActor() == null ? null : log.getActor().getName());
        dto.setAction(log.getAction());
        dto.setTargetType(log.getTargetType());
        dto.setTargetId(log.getTargetId());
        dto.setReason(log.getReason());
        dto.setMetadata(log.getMetadata());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
