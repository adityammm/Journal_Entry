package com.journalapp.userservice.event;

import com.journalapp.userservice.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Publishes USER_REGISTERED events to Kafka. */
@Component
@Slf4j
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String userEventsTopic;

    public UserEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                             @Value("${app.kafka.topics.user-events:user-events}") String userEventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.userEventsTopic = userEventsTopic;
    }

    public void publishUserRegistered(User user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                "USER_REGISTERED",
                user.getId() != null ? user.getId().toHexString() : null,
                user.getUserName(),
                user.getEmail(),
                System.currentTimeMillis());

        // Key by username so all events for one user land on the same partition (ordering).
        kafkaTemplate.send(userEventsTopic, user.getUserName(), event);
        log.info("Published USER_REGISTERED event for '{}' to topic '{}'", user.getUserName(), userEventsTopic);
    }
}
