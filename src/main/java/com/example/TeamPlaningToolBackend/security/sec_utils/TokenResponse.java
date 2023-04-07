package com.example.TeamPlaningToolBackend.security.sec_utils;

import com.example.TeamPlaningToolBackend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * This class represents the response object sent to the authenticated client.
 * So the client, once she signed in, receives a correspondent
 * object that contains all parameters below.
 */

@Deprecated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String jwt;
    private final String type = "Bearer";
    private String username;
    private List<Role> roles;
}
