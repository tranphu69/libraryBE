package com.example.library.domain;

import jakarta.persistence.*;

import java.security.AuthProvider;
import java.time.LocalDateTime;

//@Entity
//@Table(name = "USERS")
public class User {
//    @Id
//    @Column(name = "ID")
//    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String password;
    private Boolean isActive;
    private Boolean isEmailVerified;
//    private AuthProvider provider = AuthProvider.LOCAL;
    private String providerId;
    private Boolean mfaEnabled;
    private String mfaSecret;
    private Boolean isLocked;
    private Integer failedLoginAttempts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime lastLoginAt;
}
