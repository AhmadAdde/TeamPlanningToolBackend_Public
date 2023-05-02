package com.example.TeamPlaningToolBackend.repository;

import com.example.TeamPlaningToolBackend.entities.Person;
import com.example.TeamPlaningToolBackend.entities.PersonMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;

@Repository
public interface PersonMetaDataRepository extends JpaRepository<PersonMetaData, String> {
    ArrayList<PersonMetaData> findAllByTeamName(String teamName);
}
