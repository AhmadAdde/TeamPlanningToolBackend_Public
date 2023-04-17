package com.example.TeamPlaningToolBackend.DB;

import com.example.TeamPlaningToolBackend.DB.TeamsDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamsRepository extends JpaRepository<TeamsDB, String> {
    public Optional<TeamsDB> findByteamName(String teamName);
}
