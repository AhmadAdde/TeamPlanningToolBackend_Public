package com.example.TeamPlaningToolBackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/data")
public class TestController {

    @GetMapping("/get")
    public ResponseEntity<?> getData(@RequestParam("username") String username) {
        return ResponseEntity.ok("Welcome to the Rest API " + username);
    }
}