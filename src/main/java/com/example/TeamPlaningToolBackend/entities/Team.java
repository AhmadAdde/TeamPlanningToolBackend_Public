package com.example.TeamPlaningToolBackend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "team_t")
public class Team {

    @Id
    @Column(name = "team_name")
    private String teamName;
    @Column(name = "scrum_master_id")
    private String scrumMaster;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "team_person",
            joinColumns = {@JoinColumn(name = "team_name")},
            inverseJoinColumns = {
                    @JoinColumn(name = "username", referencedColumnName = "username", table="person_t")
            })
    private List<Person> members = new ArrayList<>();
}
