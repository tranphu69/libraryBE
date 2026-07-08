package com.example.library.repository;

import com.example.library.domain.Category;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.code = :code AND c.isDeleted <> true")
    boolean existsActiveCode(@Param("code")String code);

    boolean existsByIdAndIsDeletedNot(Long id, boolean isDeleted);

    Optional<Category> findByIdAndStatusNot(Long id, boolean isDeleted);
}
