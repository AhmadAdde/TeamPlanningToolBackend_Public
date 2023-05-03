package com.example.TeamPlaningToolBackend.utils;

import com.example.TeamPlaningToolBackend.entities.PersonMetaData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDTO {

    private String teamName;
    private List<String> userIds;
    private String scrumMaster;
    private ArrayList<PersonMetaData> metaData;
}
