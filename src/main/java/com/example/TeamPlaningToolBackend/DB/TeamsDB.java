package com.example.TeamPlaningToolBackend.DB;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = "team_table")
public class TeamsDB {
    private static final long serialVersionUID = 1L;

    @Id
    private String teamName;
    @ManyToMany
    private Set<PersonDB> persons = new HashSet<>();

}
