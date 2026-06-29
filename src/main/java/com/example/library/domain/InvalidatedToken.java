package com.example.library.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "INVALIDATED_TOKEN")
public class InvalidatedToken {
    @Id
    @Column(name = "JTI")
    private String jti;
    @Column(name = "EXPIRES_AT")
    private Instant expiresAt;
}
