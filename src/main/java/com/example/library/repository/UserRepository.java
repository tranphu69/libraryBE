package com.example.library.repository;

import com.example.library.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface UserRepository  extends JpaRepository<User, String>, JpaSpecificationExecutor<User>, UserRepositoryCustom {
    @Query("SELECT u.code FROM User u WHERE u.isDeleted <> true")
    Set<String> findAllCodes();

    @Query("SELECT u.email FROM User u WHERE u.isDeleted <> true")
    Set<String> findAllEmails();
}
