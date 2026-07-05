package org.example.enterprisedigitalbankingsystem.auth.repository;

import org.example.enterprisedigitalbankingsystem.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsrerRepository extends JpaRepository<User, UUID> {

}
