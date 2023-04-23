package com.example.TeamPlaningToolBackend.repository;

import com.example.TeamPlaningToolBackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
