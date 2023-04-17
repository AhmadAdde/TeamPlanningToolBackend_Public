package com.example.TeamPlaningToolBackend.DB;

import com.example.TeamPlaningToolBackend.DB.PersonDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<PersonDB, String> {
}
