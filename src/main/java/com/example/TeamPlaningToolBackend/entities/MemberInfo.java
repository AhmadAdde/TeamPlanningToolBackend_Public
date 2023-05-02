package com.example.TeamPlaningToolBackend.entities;

import com.example.TeamPlaningToolBackend.enums.Role;


public class MemberInfo {

    private String teamName;
    private Role role;
    private int availability;

    public MemberInfo(String teamName, Role role, int availability) {
        this.teamName = teamName;
        this.role = role;
        this.availability = availability;
    }

    public String getTeamName() {
        return teamName;
    }

    public Role getRole() {
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
