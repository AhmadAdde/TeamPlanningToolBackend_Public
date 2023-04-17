package com.example.TeamPlaningToolBackend.Bo;

import com.example.TeamPlaningToolBackend.DB.PersonDB;
import com.example.TeamPlaningToolBackend.DB.PersonRepository;
import com.example.TeamPlaningToolBackend.DB.TeamsDB;
import com.example.TeamPlaningToolBackend.DB.TeamsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Service
public class PersonService implements IPersonService{

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private TeamsRepository teamsRepository;


    public void readFileToDatabase(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))){
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                PersonDB person = new PersonDB();
                person.setFirstname(parts[2]);
                person.setLastname(parts[3]);
                person.setUsername(parts[4]);
                String teamName = parts[1];
                System.out.println(person);
                if (teamsRepository != null) {
                    TeamsDB team = teamsRepository.findByteamName(teamName)
                            .orElseGet(() -> {
                                TeamsDB newTeam = new TeamsDB();
                                newTeam.setTeamName(teamName);
                                return teamsRepository.save(newTeam);
                            });

                    // add the team to the user
                    person.getTeam().add(team);
                    personRepository.save(person);
                } else {
                    // Handle the case when teamsRepository is null
                    // e.g. throw an exception, instantiate it, or use a default value
                    // Example:
                    throw new RuntimeException("teamsRepository is null");
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
