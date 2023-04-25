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

    private final String localFilePath = "./src/main/java/com/example/TeamPlaningToolBackend/assets/fake-ad-output.txt";
    private final String dockerFilePath = "/assets/src/main/java/com/example/TeamPlaningToolBackend/assets/fake-ad-output.txt";

    @Bean
    public void readFile() {
        personService.readFileToDatabase(localFilePath);
    }
}
