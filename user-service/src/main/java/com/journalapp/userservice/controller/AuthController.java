package com.journalapp.userservice.controller;

import com.journalapp.userservice.dto.AuthResponse;
import com.journalapp.userservice.dto.LoginRequest;
import com.journalapp.userservice.dto.RegisterRequest;
import com.journalapp.userservice.entity.User;
import com.journalapp.userservice.security.JwtService;
import com.journalapp.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getUserName() == null || request.getUserName().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return new ResponseEntity<>(Map.of("error", "userName and password are required"),
                    HttpStatus.BAD_REQUEST);
        }
        if (userService.userExists(request.getUserName())) {
            return new ResponseEntity<>(Map.of("error", "Username already taken"), HttpStatus.CONFLICT);
        }

        userService.register(request); // saves and publishes USER_REGISTERED
        return new ResponseEntity<>(Map.of("message", "User registered successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword()));

            User user = userService.findByUserName(request.getUserName());
            String token = jwtService.generateToken(user.getUserName(), user.getRoles());
            return new ResponseEntity<>(new AuthResponse(token, user.getUserName(), user.getRoles()), HttpStatus.OK);
        } catch (AuthenticationException e) {
            log.warn("Failed login attempt for user: {}", request.getUserName());
            return new ResponseEntity<>(Map.of("error", "Invalid username or password"), HttpStatus.UNAUTHORIZED);
        }
    }
}
