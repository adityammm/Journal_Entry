package com.journalapp.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirror of journal-service's JournalCreatedEvent. Duplicated here (rather than shared via a
 * library) to honor the "no multi-module / each service standalone" constraint. The JSON shape
 * must stay in sync with the producer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalCreatedEvent {
    private String eventType;
    private String journalId;
    private String userName;
    private String title;
    private long timestamp;
}
