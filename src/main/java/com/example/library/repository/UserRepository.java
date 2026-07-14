package com.example.library.repository;

import com.example.library.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository  extends JpaRepository<User, String>, JpaSpecificationExecutor<User>, UserRepositoryCustom {
    @Query("SELECT u.code FROM User u WHERE u.isDeleted <> true")
    Set<String> findAllCodes();

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.code = :code AND u.isDeleted <> true")
    boolean existsActiveCode(@Param("code")String code);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.code = :code AND u.isDeleted <> true AND u.id <> :id")
    boolean existsActiveCodeAndNotId(@Param("code")String code, @Param("id")String id);

    Optional<User> findByIdAndIsDeletedNot(String id, boolean isDeleted);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE u.code = :code AND u.isDeleted <> true")
    Optional<User> findByCodeAndIsDeletedNot(String code, boolean isDeleted);

    @Query("SELECT u FROM User u JOIN u.roles r " +
            "WHERE (:code IS NULL OR :code = '' OR LOWER(u.code) LIKE LOWER(CONCAT('%', :code, '%'))) " +
            "AND (:fullName IS NULL OR :fullName = '' OR u.fullName LIKE CONCAT('%', :fullName, '%')) " +
            "AND (:email IS NULL OR :email = '' OR u.email LIKE CONCAT('%', :email, '%')) " +
            "AND (:listRole IS NULL OR r.id IN :listRole) AND u.isDeleted <> true")
    Page<User> search(@Param("code")String code, @Param("fullName") String fullName, @Param("email") String email,
                      @Param("listRole") Set<Long> listRole, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r " +
            "WHERE (:code IS NULL OR :code = '' OR LOWER(u.code) LIKE LOWER(CONCAT('%', :code, '%'))) " +
            "AND (:fullName IS NULL OR :fullName = '' OR u.fullName LIKE CONCAT('%', :fullName, '%')) " +
            "AND (:email IS NULL OR :email = '' OR u.email LIKE CONCAT('%', :email, '%')) " +
            "AND (:listRole IS NULL OR r.id IN :listRole) AND u.isDeleted <> true")
    List<User> searchExport(@Param("code")String code, @Param("fullName") String fullName, @Param("email") String email, @Param("listRole") Set<Long> listRole);

    @Query("SELECT r.code FROM User u JOIN u.roles r WHERE u.id = :userId AND r.status <> -1")
    Set<String> getRoleCodesByUserId(@Param("userId") String userId);
}
