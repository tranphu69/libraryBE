package com.example.library.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String id;
    private String code;
    private String email;
    private String fullName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Set<SimpleResponse> listRole;
    private String avatarUrl;
    private boolean isDeleted;
    private boolean isActive;
    private boolean isEmailVerified;
    private boolean mfaEnabled;
    private boolean isLocked;
    private int failedLoginAttempts;
    private LocalDateTime lastLoginAt;
}
