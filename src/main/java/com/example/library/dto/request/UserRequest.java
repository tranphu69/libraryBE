package com.example.library.dto.request;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    private String id;
    private String code;
    private String fullName;
    private String password;
    private Set<String> listRole;
    private Boolean mfaEnabled;
    private Boolean isLocked;
}
