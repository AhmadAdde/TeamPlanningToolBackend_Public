package com.example.TeamPlaningToolBackend.DB;


import jakarta.persistence.*;

@Entity
@Table(name = "team_table")
public class TeamsDB {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "teamName")
    private String teamName;
    //private Set<PersonDB> persons = new HashSet<>();

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    @Override
    public String toString() {
        return "TeamsDB{" +
                "teamName='" + teamName + '\'' +
                '}';
    }
}
