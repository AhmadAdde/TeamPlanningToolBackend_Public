package com.example.TeamPlaningToolBackend.controller;

import com.example.TeamPlaningToolBackend.services.TeamService;
import com.example.TeamPlaningToolBackend.utils.AddMemberRequest;
import com.example.TeamPlaningToolBackend.utils.TeamDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

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
    public void createTeam(@RequestBody TeamDTO team) {
        teamService.createTeam(team);
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
    public void delete(@RequestBody TeamDTO team) {
        teamService.deleteTeam(team.getTeamName());
    }

}