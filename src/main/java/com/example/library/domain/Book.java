package com.example.library.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "BOOK")
public class Book {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ISBN")
    private String isbn;
    @Column(name = "TITLE")
    private String title;
    @Column(name = "DESCRIPTION")
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PUBLISHER_ID")
    private Publisher publisher;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;
    @Column(name = "LANGUAGE")
    private String language;
    @Column(name = "PUBLISHED_DATE")
    private LocalDate publishedDate;
    @Column(name = "PAGE_COUNT")
    private Integer pageCount;
    @Column(name = "COVER_IMAGE_URL")
    private String coverImageUrl;
    @Column(name = "EDITION")
    private String edition;
    @Column(name = "TOTAL_COPIES")
    @Builder.Default
    private Integer totalCopies = 0;
    @Column(name = "AVAILABLE_COPIES")
    @Builder.Default
    private Integer availableCopies = 0;
    @Column(name = "IS_DELETED")
    private Boolean isDeleted = false;
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
    @Column(name = "CREATED_BY")
    private String createdBy;
    @Column(name = "UPDATED_BY")
    private String updatedBy;
}
