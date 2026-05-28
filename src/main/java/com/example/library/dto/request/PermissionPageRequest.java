package com.example.library.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionPageRequest extends BasePageRequest{
    private String code;
    private String name;
    private Long status;
}
