package com.example.library.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "REFRESH_TOKEN")
public class RefreshToken {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "TOKEN_HASH")
    private String tokenHash;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;
    @Column(name = "EXPIRES_AT")
    private Instant expiresAt;
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
}
