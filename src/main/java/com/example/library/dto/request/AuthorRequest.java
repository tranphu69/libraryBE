package com.example.library.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorRequest {
    private Long id;
    private String code;
    private String name;
    private String dateBirth;
    private String dateDeath;
    private String nationality;
    private String biography;
    private String imageURL;
}
