package com.example.library.domain;

import com.example.library.constant.Provider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "USERS")
public class User {
    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "EMAIL")
    private String email;
    @Column(name = "FULL_NAME")
    private String fullName;
    @Column(name = "AVATAR_URL")
    private String avatarUrl;
    @Column(name = "PASSWORD")
    private String password;
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
    @Column(name = "CREATED_BY")
    private String createdBy;
    @Column(name = "UPDATED_BY")
    private String updatedBy;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    // XEM TAI KHOAN DA GUI OPT DE KICH HOAT CHUA
    @Column(name = "IS_ACTIVE")
    private boolean isActive = false;
    // DANH DAU EMAIL DA XAC THUC
    @Column(name = "IS_EMAIL_VERIFIED")
    private boolean isEmailVerified = false;
    // NGUON DANG NHAP CUA NGUOI DUNG
    @Enumerated(EnumType.STRING)
    @Column(name = "PROVIDER")
    private Provider provider = Provider.LOCAL;
    // ID NGUOI DUNG PHIA PROVIDER BEN NGOAI
    @Column(name = "PROVIDER_ID")
    private String providerId;
    // BAT/TAT XAC THUC OTP
    @Column(name = "MFA_ENABLED")
    private boolean mfaEnabled = false;
    // SECRET KEY TOTP - DUOC TAO KHI USER SETUP GOOGLE AUTHENTICATOR
    @Column(name = "MFA_SECRET")
    private String mfaSecret;
    // TRANG THAI KHOA TAI KHOAN
    @Column(name = "IS_LOCKED")
    private boolean isLocked = false;
    // SO LAN DANG NHAP SAI LIEN TIEP
    @Column(name = "FAILED_LOGIN_ATTEMPTS")
    private int failedLoginAttempts = 0;
    // THOI DIEM DANG NHAP CUOI CUNG
    @Column(name = "LAST_LOGIN_AT")
    private LocalDateTime lastLoginAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
