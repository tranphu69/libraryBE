package com.example.library.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorPageRequest extends BasePageRequest{
    private String code;
    private String name;
    private String dateBirth;
    private String dateDeath;
    private String nationality;
}
