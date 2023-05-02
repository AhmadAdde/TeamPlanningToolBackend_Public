package com.example.TeamPlaningToolBackend.utils;

import com.example.TeamPlaningToolBackend.enums.Role;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonDTO {

    private String username;
    private String firstname;
    private String lastname;
    private Role role;
    private int availability;
    private List<String> teams;

}
