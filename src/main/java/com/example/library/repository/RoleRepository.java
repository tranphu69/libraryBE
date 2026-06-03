package com.example.library.repository;

import com.example.library.domain.Permission;
import com.example.library.domain.Role;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    @Query("SELECT r.code FROM Role r WHERE r.status <> -1 ")
    Set<String> findAllCodes();

    @Query("SELECT r.code FROM Role r WHERE r.status <> -1 AND r.publicId <> :id")
    Set<String> findAllCodesOtherPublicId(String id);

    Optional<Role> findByPublicIdAndStatusNot(String id, Long status);

    @Query("SELECT count(r) > 0 FROM Role r JOIN r.permissions p WHERE p.id = :permissionId AND r.status <> -1")
    boolean existsByPermissionId(Long permissionId);

    @Query("SELECT r FROM Role r JOIN r.permissions p " +
            "WHERE (:code IS NULL OR :code = '' OR LOWER(r.code) LIKE LOWER(CONCAT('%', :code, '%'))) " +
            "AND (:name IS NULL OR :name = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND r.status <> -1 AND (:listPermission IS NULL OR p.id IN :listPermission)")
    Page<Role> search(@Param("code") String code, @Param("name") String name, @Param("listPermission") Set<Long> listPermission,
                            @Param("status") Long status, Pageable pageable);
}
