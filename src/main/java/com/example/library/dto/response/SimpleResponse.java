package com.example.library.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleResponse {
    private String id;
    private String code;
    private String name;
}
