package com.example.TeamPlaningToolBackend.services;

import com.example.TeamPlaningToolBackend.utils.MemberMetaDataDTO;
import com.example.TeamPlaningToolBackend.entities.Person;
import com.example.TeamPlaningToolBackend.entities.PersonMetaData;
import com.example.TeamPlaningToolBackend.entities.Team;
import com.example.TeamPlaningToolBackend.repository.PersonMetaDataRepository;
import com.example.TeamPlaningToolBackend.repository.PersonRepository;
import com.example.TeamPlaningToolBackend.repository.TeamRepository;
import com.example.TeamPlaningToolBackend.utils.AddMemberRequest;
import com.example.TeamPlaningToolBackend.utils.PersonDTO;
import com.example.TeamPlaningToolBackend.utils.TeamDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

import java.lang.reflect.Array;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private static final String irmSheetPath = "src/main/java/com/example/TeamPlaningToolBackend/assets/IRM-Sample.xlsm";
    private static final String irmSheetPassword = "sample";

    private static final String confluenceUrl = "https://teamplaningtool.atlassian.net/wiki/rest/api/content/";
    private static final String apiKey = "ATATT3xFfGF0WAIg2Waax0zpDbLk9wGSUCqkRW22LQtjksEsauUIEpVv_luk9rwLfMJ04KOPZDXshv2tp62jMiSZF790x9A_YhtR_6IlsLu7FKO1gqhF6jx4m_8qCtNbqpUUiXwvS1ij9u9-P6KCP27pMsHLkynijD-J2Xn_HDltE1Wmq8q-DiI=AFC54599";
    private static final String email = "vinhed@gmail.com";
    private static HashMap<String, String> teamIds;
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
            String role = row.getCell(10).getStringCellValue().replaceAll("/", "");
            float convertedAvailability = (float) row.getCell(11).getNumericCellValue();
            int availability = convertAvailability(convertedAvailability);
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
                    for(String r: metaData.get().getRole()) role.append(r).append("/");
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

    private String getBasicAuthHeader() {
        String auth = email + ":" + apiKey;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }

    private HashMap<String, String> getChildPageIds() {
        String parentId = "65563";

        HashMap<String, String> teamMap = new HashMap<>();
        try  {
            HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TeamServiceImpl.confluenceUrl + "/search?cql=parent="+ parentId))
                    .header("Authorization", getBasicAuthHeader())
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray json = new JSONObject(resp.body()).getJSONArray("results");

            for(Object a: json) {
                JSONObject teamObj = (JSONObject)a;
                String teamId = teamObj.getString("id");
                String teamName = teamObj.getString("title").split(" - ")[0].replaceAll(" ", "_");
                teamMap.put(teamName, teamId);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        teamIds = (HashMap<String, String>) teamMap.clone();
        return teamMap;
    }

    private ArrayList<ArrayList<String>> getTableFromPageId(String pageId) {
        ArrayList<ArrayList<String>> tableContent = new ArrayList<>();

        HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(confluenceUrl + pageId + "?expand=body.view"))
                .header("Authorization", getBasicAuthHeader())
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> resp = null;
        try {
            resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(resp.body());
            String html = json.getJSONObject("body").getJSONObject("view").getString("value");

            Document doc = Jsoup.parse(html);
            Elements tables = doc.select("tbody");
            for(Element table: tables) {
                String headers = table.childNode(0).toString();
                if(!(headers.contains("Name") && headers.contains("Role") && headers.contains("Availability"))) continue;
                for(int j = 1, i; j < table.children().size(); j++) {
                    Element row = table.child(j);
                    ArrayList<String> cells = new ArrayList<>();
                    for(i = 0; i < 3; i++) {
                        Element cell = row.child(i);
                        if(cell.text().isEmpty()) break;
                        cells.add(cell.text());
                    }
                    if(i != 3) continue;
                    cells.set(2, cells.get(2).replaceAll("%", ""));
                    tableContent.add(cells);
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return tableContent;
    }

    private HashMap<String, ArrayList<ArrayList<String>>> readConfluence() {
        HashMap<String, ArrayList<ArrayList<String>>> teamMemberData = new HashMap<>();
        HashMap<String, String> teams = getChildPageIds();
        for (Map.Entry<String, String> teamData : teams.entrySet()) {
            teamMemberData.put(teamData.getKey(), getTableFromPageId(teamData.getValue()));
        }
        return teamMemberData;
    }

    private HashMap<String, ArrayList<ArrayList<String>>> readIRM() {
        XSSFWorkbook wb = null;
        try {
            FileInputStream fis = new FileInputStream(irmSheetPath);
            wb = (XSSFWorkbook) WorkbookFactory.create(fis, irmSheetPassword);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        XSSFSheet sheet = wb.getSheetAt(0);

        HashMap<String, ArrayList<ArrayList<String>>> teamMemberData = new HashMap<>();

        for (int rowIndex = 9; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            String name = row.getCell(8).getStringCellValue();
            if(name.isEmpty()) break;

            String[] splitName = row.getCell(8).getStringCellValue().split(", ");
            name = splitName[1] + " " + splitName[0];
            if(name.charAt(0) == ' ') name = name.substring(1);
            String teamName = row.getCell(9).getStringCellValue().split(" - ")[0].replaceAll("AirOn360_Team0", "AirOn360_Team").replaceAll("AirOn360_Team", "AirOn360_Team_");
            String role = row.getCell(10).getStringCellValue().replaceAll("/", "");
            float convertedAvailability = (float) row.getCell(11).getNumericCellValue();
            int availability = convertAvailability(convertedAvailability);

            if(!teamMemberData.containsKey(teamName)) {
                teamMemberData.put(teamName, new ArrayList<>(List.of(new ArrayList<>(List.of(name, role, String.valueOf(availability))))));
            } else {
                teamMemberData.get(teamName).add(new ArrayList<>(List.of(name, role, String.valueOf(availability))));
            }
        }

        return teamMemberData;
    }

    @Override
    public void readData() {
        HashMap<String, ArrayList<ArrayList<String>>> confluenceData = readConfluence();
        HashMap<String, ArrayList<ArrayList<String>>> irmData = readIRM();
        for (Map.Entry<String, ArrayList<ArrayList<String>>> irmMemberData : irmData.entrySet()) {
            if(!confluenceData.containsKey(irmMemberData.getKey())) {
                confluenceData.put(irmMemberData.getKey(), irmMemberData.getValue());
            } else {
                ArrayList<String> members = new ArrayList<>();
                for(ArrayList<String> member: confluenceData.get(irmMemberData.getKey())) members.add(member.get(0));
                for(ArrayList<String> member: irmMemberData.getValue()) {
                    if(!members.contains(member.get(0))) {
                        confluenceData.get(irmMemberData.getKey()).add(member);
                    }
                }
            }
        }
        loadToDatabase(confluenceData);
    }

    private void loadToDatabase(HashMap<String, ArrayList<ArrayList<String>>> memberData) {
        Map<String, Team> teamsMap = new HashMap<>();
        Map<String, ArrayList<MemberMetaDataDTO>> memberTeamMap = new HashMap<>();

        for (Map.Entry<String, ArrayList<ArrayList<String>>> mData : memberData.entrySet()) {
            if(!teamsMap.containsKey(mData.getKey())) {
                teamsMap.put(mData.getKey(), new Team(mData.getKey(), "", new ArrayList<>()));
            }

            for(ArrayList<String> m: mData.getValue()) {
                if(!memberTeamMap.containsKey(m.get(0))) memberTeamMap.put(m.get(0), new ArrayList<>(List.of(new MemberMetaDataDTO(mData.getKey(), m.get(1), Integer.parseInt(m.get(2))))));
                else memberTeamMap.get(m.get(0)).add(new MemberMetaDataDTO(mData.getKey(), m.get(1), Integer.parseInt(m.get(2))));
            }

        }

        for(String name: memberTeamMap.keySet()) {
            ArrayList<MemberMetaDataDTO> memberMetaDataDTOS = memberTeamMap.get(name);
            String firstname = name.split(" ")[0];
            String lastname = String.join(" ", Arrays.copyOfRange(name.split(" "), 1, name.split(" ").length));
            Optional<Person> personOptional = personRepository.findByFirstnameAndLastname(firstname, lastname);
            Person person;
            if(personOptional.isEmpty()) {
                person = new Person((firstname.toLowerCase() + lastname.toLowerCase()).replaceAll(" ", ""), firstname, lastname, new ArrayList<>());
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

}
