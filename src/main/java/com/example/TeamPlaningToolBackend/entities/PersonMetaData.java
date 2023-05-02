package com.example.TeamPlaningToolBackend.entities;


import com.example.TeamPlaningToolBackend.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "personMetaData_t")
@IdClass(PersonMetaData.class)
public class PersonMetaData implements Serializable {

    @Id
    private String username;
    @Id
    private String teamName;
    private int availability;

}
