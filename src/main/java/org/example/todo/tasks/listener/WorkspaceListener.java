package org.example.todo.tasks.listener;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.common.dto.WorkspaceDto;
import org.example.todo.common.kafka.KafkaOperation;
import org.example.todo.common.util.Status;
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
public class WorkspaceListener {

	private final Set<UUID> workspaceUuidSet = Collections.synchronizedSet(
			Collections.newSetFromMap(
					new WeakHashMap<>()
			)
	);

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("{}", event.getSource());
		log.info("startup");
		//TODO: execute API call to accounts service on startup to get initial list of workspaces
	}

	@KafkaListener(topics = "workspaces")
	void listen(WorkspaceDto workspaceDto,
	            Acknowledgment acknowledgment,
	            @Header(value = "operation") KafkaOperation kafkaOperation) {
		log.trace("Received Message: {}", workspaceDto);
		UUID workspaceUuid = workspaceDto.getUuid();
		switch (Objects.requireNonNull(kafkaOperation)) {
			case CREATE:
				log.debug("Adding workspaceUuid {}", workspaceUuid);
				workspaceUuidSet.add(workspaceUuid);
				break;
			case DELETE:
				log.debug("Removing workspaceUuid {}", workspaceUuid);
				workspaceUuidSet.remove(workspaceUuid);
				break;
			case UPDATE:
				if (workspaceDto.getStatus() != Status.ACTIVE) {
					log.debug("Removing workspaceUuid {}", workspaceUuid);
					workspaceUuidSet.remove(workspaceUuid);
				}
				break;
			default:
				log.info("Unrecognized Kafka Operation: {}", kafkaOperation);
				break;

		}
		acknowledgment.acknowledge();
	}

	public boolean contains(UUID uuid) {
		return workspaceUuidSet.contains(uuid);
	}
}
