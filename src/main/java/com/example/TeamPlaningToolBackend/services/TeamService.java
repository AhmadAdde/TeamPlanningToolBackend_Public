package com.example.TeamPlaningToolBackend.services;

import com.example.TeamPlaningToolBackend.utils.AddMemberRequest;
import com.example.TeamPlaningToolBackend.utils.TeamDTO;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface TeamService {
    void createTeam(List<TeamDTO> teams);
    ArrayList<TeamDTO> getAllTeams();
    void addMember(AddMemberRequest member);
    void deleteMember(AddMemberRequest member);
    void deleteTeam(List<String> teamNames);
    void readIRMSheet(String path) throws IOException;
}
