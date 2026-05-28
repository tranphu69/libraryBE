package com.example.library.repository;

import com.example.library.domain.Permission;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    @Query("SELECT p.code FROM Permission p WHERE p.status <> -1 ")
    List<String> findAllCodes();

    @Query("SELECT p.code FROM Permission p WHERE p.status <> -1 AND p.publicId <> :id")
    List<String> findAllCodesOtherPublicId(String id);

    Optional<Permission> findByPublicId(String id);

    @Query("SELECT p FROM Permission p WHERE p.status = 1")
    List<Permission> getAllStatusActive();

    @Query("SELECT p FROM Permission p " +
            "WHERE (:code IS NULL OR :code = '' OR LOWER(p.code) LIKE LOWER(CONCAT('%', :code, '%'))) " +
            "AND (:name IS NULL OR :name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND p.status <> -1")
    List<Permission> searchExport(@Param("code") String code, @Param("name") String name, @Param("status") Long status);

    @Query("SELECT p FROM Permission p " +
            "WHERE (:code IS NULL OR :code = '' OR LOWER(p.code) LIKE LOWER(CONCAT('%', :code, '%'))) " +
            "AND (:name IS NULL OR :name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND p.status <> -1")
    Page<Permission> search(@Param("code") String code, @Param("name") String name,
                            @Param("status") Long status, Pageable pageable);
}
