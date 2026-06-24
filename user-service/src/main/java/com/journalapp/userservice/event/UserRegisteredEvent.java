package com.journalapp.userservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published to the "user-events" topic when a new account is created.
 * Serialized as plain JSON (no type headers) so any service can consume it.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {
    private String eventType;   // "USER_REGISTERED"
    private String userId;
    private String userName;
    private String email;
    private long timestamp;
}
