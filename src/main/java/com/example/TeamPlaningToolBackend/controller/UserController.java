package com.example.TeamPlaningToolBackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class UserController {

    @GetMapping("/get")
    public String getUser(@RequestParam("username") String username) {
        return "Welcome to the Rest API " + username;
    }
}
