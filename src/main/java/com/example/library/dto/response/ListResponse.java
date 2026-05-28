package com.example.library.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListResponse<T> {
    private List<T> content;
    private int total;
}
