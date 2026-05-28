package com.example.library.dto.response;

import lombok.*;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationMeta {
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private Boolean first;
    private Boolean last;

    public static PaginationMeta of(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
