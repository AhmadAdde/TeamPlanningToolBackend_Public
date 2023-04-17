package com.example.TeamPlaningToolBackend.Bo;

import com.example.TeamPlaningToolBackend.DB.TeamsDB;
import lombok.Data;

import java.util.List;
@Data
public class Person {

    private String username;
    private String firstname;
    private String lastname;
    private List<TeamsDB> team;




}
