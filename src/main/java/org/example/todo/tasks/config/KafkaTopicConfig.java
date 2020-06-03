package org.example.todo.tasks.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

	private static final int KAFKA_DEFAULT_TOPIC_USER_PARTITIONS = 10;

	private static final short KAFKA_DEFAULT_TOPIC_REPLICA_COUNT = 1;

	@Bean
	public NewTopic categoriesTopic() {
		return new NewTopic("categories", KAFKA_DEFAULT_TOPIC_USER_PARTITIONS, KAFKA_DEFAULT_TOPIC_REPLICA_COUNT);
	}

	@Bean
	public NewTopic tasksTopic() {
		return new NewTopic("tasks", KAFKA_DEFAULT_TOPIC_USER_PARTITIONS, KAFKA_DEFAULT_TOPIC_REPLICA_COUNT);
	}
}
