package com.example.TeamPlaningToolBackend.DB;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = "person_table")
public class PersonDB {
    private static final long serialVersionUID = 1L;

    @Id
    private String username;
    private String firstname;
    private String lastname;
    @ManyToMany
    private Set<TeamsDB> team = new HashSet<>();



}
