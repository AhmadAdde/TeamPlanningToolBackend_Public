package com.example.TeamPlaningToolBackend.security.services;

import com.example.TeamPlaningToolBackend.security.sec_utils.AuthCredentialRequest;
import com.example.TeamPlaningToolBackend.security.sec_utils.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    // Is used to eventually log in users as well
    ResponseEntity<?> authenticate(AuthCredentialRequest request);

    ResponseEntity<?> sigUp(RegisterRequest request);

}
