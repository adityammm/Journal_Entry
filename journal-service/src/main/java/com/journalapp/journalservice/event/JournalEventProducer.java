package com.journalapp.journalservice.event;

import com.journalapp.journalservice.entity.JournalEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Publishes JOURNAL_CREATED events to the journal-events topic. */
@Component
@Slf4j
public class JournalEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String journalEventsTopic;

    public JournalEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                                @Value("${app.kafka.topics.journal-events:journal-events}") String journalEventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.journalEventsTopic = journalEventsTopic;
    }

    public void publishJournalCreated(JournalEntry entry) {
        JournalCreatedEvent event = new JournalCreatedEvent(
                "JOURNAL_CREATED",
                entry.getId() != null ? entry.getId().toHexString() : null,
                entry.getUserName(),
                entry.getTitle(),
                System.currentTimeMillis());

        kafkaTemplate.send(journalEventsTopic, entry.getUserName(), event);
        log.info("Published JOURNAL_CREATED event for '{}' (title='{}') to topic '{}'",
                entry.getUserName(), entry.getTitle(), journalEventsTopic);
    }
}
