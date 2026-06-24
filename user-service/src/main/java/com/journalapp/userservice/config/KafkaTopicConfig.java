package com.journalapp.userservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Auto-creates the user-events topic on startup (handy for local/dev). */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic userEventsTopic(@Value("${app.kafka.topics.user-events:user-events}") String topic) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }
}
