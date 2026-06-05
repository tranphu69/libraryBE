package com.example.library.repository;

import com.example.library.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository  extends JpaRepository<User, String>, JpaSpecificationExecutor<User>, UserRepositoryCustom {

}
