package com.example.library.repository;

import com.example.library.domain.Configuration;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long>, JpaSpecificationExecutor<Configuration> {
    @Query("SELECT COUNT(c) > 0 FROM Configuration c WHERE c.code = :code AND c.isDeleted <> true AND c.type = :type")
    boolean existsActiveCode(@Param("code")String code, @Param("type")String type);

    @Query("SELECT COUNT(c) > 0 FROM Configuration c WHERE c.code = :code AND c.isDeleted <> true AND c.type = :type AND c.id <> :id")
    boolean existsActiveCodeAndNotId(@Param("code")String code, @Param("type")String type, @Param("id")Long id);

    Optional<Configuration> findByIdAndIsDeletedNot(Long id, boolean isDeleted);

    @Query("SELECT c FROM Configuration c WHERE " +
            "c.isDeleted = false " +
            "AND (:code IS NULL OR :code = '' OR LOWER(c.code) LIKE LOWER(CONCAT('%', :code, '%'))) " +
            "AND (:name IS NULL OR :name = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:description IS NULL OR :description = '' OR LOWER(c.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
            "AND (:type IS NULL OR :type = '' OR LOWER(c.type) LIKE LOWER(CONCAT('%', :type, '%'))) ")
    Page<Configuration> search(@Param("code") String code, @Param("name") String name, @Param("description") String description,
                        @Param("type") String type, Pageable pageable);
}
