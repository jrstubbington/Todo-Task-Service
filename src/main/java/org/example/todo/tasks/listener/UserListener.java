package org.example.todo.tasks.listener;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.common.dto.UserDto;
import org.example.todo.common.kafka.KafkaOperation;
import org.example.todo.common.util.Status;
import org.example.todo.tasks.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

@Component
@Slf4j
public class UserListener {
	private final Set<UUID> userUuidSet = Collections.synchronizedSet(
			Collections.newSetFromMap(
					new WeakHashMap<>()
			)
	);

	private TaskService taskService;

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("{}", event.getSource());
		log.info("startup");
		//TODO: execute API call to accounts service on startup to get initial list of users
	}

	@KafkaListener(topics = "users")
	void listen(UserDto userDto,
	            Acknowledgment acknowledgment,
	            @Header(value = "operation") KafkaOperation kafkaOperation) {
		log.trace("Received Message: {}", userDto);
		UUID userUuid = userDto.getUuid();
		switch (Objects.requireNonNull(kafkaOperation)) {
			case CREATE:
				log.debug("Adding userUuid {}", userUuid);
				userUuidSet.add(userUuid);
				break;
			case DELETE:
				log.debug("Removing userUuid {}", userUuid);
				userUuidSet.remove(userUuid);
				taskService.reassignAllUserTasks(userUuid, null);
				break;
			case UPDATE:
				if (userDto.getStatus() != Status.ACTIVE) {
					log.debug("Removing userUuid {}", userUuid);
					userUuidSet.remove(userUuid);
				}
				break;
			default:
				log.info("Unrecognized Kafka Operation: {}", kafkaOperation);
				break;
		}
		acknowledgment.acknowledge();
	}

	public boolean contains(UUID uuid) {
		return userUuidSet.contains(uuid);
	}

	@Autowired
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}
}
