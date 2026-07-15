package com.example.library.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublisherRequest {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
}
