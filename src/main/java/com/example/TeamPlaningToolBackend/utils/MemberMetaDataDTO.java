package com.example.TeamPlaningToolBackend.utils;

import com.example.TeamPlaningToolBackend.enums.Role;

import java.util.ArrayList;
import java.util.List;


public class MemberMetaDataDTO {

    private String teamName;
    private ArrayList<String> role;
    private int availability;

    public MemberMetaDataDTO(String teamName, String role, int availability) {
        this.teamName = teamName;
        this.role = new ArrayList<>(List.of(role));
        this.availability = availability;
    }

    public MemberMetaDataDTO(String teamName, ArrayList<String> role, int availability) {
        this.teamName = teamName;
        this.role = role;
        this.availability = availability;
    }

    public String getTeamName() {
        return teamName;
    }

    public ArrayList<String> getRole() {
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
