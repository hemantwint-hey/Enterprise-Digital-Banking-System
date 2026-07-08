package org.example.enterprisedigitalbankingsystem.auth.repository;

import org.example.enterprisedigitalbankingsystem.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);
    // orElseThrow return type is optional
    Optional<User> findByEmail(String email);
}
