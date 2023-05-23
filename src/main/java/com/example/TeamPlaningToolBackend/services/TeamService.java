package com.example.TeamPlaningToolBackend.services;

import com.example.TeamPlaningToolBackend.utils.AddMemberRequest;
import com.example.TeamPlaningToolBackend.utils.TeamDTO;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface TeamService {
    void createTeam(List<TeamDTO> teams);
    ArrayList<TeamDTO> getAllTeams();
    void addMember(AddMemberRequest member);
    void deleteMember(AddMemberRequest member);
    void deleteTeam(List<String> teamNames);
    void deleteSavedDatas(List<TeamDTO> teamNames);
    Map<String, Map<String, Map<String, ArrayList<String>>>> readData() throws IOException;
    void updateData(List<String> teamNames) throws IOException, InterruptedException;
    ArrayList<String> getRoles();
}
