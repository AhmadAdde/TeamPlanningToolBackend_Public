package com.example.TeamPlaningToolBackend.security.services;

import com.example.TeamPlaningToolBackend.DB.UserDB;
import com.example.TeamPlaningToolBackend.security.sec_utils.AuthCredentialRequest;
import com.example.TeamPlaningToolBackend.enums.Role;
import com.example.TeamPlaningToolBackend.DB.UserRepository;
import com.example.TeamPlaningToolBackend.security.sec_utils.CustomPasswordEncoder;
import com.example.TeamPlaningToolBackend.security.sec_utils.RegisterRequest;
import com.example.TeamPlaningToolBackend.security.sec_utils.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final Logger LOGGER = Logger.getLogger(AuthServiceImpl.class.getName());
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final AuthenticationManager authenticationManager;
    @Autowired
    private final JwtService jwtService;
    @Autowired
    private final CustomPasswordEncoder customPasswordEncoder;

    @Override
    public ResponseEntity<?> authenticate(AuthCredentialRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()
                    )
            );
            UserDB userDB = userRepository.findById(request.getUsername()).orElseThrow();

            String jwt = jwtService.generateToken(userDB);

            //TODO: DECIDE IF THERE IS ANY NEED TO SEND MORE DATA BESIDES JWT
            TokenResponse token = TokenResponse.builder()
                    .username(request.getUsername())
                    .accessToken(jwt)
                    .roles(List.of(Role.USER))
                    .build();

            return ResponseEntity.ok(token);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Override
    public ResponseEntity<?> sigUp(RegisterRequest request) {
        try {
            // Check if the user already exists in the db
            if (userRepository.findById(request.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            UserDB userDB = UserDB.builder()
                    .firstname(request.getFullName())
                    .lastname(request.getFullName())
                    .username(request.getUsername())
                    .password(customPasswordEncoder.getPasswordEncoder().encode(request.getPassword()))
                    .role(Role.USER)
                    .build();
            userRepository.save(userDB);

            return ResponseEntity.ok(HttpStatus.CREATED);

        } catch (Exception ex) {
            LOGGER.warning(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

}
