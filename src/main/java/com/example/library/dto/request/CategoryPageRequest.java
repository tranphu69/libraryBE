package com.example.library.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryPageRequest extends BasePageRequest{
    private String code;
    private String name;
}
