package com.example.TeamPlaningToolBackend.security.config;

import com.example.TeamPlaningToolBackend.services.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FileLoadConfig {

    @Autowired
    private final PersonService personService;

    @Bean
    public void readFile() {
        personService.readFileToDatabase("fake-ad-output.txt");
    }

}
