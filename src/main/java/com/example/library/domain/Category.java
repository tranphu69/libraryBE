package com.example.library.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CATEGORY")
public class Category {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "CODE")
    private String code;
    @Column(name = "NAME")
    private String name;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "PARENT_ID")
    private Long parentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID", insertable = false, updatable = false)
    private Category parent;
    @OneToMany(mappedBy = "parent")
    private Set<Category> children;
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
