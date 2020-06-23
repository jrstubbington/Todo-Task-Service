package org.example.todo.tasks.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DatabaseReadinessCheck {

	@Autowired
	private KafkaProperties kafkaProperties;

	@Autowired
	private ApplicationContext appContext;

	public void checkSomething() {
		try {
			//...
		}
		catch (Exception e) {
			log.error("Failed to do the... thing.", e);
			AvailabilityChangeEvent.publish(appContext, ReadinessState.REFUSING_TRAFFIC);
		}
	}

}
