package com.example.TeamPlaningToolBackend.services;

import com.example.TeamPlaningToolBackend.entities.Person;
import com.example.TeamPlaningToolBackend.entities.Team;
import com.example.TeamPlaningToolBackend.repository.PersonRepository;
import com.example.TeamPlaningToolBackend.repository.TeamRepository;
import com.example.TeamPlaningToolBackend.utils.AddMemberRequest;
import com.example.TeamPlaningToolBackend.utils.PersonDTO;
import com.example.TeamPlaningToolBackend.utils.TeamDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public void createTeam(TeamDTO team) {
        List<Person> members = new ArrayList<>();

        if (team.getMembers() != null) {
            team.getMembers().forEach(obj ->
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

        PersonDTO scrumMasterDTO = personService.getPersonByUsername(team.getScrumMaster());
        Person scrumMaster = Person.builder()
                .username(scrumMasterDTO.getUsername())
                .firstname(scrumMasterDTO.getFirstname())
                .lastname(scrumMasterDTO.getLastname())
                .role(scrumMasterDTO.getRole())
                .build();
        members.add(scrumMaster);

        Team newTeam = Team.builder()
                .members(members)
                .teamName(team.getTeamName())
                .scrumMaster(team.getScrumMaster())
                .build();

        teamRepository.save(newTeam);
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
                    .members(members)
                    .scrumMaster(team.getScrumMaster())
                    .teamName(team.getTeamName())
                    .build();

            listOfTeam.add(newTeam);
        }

        return listOfTeam;
    }

    @Override
    public void addMember(AddMemberRequest member) {
        Team team = teamRepository.findById(member.getTeamName()).get();
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
        Optional<Team> team = teamRepository.findById(member.getTeamName());
        Person person = personRepository.findById(member.getMemberName()).get();

        if (team.isEmpty() || person == null) return;

        team.get().getMembers().remove(person);

        team.get().getMembers().forEach(ob -> System.out.println(ob.getUsername()));
        teamRepository.save(team.get());
    }

    @Override
    public void deleteTeam(String teamName) {
        Optional<Team> teamsDB = teamRepository.findById(teamName);
        if (teamsDB.isEmpty()) return;
        teamRepository.deleteById(teamsDB.get().getTeamName());
    }
}
