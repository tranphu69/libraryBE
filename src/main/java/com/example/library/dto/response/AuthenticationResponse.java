package com.example.library.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private boolean mfaRequired;
    private String challengeToken;
}
