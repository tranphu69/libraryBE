package com.example.library.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private String code;
    private Boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
