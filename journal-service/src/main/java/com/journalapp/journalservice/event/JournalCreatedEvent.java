package com.journalapp.journalservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published to the "journal-events" topic when a new entry is saved.
 * notification-service consumes this. Serialized as plain JSON (no type headers).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalCreatedEvent {
    private String eventType;   // "JOURNAL_CREATED"
    private String journalId;
    private String userName;
    private String title;
    private long timestamp;
}
