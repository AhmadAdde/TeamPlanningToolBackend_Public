package com.example.TeamPlaningToolBackend.services;

import com.example.TeamPlaningToolBackend.entities.Person;
import com.example.TeamPlaningToolBackend.repository.PersonRepository;
import com.example.TeamPlaningToolBackend.utils.PersonDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService{

    @Autowired
    private final PersonRepository personRepository;

    @Override
    public void readFileToDatabase(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))){
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");

                Person person = Person.builder()
                        .firstname(parts[2])
                        .lastname(parts[3])
                        .username(parts[4])
                        .build();

                personRepository.save(person);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public List<PersonDTO> getAllPeople() {
        List<PersonDTO> personList = new ArrayList<>();

        for (Person person: personRepository.findAll()) {
            List<String> teams = new ArrayList<>();
            person.getTeams().forEach(obj -> teams.add(obj.getTeamName()));
            PersonDTO p = PersonDTO.builder()
                    .username(person.getUsername())
                    .firstname(person.getFirstname())
                    .lastname(person.getLastname())
                    .role(person.getRole())
                    .teams(teams)
                    .build();

            personList.add(p);
        }

        return personList;
    }

    @Override
    public PersonDTO getPersonByUsername(String username) {
        Person person = personRepository.findById(username).get();

        List<String> teams = new ArrayList<>();
        person.getTeams().forEach(obj -> teams.add(obj.getTeamName()));

        return PersonDTO.builder()
                .username(person.getUsername())
                .firstname(person.getFirstname())
                .lastname(person.getLastname())
                .teams(teams)
                .build();
    }

}
