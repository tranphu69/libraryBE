package com.example.library.dto.request;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPageRequest extends BasePageRequest{
    private String code;
    private String fullName;
    private Set<Long> listRole;
    private String email;
}
