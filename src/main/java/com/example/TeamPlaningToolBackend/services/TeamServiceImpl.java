package com.example.TeamPlaningToolBackend.services;

import com.example.TeamPlaningToolBackend.entities.MemberInfo;
import com.example.TeamPlaningToolBackend.entities.Person;
import com.example.TeamPlaningToolBackend.entities.PersonMetaData;
import com.example.TeamPlaningToolBackend.entities.Team;
import com.example.TeamPlaningToolBackend.enums.Role;
import com.example.TeamPlaningToolBackend.repository.PersonMetaDataRepository;
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
    private final PersonMetaDataRepository personMetaDataRepository;
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
            Map<String, PersonMetaData> personMetaMap = new HashMap<>();

            for(PersonMetaData metaData: personMetaDataRepository.findAllByTeamName(team.getTeamName())) {
                personMetaMap.put(metaData.getUsername(), metaData);
            }

            team.getMembers().forEach(obj ->
                    members.add((obj.getUsername()
            )));

            TeamDTO newTeam = TeamDTO.builder()
                    .userIds(members)
                    .teamName(team.getTeamName())
                    .metaData(personMetaMap)
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
        String password = "sample";
        XSSFWorkbook wb = (XSSFWorkbook) WorkbookFactory.create(fis, password);

        XSSFSheet sheet = wb.getSheetAt(0);

        Map<String, Team> teamsMap = new HashMap<>();
        Map<String, ArrayList<MemberInfo>> memberTeamMap = new HashMap<>();

        for (int rowIndex = 9; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            String name = row.getCell(8).getStringCellValue();
            String teamName = row.getCell(9).getStringCellValue();
            String roleStr = row.getCell(10).getStringCellValue();
            Role role = convertStringToRole(roleStr); // Add a method to convert the string to Role enum
            float convertedAvailability = (float) row.getCell(11).getNumericCellValue();
            int availability = convertAvailability(convertedAvailability);
            System.out.println("UTANFLR FUNKTION: " + roleStr);
            if(name.isEmpty()) break;

            if(!teamsMap.containsKey(teamName)) {
                teamsMap.put(teamName, new Team(teamName, "", new ArrayList<>()));
            }

            if(!memberTeamMap.containsKey(name)) memberTeamMap.put(name, new ArrayList<>(List.of(new MemberInfo(teamName, role, availability))));
            else memberTeamMap.get(name).add(new MemberInfo(teamName, role, availability));
        }

        for(String name: memberTeamMap.keySet()) {
            ArrayList<MemberInfo> memberInfos = memberTeamMap.get(name);
            System.out.println("MEMERINFOS"+memberInfos + " : " + name);
            String firstname = name.split(",")[1];
            String lastname = name.split(",")[0];
            Optional<Person> personOptional = personRepository.findByFirstnameAndLastname(firstname, lastname);
            Person person;
            if(personOptional.isEmpty()) {
                person = new Person(firstname.toLowerCase() + lastname.toLowerCase(), firstname, lastname, memberInfos.get(0).getRole(), new ArrayList<>());
                personRepository.save(person);
            } else {
                person = personOptional.get();
            }

            for(MemberInfo memberInfo : memberInfos) {
                PersonMetaData personMetaData = new PersonMetaData(person.getUsername(), memberInfo.getTeamName(), memberInfo.getAvailability());
                personMetaDataRepository.save(personMetaData);
            }

            for (MemberInfo memberInfo : memberInfos) {
                Team team = teamsMap.get(memberInfo.getTeamName());
                team.getMembers().add(person);
            }
        }

        for(String teamName: teamsMap.keySet()) teamRepository.save(teamsMap.get(teamName));
    }
    private int convertAvailability(float availability) {
        float ava = (availability/40) * 100;

        return (int) ava;
    }
    private Role convertStringToRole(String roleStr) {
        System.out.println("I FUNKTION: " + roleStr);
        if ("Development Engineer (Client/Server SW)".equalsIgnoreCase(roleStr) || roleStr.isEmpty()) {
            return Role.DEVELOPMENT_ENGINEER;
        } else if ("Technical Product Owner".equalsIgnoreCase(roleStr)) {
            return Role.TECHNICAL_PRODUCT_OWNER;
        } else if ("Requirements Engineer/Systems Architect".equalsIgnoreCase(roleStr)) {
            return Role.REQUIREMENTS_ENGINEER_SYSTEMS_ARCHITECT;
        } else if ("DevOps Engineer".equalsIgnoreCase(roleStr)) {
            return Role.DEVOPS_ENGINEER;
        } else if ("Line Manager".equalsIgnoreCase(roleStr)) {
            return Role.LINE_MANAGER;
        } else if ("Scrum Master".equalsIgnoreCase(roleStr)) {
            return Role.SCRUM_MASTER;
        } else {
            throw new IllegalArgumentException("Unknown role: " + roleStr);
        }

    }
    @Override
    public void updateIRMSheet(String path) throws IOException {
        FileInputStream fis = new FileInputStream(path);
        String password = "sample";
        XSSFWorkbook wb = (XSSFWorkbook) WorkbookFactory.create(fis, password);

        XSSFSheet sheet = wb.getSheetAt(0);
        Map<String, Team> teamsMap = new HashMap<>();
        Map<String, ArrayList<String>> memberTeamMap = new HashMap<>();

        for (int rowIndex = 9; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            Cell nameCell = row.getCell(8);
            if(nameCell.getStringCellValue().isEmpty()) break;
            nameCell.setBlank();
            row.getCell(9).setBlank();

        }
        int rowIndex = 9;
        for (Person person: personRepository.findAll()) {

            for (Team team: person.getTeams()) {
                Row row = sheet.getRow(rowIndex);
                row.getCell(8).setCellValue(person.getLastname() + ", " + person.getFirstname());
                row.getCell(9).setCellValue(team.getTeamName());
                rowIndex++;
            }

        }
        fis.close();
        FileOutputStream fos = new FileOutputStream(path);
        wb.write(fos);
        wb.close();
        fos.close();
    }


}
