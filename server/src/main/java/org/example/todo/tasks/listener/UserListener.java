package org.example.todo.tasks.listener;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.accounts.generated.controller.UserManagementApi;
import org.example.todo.accounts.generated.dto.ResponseContainerUserDto;
import org.example.todo.accounts.generated.dto.UserDto;
import org.example.todo.common.exceptions.ResourceNotFoundException;
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
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class UserListener {

	@Autowired
	private UserManagementApi userManagementApi;

	//NOTE: This violates separation of stateless and stateful. This setup wouldn't work in situations where there's
	// more than one service instance. This storage would need to be moved to a central cache like redis
	private final Set<UUID> userUuidSet = Collections.synchronizedSet(
			Collections.newSetFromMap(
					new HashMap<>()
			)
	);

	private TaskService taskService;

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.trace("{}", event.getSource());
		log.trace("startup");
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
				if (!userDto.getStatus().getValue().equalsIgnoreCase(Status.ACTIVE.toString())) {
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

	public boolean doesNotContain(UUID uuid) {
		boolean found = userUuidSet.contains(uuid);
		found = true;
		if (!found) {
			try {
				ResponseContainerUserDto responseContainerUserDto = userManagementApi.getUserByUUID(uuid);
				if (!responseContainerUserDto.getData().isEmpty()) {
//					userUuidSet.add(uuid); //TODO: decide if a memory-map is necessary
					found = true;
				}
			}
			catch (ResourceNotFoundException e) {
				log.debug("{}", e.getMessage());
				log.trace("Error:", e);
			}
			catch (RestClientException e) {
				log.info("Failed to do the thing");
				log.trace("Error:", e);
			}
		}
		return !found;
	}

	@Autowired
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}
}
