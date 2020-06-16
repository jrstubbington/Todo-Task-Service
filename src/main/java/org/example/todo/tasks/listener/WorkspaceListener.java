package org.example.todo.tasks.listener;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.accounts.controller.WorkspaceManagementApi;
import org.example.todo.accounts.dto.ResponseContainerWorkspaceDto;
import org.example.todo.accounts.dto.WorkspaceDto;
import org.example.todo.common.kafka.KafkaOperation;
import org.example.todo.common.util.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class WorkspaceListener {

	@Autowired
	private WorkspaceManagementApi workspaceManagementApi;

	//NOTE: This violates separation of stateless and stateful. This setup wouldn't work in situations where there's
	// more than one service instance. This storage would need to be moved to a central cache like redis
	private final Set<UUID> workspaceUuidSet = Collections.synchronizedSet(
			Collections.newSetFromMap(
					new HashMap<>()
			)
	);

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.trace("{}", event.getSource());
		log.trace("startup");
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
				if (!workspaceDto.getStatus().getValue().equalsIgnoreCase(Status.ACTIVE.toString())) {
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
		boolean found = workspaceUuidSet.contains(uuid);
		if (!found) {
			//TODO: execute api call to REALLY check to make sure it doesn't exist
			//TODO: Replace with auto-generated client library
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			//set my entity
			HttpEntity<Object> entity = new HttpEntity<>(headers);
			try {
				ResponseEntity responseEntity = restTemplate.exchange("http://localhost:8081/v1/workspaces/" + uuid, HttpMethod.GET, entity, String.class);
				ResponseContainerWorkspaceDto responseContainerWorkspaceDto = workspaceManagementApi.getWorkspaceByUUID(uuid);
				if (responseEntity.getStatusCode().is2xxSuccessful()) {
//				workspaceUuidSet.add(uuid);
					found = true;
				}
			}
			catch (Exception e){
				found = false;
			}

		}
		return found;
	}
}
