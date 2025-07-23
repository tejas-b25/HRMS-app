package com.quantumsoft.hrms.repository;

import com.quantumsoft.hrms.entity.User;
import com.quantumsoft.hrms.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    public Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);


    List<User> findByRole(String role);
}
