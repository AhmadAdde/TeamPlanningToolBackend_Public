package com.example.TeamPlaningToolBackend.entities;

import com.example.TeamPlaningToolBackend.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "person_t")
public class Person {

    @Id
    private String username;
    private String firstname;
    private String lastname;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "members")
    @ToString.Exclude
    private List<Team> teams = new ArrayList<>();
}
