package com.example.library.dto.request;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequest {
    private String id;
    private String code;
    private String name;
    private String description;
    private Long status;
    private Set<Long> listPermission;
}
