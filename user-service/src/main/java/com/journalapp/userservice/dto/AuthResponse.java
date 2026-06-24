package com.journalapp.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Returned by POST /auth/login: the bearer token plus a little context for the client. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String userName;
    private List<String> roles;
}
