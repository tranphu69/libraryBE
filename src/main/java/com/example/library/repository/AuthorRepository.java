package com.example.library.repository;

import com.example.library.domain.Author;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long>, JpaSpecificationExecutor<Author> {
    @Query("SELECT COUNT(a) > 0 FROM Author a WHERE a.code = :code AND a.isDeleted <> true")
    boolean existsActiveCode(@Param("code")String code);

    @Query("SELECT COUNT(a) > 0 FROM Author a WHERE a.code = :code AND a.isDeleted <> true AND a.id <> :id")
    boolean existsActiveCodeAndNotId(@Param("code")String code, @Param("id")Long id);

    Optional<Author> findByIdAndIsDeletedNot(Long id, boolean isDeleted);

    @Query("SELECT a FROM Author a WHERE " +
            "a.isDeleted = false " +
            "AND (:code IS NULL OR :code = '' OR LOWER(a.code) LIKE LOWER(CONCAT('%', :code, '%'))) " +
            "AND (:name IS NULL OR :name = '' OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:nationality IS NULL OR :nationality = '' OR LOWER(a.nationality) LIKE LOWER(CONCAT('%', :nationality, '%'))) " +
            "AND (:dateBirth IS NULL OR a.dateOfBirth >= :dateBirth) " +
            "AND (:dateDeath IS NULL OR a.dateOfDeath <= :dateDeath)")
    Page<Author> search(@Param("code") String code, @Param("name") String name, @Param("nationality") String nationality,
                        @Param("dateBirth") String dateBirth, @Param("dateDeath") String dateDeath, Pageable pageable);
}
