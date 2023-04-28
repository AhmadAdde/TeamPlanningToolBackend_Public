package com.example.TeamPlaningToolBackend.services;

import com.example.TeamPlaningToolBackend.entities.Person;
import com.example.TeamPlaningToolBackend.utils.PersonDTO;

import java.util.List;

public interface PersonService {
    void readFileToDatabase(String filename);
    List<PersonDTO> getAllPeople();
    PersonDTO getPersonByUsername(String username);
}
