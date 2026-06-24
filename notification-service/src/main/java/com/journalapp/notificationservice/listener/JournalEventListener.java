package com.journalapp.notificationservice.listener;

import com.journalapp.notificationservice.event.JournalCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Consumes JOURNAL_CREATED events from the journal-events topic. For now it just logs the event,
 * simulating "send a notification". This is where you'd later send an email/push/SMS.
 */
@Component
@Slf4j
public class JournalEventListener {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    @KafkaListener(
            topics = "${app.kafka.topics.journal-events:journal-events}",
            groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void onJournalCreated(JournalCreatedEvent event) {
        String when = event.getTimestamp() > 0 ? FMT.format(Instant.ofEpochMilli(event.getTimestamp())) : "unknown";
        log.info("==================== NOTIFICATION ====================");
        log.info("Event       : {}", event.getEventType());
        log.info("User        : {}", event.getUserName());
        log.info("Journal id  : {}", event.getJournalId());
        log.info("Title       : {}", event.getTitle());
        log.info("Created at  : {}", when);
        log.info("Action      : (simulated) sending notification to '{}'", event.getUserName());
        log.info("======================================================");
    }
}
