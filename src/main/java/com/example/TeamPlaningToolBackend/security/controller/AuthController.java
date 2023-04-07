package com.example.TeamPlaningToolBackend.security.controller;

import com.example.TeamPlaningToolBackend.security.sec_utils.AuthCredentialRequest;
import com.example.TeamPlaningToolBackend.security.services.AuthService;
import com.example.TeamPlaningToolBackend.security.sec_utils.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private final AuthService authService;

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody AuthCredentialRequest req) {
        return authService.authenticate(req);
    }

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> signUp(@RequestBody RegisterRequest request) {
        return authService.sigUp(request);
    }

}
