package com.example.TeamPlaningToolBackend.repository;

import com.example.TeamPlaningToolBackend.entities.PersonMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;

import java.util.ArrayList;
import java.util.Optional;

@Repository
public interface PersonMetaDataRepository extends JpaRepository<PersonMetaData, String> {
    ArrayList<PersonMetaData> findAllByTeamName(String teamName);
    Optional<PersonMetaData> findAllByTeamNameAndUsername(String teamName, String username);
    @Modifying
    @Transactional
    int deleteByTeamName(String teamName);
    @Modifying
    @Transactional
    int deleteByTeamNameAndUsername(String teamName, String Username);
}
