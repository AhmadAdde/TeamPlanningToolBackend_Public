package com.example.TeamPlaningToolBackend.utils;

import com.example.TeamPlaningToolBackend.enums.Role;

import java.util.ArrayList;
import java.util.List;


public class MemberMetaDataDTO {

    private String teamName;
    private ArrayList<Role> role;
    private int availability;

    public MemberMetaDataDTO(String teamName, Role role, int availability) {
        this.teamName = teamName;
        this.role = new ArrayList<>(List.of(role));
        this.availability = availability;
    }

    public MemberMetaDataDTO(String teamName, ArrayList<Role> role, int availability) {
        this.teamName = teamName;
        this.role = role;
        this.availability = availability;
    }

    public String getTeamName() {
        return teamName;
    }

    public ArrayList<Role> getRole() {
        return role;
    }

    public int getAvailability() {
        return availability;
    }

    @Override
    public String toString() {
        return "MemberInfo{" +
                "teamName='" + teamName + '\'' +
                ", role=" + role +
                ", availability=" + availability +
                '}';
    }
}
