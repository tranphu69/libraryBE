package com.example.library.repository;

import com.example.library.domain.Publisher;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long>, JpaSpecificationExecutor<Publisher> {
    Optional<Publisher> findByIdAndIsDeletedNot(Long id, boolean isDeleted);

    @Query("SELECT p FROM Publisher p WHERE " +
            "p.isDeleted = false " +
            "AND (:name IS NULL OR :name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:address IS NULL OR :address = '' OR LOWER(p.address) LIKE LOWER(CONCAT('%', :address, '%'))) " +
            "AND (:email IS NULL OR :email = '' OR LOWER(p.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "AND (:phone IS NULL OR :phone = '' OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :phone, '%'))) ")
    Page<Publisher> search(@Param("name") String name, @Param("address") String address, @Param("email") String email,
                           @Param("phone") String phone, Pageable pageable);
}
