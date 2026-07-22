package com.example.library.dto.request;

import com.example.library.domain.Category;
import com.example.library.domain.Publisher;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {
    private Long id;
    private String isbn;
    private String title;
    private String description;
    private Long publisher;
    private Long category;
    private String language;
    private LocalDate publishedDate;
    private Integer pageCount;
    private String coverImageUrl;
    private String edition;
    private Integer totalCopies;
    private Integer availableCopies;
}
