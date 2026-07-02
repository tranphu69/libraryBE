package com.example.library.dto.Object;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpChallenge {
    private String userId;
    private String otpHash;
    private int attemptCount;
}
