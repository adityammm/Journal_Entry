package com.journalapp.journalservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Auto-creates the journal-events topic on startup. */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic journalEventsTopic(@Value("${app.kafka.topics.journal-events:journal-events}") String topic) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }
}
