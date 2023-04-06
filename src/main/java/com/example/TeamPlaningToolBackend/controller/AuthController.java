package com.example.TeamPlaningToolBackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody AuthCredentialRequest req) {
        try {

            String jwt = "Thejsonwebtokenmotherfucker";

            return ResponseEntity.ok(jwt);
            // .header(
            //        HttpHeaders.AUTHORIZATION,
            //        jwtUtil.generateToken(userDb)
            //)
            //.body(userDb);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public String AddUser(@RequestBody String user) {

        return "Registered a new user mf";
    }

}
