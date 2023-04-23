package com.example.TeamPlaningToolBackend.controller;

import com.example.TeamPlaningToolBackend.services.PersonService;
import com.example.TeamPlaningToolBackend.utils.PersonDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/person")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PersonController {

    @Autowired
    private final PersonService personService;

    @GetMapping("/get-all")
    public List<PersonDTO> getAllPeople() {
        return personService.getAllPeople();
    }

    @GetMapping("/get")
    public PersonDTO getPerson(@RequestParam(name = "username") String username) {
        return personService.getPersonByUsername(username);
    }

}
