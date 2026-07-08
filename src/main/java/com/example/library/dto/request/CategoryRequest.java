package com.example.library.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long parentId;
}
