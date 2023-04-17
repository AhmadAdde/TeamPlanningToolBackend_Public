package com.example.TeamPlaningToolBackend;

import com.example.TeamPlaningToolBackend.Bo.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.TeamPlaningToolBackend.Bo")
public class TeamPlaningToolBackendApplication {
	@Autowired
	private static PersonService personService;

	public static void main(String[] args) {
		SpringApplication.run(TeamPlaningToolBackendApplication.class, args);
		personService.readFileToDatabase("fake-ad-output.txt");
	}

}
