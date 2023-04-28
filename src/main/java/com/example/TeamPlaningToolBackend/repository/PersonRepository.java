package com.example.TeamPlaningToolBackend.repository;

import com.example.TeamPlaningToolBackend.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, String> {
    Optional<Person> findByFirstnameAndLastname(String firstname, String lastname);
}
