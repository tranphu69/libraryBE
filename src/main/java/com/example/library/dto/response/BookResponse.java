package com.example.library.dto.response;

import com.example.library.domain.Category;
import com.example.library.domain.Publisher;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
    private Long id;
    private String isbn;
    private String title;
    private String description;
    private Publisher publisher;
    private Category category;
    private String language;
    private LocalDate publishedDate;
    private Integer pageCount;
    private String coverImageUrl;
    private String edition;
    private Integer totalCopies;
    private Integer availableCopies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
