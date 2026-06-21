package com.example.library.service.service_import_impl;

import com.example.library.domain.User;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.repository.UserRepository;
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
public class UserImportImpl {
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveUser(List<User> batchInsert) {
        try {
            userRepository.saveAll(batchInsert);
        } catch (DataIntegrityViolationException e) {
            for (User user : batchInsert) {
                try {
                    userRepository.save(user);
                }  catch (DataIntegrityViolationException ex) {
                    log.warn("Duplicate detected: {}", user.getCode());
                    throw new BusinessException(ErrorCode.NOT_DUPLICATE, "User Code: " + user.getCode());
                }
            }
        }
    }
}
