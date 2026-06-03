package com.example.library.dto.response;

import com.example.library.domain.Permission;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {
    private String id;
    private String code;
    private String name;
    private String description;
    private Long status;
    private Set<SimpleResponse> listPermission;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
