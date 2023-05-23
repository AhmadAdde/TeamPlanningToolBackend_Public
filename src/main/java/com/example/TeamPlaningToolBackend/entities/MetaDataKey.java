package com.example.TeamPlaningToolBackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MetaDataKey implements Serializable {
    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "teamName", nullable = false)
    private String teamName;
}
