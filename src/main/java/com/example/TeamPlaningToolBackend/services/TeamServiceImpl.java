package com.example.TeamPlaningToolBackend.services;

import com.example.TeamPlaningToolBackend.utils.*;
import com.example.TeamPlaningToolBackend.entities.Person;
import com.example.TeamPlaningToolBackend.entities.PersonMetaData;
import com.example.TeamPlaningToolBackend.entities.Team;
import com.example.TeamPlaningToolBackend.repository.PersonMetaDataRepository;
import com.example.TeamPlaningToolBackend.repository.PersonRepository;
import com.example.TeamPlaningToolBackend.repository.TeamRepository;
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
import org.springframework.util.StringUtils;

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
    private final String dockerFilePath = "/assets/src/main/java/com/example/TeamPlaningToolBackend/assets/IRM-Sample.xlsm";
    private static final String irmSheetPassword = "sample";

    private static final String confluenceUrl = "https://teamplaningtool.atlassian.net/wiki/rest/api/content/";
    private static final String apiKey = "ATATT3xFfGF0WAIg2Waax0zpDbLk9wGSUCqkRW22LQtjksEsauUIEpVv_luk9rwLfMJ04KOPZDXshv2tp62jMiSZF790x9A_YhtR_6IlsLu7FKO1gqhF6jx4m_8qCtNbqpUUiXwvS1ij9u9-P6KCP27pMsHLkynijD-J2Xn_HDltE1Wmq8q-DiI=AFC54599";
    private static final String email = "vinhed@gmail.com";
    private static final String pageKey = "MFS";
    private static final float jaroWinklerThreshold = 0.9f;

    @Autowired
    private final TeamRepository teamRepository;

    private static final String parentId = "65563";
    private static HashMap<String, String> teamIds;
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

            team.getMetaData().forEach(obj ->{
                PersonMetaData metaData = PersonMetaData.builder()
                        .username(obj.getUsername())
                        .teamName(obj.getTeamName().replaceAll(" ", "_"))
                        .availability(obj.getAvailability())
                        .role(obj.getRole())
                        .build();
                personMetaDataRepository.deleteByTeamNameAndUsername(team.getTeamName(), obj.getUsername());
                personMetaDataRepository.save(metaData);
            });
            Team newTeam = Team.builder()
                    .members(members)
                    .teamName(team.getTeamName().replaceAll(" ", "_"))
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
            System.out.println("GETSCRUMS: " + team.getTeamName() + " " +team.getScrumMaster());
            TeamDTO newTeam = TeamDTO.builder()
                    .userIds(members)
                    .teamName(team.getTeamName())
                    .metaData(personMetaMap)
                    .scrumMaster(team.getScrumMaster())
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

        teamRepository.save(team);
    }
    @Override
    public ArrayList<String> getRoles() {
        XSSFWorkbook wb = null;
        try {
            FileInputStream fis = new FileInputStream(irmSheetPath);
            wb = (XSSFWorkbook) WorkbookFactory.create(fis, irmSheetPassword);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        XSSFSheet sheet = wb.getSheetAt(0);

        ArrayList<String> roles = new ArrayList<>();

        for (int rowIndex = 9; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            String role = row.getCell(4).getStringCellValue();
            if (role.isEmpty()) break;
            roles.add(role);
        }
        System.out.println("ROELS" + roles);
        return roles;
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

    public void updateIRMSheet() throws IOException {
        FileInputStream fis = new FileInputStream(irmSheetPath);
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
                Optional<PersonMetaData> metaData = personMetaDataRepository.findByTeamNameAndUsername(team.getTeamName(), person.getUsername());
                if(metaData.isPresent()) {
                    StringBuilder role = new StringBuilder();
                    for(String r: metaData.get().getRole()) role.append(" / ").append(r);
                    role.replace(0, 3, "");
                    row.getCell(10).setCellValue(role.toString());
                    row.getCell(11).setCellValue(metaData.get().getAvailability() * 0.4);
                }
                rowIndex++;
            }
        }
        fis.close();
        FileOutputStream fos = new FileOutputStream(irmSheetPath);
        wb.write(fos);
        wb.close();
        fos.close();
    }

    @Override
    public Map<String, Map<String, Map<String, ArrayList<String>>>> readData() {
        HashMap<String, ArrayList<ArrayList<String>>> confluenceData = readConfluence();
        HashMap<String, ArrayList<ArrayList<String>>> irmData = readIRM();
        Map<String, Map<String, Map<String, ArrayList<String>>>> comparedData =  compareData(confluenceData, irmData);
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

        System.out.println(confluenceData);

        loadToDatabase(confluenceData);
        return comparedData;
    }

    @Override
    public void updateData(List<String> teamNames) throws IOException, InterruptedException {
        teamNames.replaceAll(s -> s.replaceAll(" ", "_"));
        updateConfluence(teamNames);
        updateIRMSheet();
    }

    private void createConfluencePage(String teamName) throws IOException, InterruptedException {
        Optional<Team> teamOptional = teamRepository.findById(teamName);
        if (teamOptional.isEmpty()) return;

        JSONObject space = new JSONObject();
        space.put("key", pageKey);

        JSONObject ancestor = new JSONObject();
        ancestor.put("id", Integer.valueOf(parentId));

        JSONArray ancestorArray = new JSONArray();
        ancestorArray.put(ancestor);

        JSONObject page = new JSONObject();
        page.put("title", teamName);
        page.put("type", "page");
        page.put("space", space);
        page.put("ancestors", ancestorArray);

        JSONObject body = new JSONObject();
        JSONObject storage = new JSONObject();

        String htmlTable = "<table data-layout=\"default\"><tbody><tr><th><p><strong>Name</strong></p></th><th><p><strong>Role</strong></p></th><th><p><strong>Availability</strong></p></th></tr>";
        for(Person person: teamOptional.get().getMembers()) {
            Optional<PersonMetaData> personMetaData = personMetaDataRepository.findByTeamNameAndUsername(teamName, person.getUsername());
            StringBuilder role = new StringBuilder();
            String availability = "";
            if (personMetaData.isPresent()) {
                for (String _role : personMetaData.get().getRole())
                    role.append(" / ").append(_role);
                availability = personMetaData.get().getAvailability() + "%";
                role.replace(0, 3, "");
            }
            String htmlTableRow = """
                            <tr>
                            <td>
                            <p>
                            <ac:link>
                            <ri:user ri:account-id=\"""" + person.getUsername() + """
                            "/>
                            </ac:link>
                            </p>
                            </td>
                            <td>
                            <p>""" + role + """
                            </p>
                            </td>
                            <td>
                            <p>""" + availability + """
                            </p>
                            </td>
                            </tr>
                            """;;
            htmlTable += htmlTableRow.replaceAll("\n", "");
        }
        htmlTable += "</tbody></table>";

        storage.put("value", htmlTable);
        storage.put("representation", "storage");
        body.put("storage", storage);
        page.put("body", body);


        HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(confluenceUrl))
                .header("Authorization", getBasicAuthHeader())
                .header("Content-Type", "application/json")
                .header("User-Agent", "TeamPlanner")
                .POST(HttpRequest.BodyPublishers.ofString(page.toString()))
                .build();

        HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void updateConfluence(List<String> teamNames) throws IOException, InterruptedException {
        getChildPageIds();

        for(String teamName: teamNames) {
            if(!teamIds.containsKey(teamName)) {
                createConfluencePage(teamName);
                continue;
            }

            String teamId = teamIds.get(teamName);

            HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(confluenceUrl + teamId + "?expand=body.storage,body.view,version"))
                    .header("Authorization", getBasicAuthHeader())
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject pageObj = new JSONObject(resp.body());

            String html = pageObj.getJSONObject("body").getJSONObject("storage").getString("value");
            String htmlView = pageObj.getJSONObject("body").getJSONObject("view").getString("value");
            Document doc = Jsoup.parse(html);
            Elements tables = doc.select("tbody");

            for (int j = 0; j < tables.size(); j++) {
                Element table = tables.get(j);
                String headers = table.child(0).toString();
                if (!(headers.contains("Name") && headers.contains("Role") && headers.contains("Availability"))) continue;

                int amountOfColumns = StringUtils.countOccurrencesOf(headers, "th") / 2;
                ArrayList<ArrayList<String>> membersInConfluence = getTableFromPageId(teamId);
                Optional<Team> teamOptional = teamRepository.findById(teamName);
                if (teamOptional.isEmpty()) break;

                List<Person> membersInDatabase = teamOptional.get().getMembers();
                ArrayList<String> memberNamesInConfluence = new ArrayList<>();
                for (ArrayList<String> s : membersInConfluence) memberNamesInConfluence.add(s.get(0));

                for(int i = table.children().size() - 1; i >= 1; i--) {
                    Element usernameElement = Jsoup.parse(htmlView).select("tbody").get(j).child(i).child(0);
                    Element row = table.child(i);
                    if(usernameElement.text().isEmpty() || row.child(1).text().isEmpty() || row.child(2).text().isEmpty()) continue;

                    String username = "";
                    Elements riTags = table.child(i).getElementsByTag("ri:user");
                    if(riTags.size() > 0) username = riTags.get(0).attr("ri:account-id");
                    boolean userDoesNotExist = true;
                    for(Person person: membersInDatabase) {
                        if(person.getUsername().equals(username)) {
                            userDoesNotExist = false;
                            break;
                        }
                    }
                    if(userDoesNotExist) {
                        table.child(i).remove();
                    }
                }

                for (Person person : membersInDatabase) {
                    Optional<PersonMetaData> personMetaData = personMetaDataRepository.findByTeamNameAndUsername(teamName, person.getUsername());
                    String personFullName = person.getFirstname() + " " + person.getLastname();

                    System.out.println(personFullName);
                    StringBuilder role = new StringBuilder();
                    String availability = "";
                    if (personMetaData.isPresent()) {
                        for (String _role : personMetaData.get().getRole())
                            role.append(" / ").append(_role);
                        availability = personMetaData.get().getAvailability() + "%";
                        role.replace(0, 3, "");
                    }

                    if (memberNamesInConfluence.contains(personFullName)) {
                        for(Element tableRow: table.children()) {
                            if(personMetaData.isPresent() && tableRow.toString().contains(personMetaData.get().getUsername())) {
                                Elements cells = tableRow.getElementsByTag("p");
                                cells.get(1).text(role.toString());
                                cells.get(2).text(availability);
                                break;
                            }
                        }
                        continue;
                    }

                    String htmlTableRow = """
                            <tr>
                            <td>
                            <p>
                            <ac:link>
                            <ri:user ri:account-id=\"""" + person.getUsername() + """
                            "/>
                            </ac:link>
                            </p>
                            </td>
                            <td>
                            <p>""" + role + """
                            </p>
                            </td>
                            <td>
                            <p>""" + availability + """
                            </p>
                            </td>
                            """;
                    for (int k = 3; k < amountOfColumns; k++) htmlTableRow += "<td><p></p></td>";
                    htmlTableRow += "</tr>";
                    htmlTableRow = htmlTableRow.replaceAll("\n", "");
                    table.append(htmlTableRow);
                }
            }

            pageObj.getJSONObject("body").getJSONObject("storage").put("value", doc.outerHtml());

            JSONObject updated = new JSONObject();
            JSONObject versionNumber = new JSONObject();
            versionNumber.put("number", pageObj.getJSONObject("version").getInt("number") + 1);
            updated.put("version", versionNumber);
            updated.put("type", "page");
            updated.put("title", pageObj.getString("title"));

            JSONObject value = new JSONObject();
            JSONObject storage = new JSONObject();
            value.put("value", pageObj.getJSONObject("body").getJSONObject("storage").getString("value"));
            value.put("representation", pageObj.getJSONObject("body").getJSONObject("storage").getString("representation"));
            storage.put("storage", value);
            updated.put("body", storage);

            httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
            request = HttpRequest.newBuilder()
                    .uri(URI.create(confluenceUrl + teamId))
                    .header("Authorization", getBasicAuthHeader())
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(updated.toString()))
                    .build();

            resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(resp.body());
        }
    }

    private int convertAvailability(float availability) {
        float ava = (availability/40) * 100;
        return (int) ava;
    }

    private String getBasicAuthHeader() {
        String auth = email + ":" + apiKey;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }

    private HashMap<String, String> getChildPageIds() {
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
                .uri(URI.create(confluenceUrl + pageId + "?expand=body.storage,body.view"))
                .header("Authorization", getBasicAuthHeader())
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> resp;
        try {
            resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(resp == null) return tableContent;

        JSONObject json = new JSONObject(resp.body());
        Document docView = Jsoup.parse(json.getJSONObject("body").getJSONObject("view").getString("value"));
        Document docStorage = Jsoup.parse(json.getJSONObject("body").getJSONObject("storage").getString("value"));

        Elements tablesView = docView.select("tbody");
        Elements tablesStorage = docStorage.select("tbody");
        for(int k = 0; k < tablesView.size(); k++) {
            Element table = tablesView.get(k);
            String headers = table.child(0).toString();
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

                String username = "";
                Elements riTags = tablesStorage.get(k).child(j).getElementsByTag("ri:user");
                if(riTags.size() > 0) username = riTags.get(0).attr("ri:account-id");
                cells.add(username);
                cells.set(2, cells.get(2).replaceAll("%", ""));

                List<String> rolesSorted = Arrays.asList(cells.get(1).split(" / "));
                Collections.sort(rolesSorted);
                cells.set(1, String.join(" / ", rolesSorted));

                tableContent.add(cells);
            }
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
            String teamName = row.getCell(9).getStringCellValue().split(" - ")[0].replaceAll(" ", "_");
            String role = row.getCell(10).getStringCellValue();
            float convertedAvailability = (float) row.getCell(11).getNumericCellValue();
            int availability = convertAvailability(convertedAvailability);
            String username = "";

            List<String> rolesSorted = Arrays.asList(role.split(" / "));
            Collections.sort(rolesSorted);
            role = String.join(" / ", rolesSorted);

            if(!teamMemberData.containsKey(teamName)) {
                teamMemberData.put(teamName, new ArrayList<>(List.of(new ArrayList<>(List.of(name, role, String.valueOf(availability), username)))));
            } else {
                teamMemberData.get(teamName).add(new ArrayList<>(List.of(name, role, String.valueOf(availability), username)));
            }
        }

        return teamMemberData;
    }

    private void loadToDatabase(HashMap<String, ArrayList<ArrayList<String>>> memberData) {
        Map<String, Team> teamsMap = new HashMap<>();
        Map<String, ArrayList<MemberMetaDataDTO>> memberTeamMap = new HashMap<>();
        Map<String, String> nameToUsername = new HashMap<>();
        HashMap<String, ArrayList<String>> scrumMap = new HashMap<>();

        for (Map.Entry<String, ArrayList<ArrayList<String>>> mData : memberData.entrySet()) {
            String teamName = mData.getKey();

            if(!scrumMap.containsKey(teamName)) {
                scrumMap.put(teamName, new ArrayList<>());
            }

            for(ArrayList<String> m: mData.getValue()) {
                String username = (m.get(0).toLowerCase()).replaceAll(" ", "");
                if(!m.get(3).isEmpty()) username = m.get(3).replaceAll(" ", "");
                nameToUsername.put(m.get(0), username);
                ArrayList<String> roles = new ArrayList<>();
                for(String role : m.get(1).split(" / ")) {
                    roles.add(role);
                    if(role.equals("Scrum Master")) {
                        scrumMap.get(teamName).add(username);
                    }
                }

                if(!memberTeamMap.containsKey(m.get(0))) memberTeamMap.put(m.get(0), new ArrayList<>(List.of(new MemberMetaDataDTO(teamName, roles, Integer.parseInt(m.get(2))))));
                else memberTeamMap.get(m.get(0)).add(new MemberMetaDataDTO(teamName, roles, Integer.parseInt(m.get(2))));
            }

            if(!teamsMap.containsKey(teamName)) {
                teamsMap.put(teamName, new Team(teamName, scrumMap.get(teamName), new ArrayList<>()));
            }
        }

        System.out.println(scrumMap);

        for(String name: memberTeamMap.keySet()) {
            ArrayList<MemberMetaDataDTO> memberMetaDataDTOS = memberTeamMap.get(name);
            String firstname = name.split(" ")[0];
            String lastname = String.join(" ", Arrays.copyOfRange(name.split(" "), 1, name.split(" ").length));
            Optional<Person> personOptional = personRepository.findByFirstnameAndLastname(firstname, lastname);
            Person person;
            if(personOptional.isEmpty()) {
                System.out.println(nameToUsername.get(name));
                person = new Person(nameToUsername.get(name), firstname, lastname, new ArrayList<>());
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

    private Map<String, Map<String, Map<String, ArrayList<String>>>> compareData(HashMap<String, ArrayList<ArrayList<String>>> confluenceData, HashMap<String, ArrayList<ArrayList<String>>> irmData) {
        System.out.println("Confluence Data: " + confluenceData);
        System.out.println("IRM DATA: " + irmData + "STOP");
        Map<String, Map<String, Map<String, ArrayList<String>>>> irmAndConfMap = new HashMap<>();
        irmAndConfMap.put("confluence", new HashMap<>());
        irmAndConfMap.put("irm", new HashMap<>());
        irmAndConfMap.get("confluence").put("name", new HashMap<>());
        irmAndConfMap.get("irm").put("name", new HashMap<>());
        irmAndConfMap.get("confluence").put("roles", new HashMap<>());
        irmAndConfMap.get("irm").put("roles", new HashMap<>());
        irmAndConfMap.get("confluence").put("availability", new HashMap<>());
        irmAndConfMap.get("irm").put("availability", new HashMap<>());
        for (Map.Entry<String, ArrayList<ArrayList<String>>> set : confluenceData.entrySet()) {
            String team = set.getKey();

            ArrayList<ArrayList<String>> sortedConf = set.getValue();
            ArrayList<ArrayList<String>> sortedIrm = irmData.get(team);
            if(sortedIrm == null) sortedIrm = new ArrayList<>();
            if(sortedConf == null) sortedConf = new ArrayList<>();

            for (int i = 0, j = 0; i < sortedConf.size(); i++) {
                String nameConf = sortedConf.get(i).get(0);
                boolean foundPersonInIrm = false;
                for(; j < sortedIrm.size(); j++) {
                    if(JaroWinkler.compare(sortedIrm.get(j).get(0), nameConf) > jaroWinklerThreshold) {
                        foundPersonInIrm = true;
                        break;
                    }
                }
                if(foundPersonInIrm) {
                    String nameIrm = sortedIrm.get(j).get(0);
                    if (!nameConf.equals(nameIrm)) {
                        irmAndConfMap.get("confluence").get("name").put(nameConf, new ArrayList<>());
                        irmAndConfMap.get("confluence").get("name").get(nameConf).add(team);
                        irmAndConfMap.get("irm").get("name").put(nameIrm, new ArrayList<>());
                        irmAndConfMap.get("irm").get("name").get(nameIrm).add(team);
                    }
                    String rolesConf = sortedConf.get(i).get(1);
                    String rolesIrm = sortedIrm.get(j).get(1);
                    if (!rolesConf.equals(rolesIrm)) {
                        irmAndConfMap.get("confluence").get("roles").put(nameConf, new ArrayList<>());
                        irmAndConfMap.get("confluence").get("roles").get(nameConf).add(team);
                        irmAndConfMap.get("confluence").get("roles").get(nameConf).add(rolesConf);
                        irmAndConfMap.get("irm").get("roles").put(nameConf, new ArrayList<>());
                        irmAndConfMap.get("irm").get("roles").get(nameConf).add(team);
                        irmAndConfMap.get("irm").get("roles").get(nameConf).add(rolesIrm);
                    }
                    String availabilityConf = sortedConf.get(i).get(2);
                    String availabilityIrm = sortedIrm.get(j).get(2);
                    if (!availabilityConf.equals(availabilityIrm)) {
                        irmAndConfMap.get("confluence").get("availability").put(nameConf, new ArrayList<>());
                        irmAndConfMap.get("confluence").get("availability").get(nameConf).add(team);
                        irmAndConfMap.get("confluence").get("availability").get(nameConf).add(availabilityConf);
                        irmAndConfMap.get("irm").get("availability").put(nameConf, new ArrayList<>());
                        irmAndConfMap.get("irm").get("availability").get(nameConf).add(team);
                        irmAndConfMap.get("irm").get("availability").get(nameConf).add(availabilityIrm);
                    }
                }

            }
            System.out.println("ENDHASHAP" + irmAndConfMap);


        }
        return irmAndConfMap;
    }

}
/*
Confluence
{AirOn360_Team_1=[[Thomas Steiner, Systems Architect / Test Engineer (SW) / Scrum Master, 30, 557058:347d3b19-192c-4372-b723-c0d827a289fd],
    [Benim Johansson, Systems Architect / Development Engineer (Client/Server SW), 100, 712020:bdbe2f5a-7187-4ac5-8234-95ee2079a83e],
    [Anders Askevold Johnsen, Development Engineer (Client/Server SW), 50, 5bd6ec141968013b778a57fb],
    [Andreas Jose Laursen Gonzalez, Systems Architect / Development Engineer (Client/Server SW), 60, 60b5277d9941850074212d65]],
AirOn360_Team_2=[[Thomas Steiner, Systems Architect, 100, 557058:347d3b19-192c-4372-b723-c0d827a289fd]],
AirOn360_Team_5=[[Patrik Lind Amigo, Scrum Master, 100, 712020:d66bceef-b2bc-4c0f-a0aa-31ea5d3c4da4],
    [Vincent Hedblom, Requirements Engineer, 50, 712020:b19488b1-1a6a-4a01-9911-042834bbb004]],
AirOn360_Team_6=[[Patrik Nilsson, DevOps Engineer, 40, 70121:e93b0d8c-2ccf-4a8f-8c6b-7667d32bb80d],
    [Anders Askevold Johnsen, Scrum Master, 100, 5bd6ec141968013b778a57fb],
    [Peter Peter, Quality Manager / Scrum Master, 30, 557058:07eed10c-934c-4bd2-884e-3eeca74182f1],
    [Andreas Jose Laursen Gonzalez, Systems Architect, 50, 60b5277d9941850074212d65]],
AirOn360_Team_3=[[Patrik Lind Amigo, Scrum Master, 30, 712020:d66bceef-b2bc-4c0f-a0aa-31ea5d3c4da4],
    [Benim Johansson, Development Engineer (HW) / Scrum Master, 50, 712020:bdbe2f5a-7187-4ac5-8234-95ee2079a83e],
    [Thomas Steiner, IP Rigths Coordinator, 40, 557058:347d3b19-192c-4372-b723-c0d827a289fd]],
AirOn360_Team_4=[[Vincent Hedblom, Line Manager, 50, 712020:b19488b1-1a6a-4a01-9911-042834bbb004],
    [Peter Peter, Development Engineer (HW), 100, 557058:07eed10c-934c-4bd2-884e-3eeca74182f1],
    [Benim Johansson, Project Manager, 40, 712020:bdbe2f5a-7187-4ac5-8234-95ee2079a83e]]}

IRM
{AirOn360_Team_1=[[Andreas Jose Laursen Gonzalez, Systems Architect / Development Engineer (Client/Server SW), 60, ],
    [Thomas Steiner, Systems Architect / Test Engineer (SW) / Scrum Master, 30, ],
    [Anders Askevold Johnsen, Development Engineer (Client/Server SW), 50, ],
    [Benim Johansson, Systems Architect / Development Engineer (Client/Server SW), 100, ]],
AirOn360_Team_2=[[Thomas Steiner, Systems Architect, 100, ]],
AirOn360_Team_5=[[Patrik Lind Amigo, Scrum Master, 100, ],
    [Vincent Hedblom, Requirements Engineer, 50, ]],
AirOn360_Team_6=[[Anders Askevold Johnsen, Scrum Master, 100, ],
    [Patrik Nilsson, DevOps Engineer, 40, ],
    [Andreas Jose Laursen Gonzalez, Systems Architect, 50, ],
    [Peter Peter, Quality Manager / Scrum Master, 30, ]],
AirOn360_Team_3=[[Benim Johansson, Development Engineer (HW) / Scrum Master, 50, ],
    [Thomas Steiner, IP Rigths Coordinator, 40, ],
    [Patrik Lind Amigo, Scrum Master, 30, ]],
AirOn360_Team_4=[[Benim Johansson, Project Manager, 40, ],
    [Vincent Hedblom, Line Manager, 50, ],
    [Peter Peter, Development Engineer (HW), 100, ]]}

    */

