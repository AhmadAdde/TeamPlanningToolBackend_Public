package com.example.TeamPlaningToolBackend.security.sec_utils;

import lombok.Getter;
import lombok.Setter;

/**
 * This class represents the request object sent from the client application.
 * So the client, whenever SIGNING IN, needs to send a correspondent
 * object that contains all parameters below.
 */

@Getter
@Setter
public class AuthCredentialRequest {
    private String username;
    private String password;
}
