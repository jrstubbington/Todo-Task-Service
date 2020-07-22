package org.example.todo.tasks.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.example.todo.tasks.generated.controller.TaskManagementApi;
import org.example.todo.tasks.generated.dto.ResponseContainerTaskDto;
import org.example.todo.tasks.generated.dto.ResponseContainerWorkLogEntryDto;
import org.example.todo.tasks.generated.dto.TaskCreationRequest;
import org.example.todo.tasks.generated.dto.TaskDto;
import org.example.todo.tasks.service.TaskService;
import org.example.todo.tasks.service.WorkLogService;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Objects;
import java.util.UUID;

@RestController
@Api(tags = "Task Management")
@Validated
@Slf4j
@CrossOrigin(value = "*")
public class TaskController implements TaskManagementApi {

	//TODO: The link below may solve the issue of getting the Principle object. The current method isn't that bad though
	//	https://github.com/OpenAPITools/openapi-generator/issues/4680

	private TaskService taskService;

	private WorkLogService workLogService;

	@Autowired
	private HttpServletRequest request;

	@Override
	public ResponseEntity<ResponseContainerTaskDto> createTask(@Valid TaskCreationRequest taskCreationRequest) {
		return ResponseEntity.ok(taskService.createTaskResponse(taskCreationRequest));
	}

	@Override
	public ResponseEntity<ResponseContainerTaskDto> getTaskByUUID(UUID uuid) {
		return ResponseEntity.ok(taskService.findTaskByUuidResponse(uuid));
	}

	@Override
	@RolesAllowed("admin")
	public ResponseEntity<ResponseContainerTaskDto> getTaskListByUserUuid(UUID uuid) {
		return ResponseEntity.ok(taskService.getAllTasksByUserUuidResponse(uuid));
	}

	@Override
//	@RolesAllowed("admin")
	public ResponseEntity<ResponseContainerTaskDto> getTasks(@Valid Integer page, @Valid Integer pageSize) {
		Pair<UUID, AccessToken> authToken = processAccessToken();
		return ResponseEntity.ok(taskService.getAllTasksResponse(PageRequest.of(page, pageSize)));
	}

	@Override
	public ResponseEntity<ResponseContainerTaskDto> updateTask(@Valid TaskDto taskDto) {
		return ResponseEntity.ok(taskService.updateTaskResponse(taskDto));
	}

	/**
	 * GET /v1/tasks/{uuid}/worklogs : Get a specific task&#39;s information
	 *
	 * @param uuid Task uuid to get task object with (required)
	 * @return OK (status code 200)
	 * or Client Error (status code 400)
	 * or Not Found (status code 404)
	 * or Internal error has occurred (status code 500)
	 */
	@Override
	public ResponseEntity<ResponseContainerWorkLogEntryDto> getWorkLogsForTaskUuid(UUID uuid) {
		return ResponseEntity.ok(workLogService.getWorkLogEntriesForTaskUuidResponse(uuid));
	}

	//TODO: Move to utility package
	private Pair<UUID, AccessToken> processAccessToken() {
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) request.getUserPrincipal();
		if (Objects.nonNull(token)) {
			@SuppressWarnings("unchecked")
			KeycloakPrincipal<KeycloakSecurityContext> principal = (KeycloakPrincipal<KeycloakSecurityContext>) token.getPrincipal();
			KeycloakSecurityContext session = principal.getKeycloakSecurityContext();
			return new ImmutablePair<>(UUID.fromString(principal.getName()), session.getToken());
		}
		return new ImmutablePair<>(UUID.randomUUID(), new AccessToken());
	}


	@Autowired
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	@Autowired
	public void setWorkLogService(WorkLogService workLogService) {
		this.workLogService = workLogService;
	}
}
