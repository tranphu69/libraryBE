package com.example.library.repository;

import com.example.library.domain.Category;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.code = :code AND c.isDeleted <> true")
    boolean existsActiveCode(@Param("code")String code);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.code = :code AND c.isDeleted <> true AND c.id <> :id")
    boolean existsActiveCodeAndNotId(@Param("code")String code, @Param("id")Long id);

    boolean existsByIdAndIsDeletedNot(Long id, boolean isDeleted);

    Optional<Category> findByIdAndIsDeletedNot(Long id, boolean isDeleted);

    List<Category> findAllByIsDeletedNot(boolean isDeleted);

    @Query("""
        SELECT c FROM Category c
        WHERE (:code IS NULL OR LOWER(c.code) LIKE LOWER(CONCAT('%', :code, '%')))
          AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
        """)
    List<Category> search(@Param("code") String code, @Param("name") String name);

    @Query(value = """
        WITH RECURSIVE category_ancestors AS (
            SELECT * FROM category WHERE id IN (:ids)
            UNION ALL
            SELECT c.* FROM category c
            INNER JOIN category_ancestors ca ON c.id = ca.parent_id
        )
        SELECT DISTINCT * FROM category_ancestors
        """, nativeQuery = true)
    List<Category> findAllWithAncestors(@Param("ids") List<Long> ids);
}
