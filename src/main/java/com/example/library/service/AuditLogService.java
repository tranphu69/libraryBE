package com.example.library.service;

public interface AuditLogService {
    void save(String actorId, String actorRole, String actorPermissionsSnapshot, String action,
              String targetType, String targetId, String oldValue, String newValue, String status);
}
