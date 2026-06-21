package com.example.library.service.service_import_impl;

import com.example.library.domain.Permission;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionImportImpl {
    private final PermissionRepository permissionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePermission(List<Permission> batchInsert) {
        try {
            permissionRepository.saveAll(batchInsert);
        } catch (DataIntegrityViolationException e) {
            for(Permission permission : batchInsert) {
                try {
                    permissionRepository.save(permission);
                } catch (DataIntegrityViolationException ex) {
                    log.warn("Duplicate detected: {}", permission.getCode());
                    throw new BusinessException(ErrorCode.NOT_DUPLICATE, "Permission Code: " + permission.getCode());
                }
            }
        }
    }
}
