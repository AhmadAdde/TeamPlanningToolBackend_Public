package com.example.TeamPlaningToolBackend.controller;

import com.example.TeamPlaningToolBackend.services.TeamService;
import com.example.TeamPlaningToolBackend.utils.AddMemberRequest;
import com.example.TeamPlaningToolBackend.utils.TeamDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/team")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TeamController {

    @Autowired
    private final TeamService teamService;

    @GetMapping("/get-all")
    public ArrayList<TeamDTO> getAllTeams() {
        return teamService.getAllTeams();
    }

    @PostMapping("/create")
    public void createTeam(@RequestBody List<TeamDTO> teams) {
        System.out.println("TEAMS" + teams);
        teamService.createTeam(teams);
    }
    @GetMapping("/get-roles")
    public ArrayList<String> getRoles() {
        return teamService.getRoles();
    }
    @PostMapping("/add-member")
    public void addMember(@RequestBody AddMemberRequest newMember) {
        teamService.addMember(newMember);
    }

    @PostMapping("/delete-member")
    public void deleteMember(@RequestBody AddMemberRequest member) {
        teamService.deleteMember(member);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody List<String> teamNames) {
        teamService.deleteTeam(teamNames);
    }

    @PostMapping("/deleteSavedData")
    public void deleteSavedData(@RequestBody List<TeamDTO> teamNames) {
        teamService.deleteSavedDatas(teamNames);
    }

    @GetMapping("/load-data")
    public Map<String, Map<String, Map<String, ArrayList<String>>>> loadConfluence() {
        try {
           return teamService.readData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/update-data")
    public void updateConfluence(@RequestBody List<String> teamNames) {
        try {
            teamService.updateData(teamNames);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

