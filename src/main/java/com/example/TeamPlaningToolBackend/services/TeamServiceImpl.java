package com.example.TeamPlaningToolBackend.services;

import com.example.TeamPlaningToolBackend.utils.MemberMetaDataDTO;
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
                                    .build()
                    );
                });
            }

            System.out.println("HEJSN");
           team.getMetaData().forEach(obj ->{
                    System.out.println("KEBAB" + obj.getTeamName() + " " + obj.getUsername() + " " + obj.getAvailability());
                    PersonMetaData metaData = PersonMetaData.builder()
                             .username(obj.getUsername())
                             .teamName(obj.getTeamName())
                             .availability(obj.getAvailability())
                             .role(obj.getRole())
                             .build();
                     personMetaDataRepository.save(metaData);
              });
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

            ArrayList<PersonMetaData> personMetaMap = new ArrayList<>(personMetaDataRepository.findAllByTeamName(team.getTeamName()));

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
            personMetaDataRepository.deleteByTeamName(teamName);
        });
    }

    @Override
    public void deleteSavedDatas(List<TeamDTO> teamNames) {
        for(TeamDTO team: teamNames) {
            for (PersonMetaData metaData: team.getMetaData()) {
                personMetaDataRepository.deleteByTeamNameAndUsername(team.getTeamName(), metaData.getUsername());
            }
        }
    }

    @Override
    public void readIRMSheet(String path) throws IOException {
        FileInputStream fis = new FileInputStream(path);
        String password = "sample";
        XSSFWorkbook wb = (XSSFWorkbook) WorkbookFactory.create(fis, password);

        XSSFSheet sheet = wb.getSheetAt(0);

        Map<String, Team> teamsMap = new HashMap<>();
        Map<String, ArrayList<MemberMetaDataDTO>> memberTeamMap = new HashMap<>();

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

            if(!memberTeamMap.containsKey(name)) memberTeamMap.put(name, new ArrayList<>(List.of(new MemberMetaDataDTO(teamName, role, availability))));
            else memberTeamMap.get(name).add(new MemberMetaDataDTO(teamName, role, availability));
        }

        for(String name: memberTeamMap.keySet()) {
            ArrayList<MemberMetaDataDTO> memberMetaDataDTOS = memberTeamMap.get(name);
            System.out.println("MEMERINFOS"+ memberMetaDataDTOS + " : " + name);
            String firstname = name.split(",")[1];
            String lastname = name.split(",")[0];
            Optional<Person> personOptional = personRepository.findByFirstnameAndLastname(firstname, lastname);
            Person person;
            if(personOptional.isEmpty()) {
                person = new Person(firstname.toLowerCase() + lastname.toLowerCase(), firstname, lastname, new ArrayList<>());
                personRepository.save(person);
            } else {
                person = personOptional.get();
            }

            for(MemberMetaDataDTO memberMetaDataDTO : memberMetaDataDTOS) {
                PersonMetaData personMetaData = new PersonMetaData(person.getUsername(), memberMetaDataDTO.getTeamName(), memberMetaDataDTO.getAvailability(), memberMetaDataDTO.getRole());
                personMetaDataRepository.save(personMetaData);
            }

            for (MemberMetaDataDTO memberMetaDataDTO : memberMetaDataDTOS) {
                Team team = teamsMap.get(memberMetaDataDTO.getTeamName());
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

        for (int rowIndex = 9; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            Cell nameCell = row.getCell(8);
            if(nameCell.getStringCellValue().isEmpty()) break;
            nameCell.setBlank();
            row.getCell(9).setBlank();
            row.getCell(10).setBlank();
            row.getCell(11).setBlank();
        }

        int rowIndex = 9;
        for (Person person: personRepository.findAll()) {
            for (Team team: person.getTeams()) {
                Row row = sheet.getRow(rowIndex);
                row.getCell(8).setCellValue(person.getLastname() + ", " + person.getFirstname());
                row.getCell(9).setCellValue(team.getTeamName());
                Optional<PersonMetaData> metaData = personMetaDataRepository.findAllByTeamNameAndUsername(team.getTeamName(), person.getUsername());
                if(metaData.isPresent()) {
                    StringBuilder role = new StringBuilder();
                    for(Role r: metaData.get().getRole()) role.append(r.toString()).append("/");
                    row.getCell(10).setCellValue(role.toString());
                    row.getCell(11).setCellValue(metaData.get().getAvailability() * 0.4);
                }
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
