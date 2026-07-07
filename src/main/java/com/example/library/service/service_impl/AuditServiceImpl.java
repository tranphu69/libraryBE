package com.example.library.service.service_impl;

import com.example.library.domain.AuditLog;
import com.example.library.repository.AuditLogRepository;
import com.example.library.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuditServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(String actorId, String actorRole, String actorPermissionsSnapshot, String action,
                     String targetType, String targetId, String oldValue, String newValue, String status) {
        AuditLog log = new AuditLog();
        log.setId(UUID.randomUUID().toString());
        log.setTimestamp(Instant.now());
        log.setActorId(actorId);
        log.setActorRole(actorRole);
        log.setActorPermissionsSnapshot(actorPermissionsSnapshot);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setStatus(status);
        auditLogRepository.save(log);
    }
}
