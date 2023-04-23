package com.example.TeamPlaningToolBackend.security.sec_utils;

import lombok.Data;

/**
 * This class represents the request object sent from the client application.
 * So the client, when REGISTERING for the first time, needs to send a correspondent
 * object that contains all parameters below.
 */
@Data
public class RegisterRequest {
    private String username;
    private String fullName;
    private String password;
    private int age;
}
