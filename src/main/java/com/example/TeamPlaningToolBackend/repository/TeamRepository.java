package com.example.TeamPlaningToolBackend.repository;

import com.example.TeamPlaningToolBackend.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {
}
