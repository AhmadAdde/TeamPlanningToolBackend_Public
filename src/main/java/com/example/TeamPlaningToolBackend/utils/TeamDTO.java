package com.example.TeamPlaningToolBackend.utils;

import com.example.TeamPlaningToolBackend.entities.PersonMetaData;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDTO {

    private String teamName;
    private List<String> userIds;
    private String scrumMaster;
    private Map<String, PersonMetaData> metaData;
}
