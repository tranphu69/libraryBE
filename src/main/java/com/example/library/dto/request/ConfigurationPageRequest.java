package com.example.library.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigurationPageRequest extends BasePageRequest{
    private String code;
    private String name;
    private String description;
    private String type;
}
