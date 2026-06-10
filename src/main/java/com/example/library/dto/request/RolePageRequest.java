package com.example.library.dto.request;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePageRequest extends BasePageRequest{
    private String code;
    private String name;
    private Long status;
    private Set<Long> listPermission;
}
