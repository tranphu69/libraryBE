package com.example.library.service.serviceImportImpl;

import com.example.library.domain.Role;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.repository.RoleRepository;
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
public class RoleImportImpl {
    private final RoleRepository roleRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRole(List<Role> batchInsert) {
        try {
            roleRepository.saveAll(batchInsert);
        } catch (DataIntegrityViolationException e) {
            for (Role role : batchInsert) {
                try {
                    roleRepository.save(role);
                }  catch (DataIntegrityViolationException ex) {
                    log.warn("Duplicate detected: {}", role.getCode());
                    throw new BusinessException(ErrorCode.NOT_DUPLICATE, "Role Code: " + role.getCode());
                }
            }
        }
    }
}
