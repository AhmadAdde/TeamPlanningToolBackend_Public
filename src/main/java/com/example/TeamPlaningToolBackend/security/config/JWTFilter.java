package com.example.TeamPlaningToolBackend.security.config;

import com.example.TeamPlaningToolBackend.repository.UserRepository;
import com.example.TeamPlaningToolBackend.security.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final JwtService jwtService;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        //First, get the AUTHORIZATION header from the request headers
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Check if the AUTHORIZATION header exists and if it is valid, and if not throw the request to the next filter
        if (!StringUtils.hasText(authHeader) ||
            (StringUtils.hasText(authHeader) && !authHeader.startsWith("Bearer "))){
            filterChain.doFilter(request,response);
            return;
        }

        // Now take the JWT out from the header and store it into token
        final String token = authHeader.split(" ")[1].trim().replaceAll("^\"|\"$", "");

        // Get user identity and set it on the spring security context
        UserDetails userDetails = userRepository.findById(jwtService.getUsernameFromToken(token)).get();

        // Get jwt token and validate
        if (!jwtService.validateToken(token, userDetails)) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken
                authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null,
                userDetails == null ?
                        List.of() : userDetails.getAuthorities()
        );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        // The user is now authenticated
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
