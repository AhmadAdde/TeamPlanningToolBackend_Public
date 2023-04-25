package com.example.TeamPlaningToolBackend.services;

import com.example.TeamPlaningToolBackend.entities.Person;
import com.example.TeamPlaningToolBackend.entities.Team;
import com.example.TeamPlaningToolBackend.repository.PersonRepository;
import com.example.TeamPlaningToolBackend.repository.TeamRepository;
import com.example.TeamPlaningToolBackend.utils.AddMemberRequest;
import com.example.TeamPlaningToolBackend.utils.PersonDTO;
import com.example.TeamPlaningToolBackend.utils.TeamDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService{

    @Autowired
    private final TeamRepository teamRepository;
    @Autowired
    private final PersonRepository personRepository;
    @Autowired
    private final PersonService personService;

    @Override
    public void createTeam(List<TeamDTO> teams) {
        teams.forEach(team -> {
            List<Person> members = new ArrayList<>();

            if (team.getUserIds() != null) {
                team.getUserIds().forEach(obj ->
                {
                    PersonDTO personDTO = personService.getPersonByUsername(obj);
                    members.add(
                            Person.builder()
                                    .username(personDTO.getUsername())
                                    .firstname(personDTO.getFirstname())
                                    .lastname(personDTO.getLastname())
                                    .role(personDTO.getRole())
                                    .build()
                    );
                });
            }

            /*
            PersonDTO scrumMasterDTO = personService.getPersonByUsername(team.getScrumMaster());
            Person scrumMaster = Person.builder()
                    .username(scrumMasterDTO.getUsername())
                    .firstname(scrumMasterDTO.getFirstname())
                    .lastname(scrumMasterDTO.getLastname())
                    .role(scrumMasterDTO.getRole())
                    .build();
            members.add(scrumMaster);
*/

            Team newTeam = Team.builder()
                    .members(members)
                    .teamName(team.getTeamName())
                    //.scrumMaster(team.getScrumMaster())
                    .build();

            teamRepository.save(newTeam);
        });
    }

    @Override
    public ArrayList<TeamDTO> getAllTeams() {
        ArrayList<TeamDTO> listOfTeam = new ArrayList<>();

        for (Team team: teamRepository.findAll()) {
            List<String> members = new ArrayList<>();

            team.getMembers().forEach(obj ->
                    members.add((obj.getUsername()
            )));
            TeamDTO newTeam = TeamDTO.builder()
                    .userIds(members)
                    .scrumMaster(team.getScrumMaster())
                    .teamName(team.getTeamName())
                    .build();

            listOfTeam.add(newTeam);
        }

        return listOfTeam;
    }

    @Override
    public void addMember(AddMemberRequest member) {
        Optional<Team> teamOptional = teamRepository.findById(member.getTeamName());
        if(teamOptional.isEmpty()) return;
        Team team = teamOptional.get();
        PersonDTO personDTO = personService.getPersonByUsername(member.getMemberName());

        team.getMembers().add(
                Person.builder()
                        .username(personDTO.getUsername())
                        .firstname(personDTO.getFirstname())
                        .lastname(personDTO.getLastname())
                        .role(personDTO.getRole())
                        .build()
        );

        teamRepository.save(team);
    }

    @Override
    public void deleteMember(AddMemberRequest member) {
        Optional<Team> teamOptional = teamRepository.findById(member.getTeamName());
        Optional<Person> personOptional = personRepository.findById(member.getMemberName());
        if(personOptional.isEmpty() || teamOptional.isEmpty()) return;

        Person person = personOptional.get();
        Team team = teamOptional.get();

        team.getMembers().remove(person);
        team.getMembers().forEach(ob -> System.out.println(ob.getUsername()));

        teamRepository.save(team);
    }

    @Override
    public void deleteTeam(List<String> teamNames) {
        teamNames.forEach(teamName -> {
            Optional<Team> teamsDB = teamRepository.findById(teamName);
            if (teamsDB.isEmpty()) return;
            teamRepository.deleteById(teamsDB.get().getTeamName());
        });
    }

    @Override
    public void readIRMSheet(String path) throws IOException {
        FileInputStream fis = new FileInputStream(path);
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(0);

        Map<String, Team> teamsMap = new HashMap<>();
        Map<String, ArrayList<String>> memberTeamMap = new HashMap<>();

        for (int rowIndex = 10; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            String username = row.getCell(7).getStringCellValue();
            String teamName = row.getCell(8).getStringCellValue();

            if(!teamsMap.containsKey(teamName)) {
                teamsMap.put(teamName, new Team(teamName, "", new ArrayList<>()));
            }

            if(!memberTeamMap.containsKey(username)) memberTeamMap.put(username, new ArrayList<>(List.of(teamName)));
            else memberTeamMap.get(username).add(teamName);
        }

        for(String username: memberTeamMap.keySet()) {
            ArrayList<String> teamName = memberTeamMap.get(username);
            Optional<Person> personOptional = personRepository.findById(username);
            Person person;
            if(personOptional.isEmpty()) {
                person = new Person(username, "", "", null, new ArrayList<>());
                personRepository.save(person);
            } else {
                person = personOptional.get();
            }
            for(String _teamName: teamName) teamsMap.get(_teamName).getMembers().add(person);
        }

        for(String teamName: teamsMap.keySet()) teamRepository.save(teamsMap.get(teamName));
    }
}
