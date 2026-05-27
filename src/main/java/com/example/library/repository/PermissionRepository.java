package com.example.library.repository;

import com.example.library.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    @Query(value = "SELECT code FROM roles", nativeQuery = true)
    List<String> findAllCodes();
}
