package com.example.TeamPlaningToolBackend.services;

import com.example.TeamPlaningToolBackend.utils.AddMemberRequest;
import com.example.TeamPlaningToolBackend.utils.TeamDTO;

import java.util.ArrayList;

public interface TeamService {
    void createTeam(TeamDTO team);
    ArrayList<TeamDTO> getAllTeams();
    void addMember(AddMemberRequest member);
    void deleteMember(AddMemberRequest member);
    void deleteTeam(String teamName);
}
