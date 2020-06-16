package org.example.todo.tasks.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.todo.common.exceptions.ErrorDetails;
import org.example.todo.common.exceptions.ImproperResourceSpecification;
import org.example.todo.common.exceptions.ResourceNotFoundException;
import org.example.todo.common.util.ResponseContainer;
import org.example.todo.common.util.verification.group.Create;
import org.example.todo.common.util.verification.group.Update;
import org.example.todo.tasks.dto.TaskCreationRequest;
import org.example.todo.tasks.dto.TaskDto;
import org.example.todo.tasks.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/tasks")
@Tag(name = "Task Management", description = "A collection of APIs designed to handle functions related to Task management")
@Validated
@Slf4j
public class TaskController {

	private TaskService taskService;

	@Operation(summary = "View a list of available tasks")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "500", description = "Internal error has occurred", content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
	})
	@GetMapping(value = "/", produces={"application/json"})
	public ResponseEntity<ResponseContainer<TaskDto>> getTasks(
			@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
		return ResponseEntity.ok(taskService.getAllTasksResponse(PageRequest.of(page, pageSize)));
	}

	@Operation(summary = "Get a specific task's information")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode  = "500", description = "Internal error has occurred", content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
	})
	@GetMapping(value = "/{uuid}", produces={"application/json"})
	public ResponseEntity<ResponseContainer<TaskDto>> getTaskByUUID(
			@Parameter(description ="Task uuid to get task object with") @PathVariable UUID uuid) throws ResourceNotFoundException {
		return ResponseEntity.ok(taskService.findTaskByUuidResponse(uuid));
	}

	@Operation(summary = "Update an existing task")
	@ApiResponses(value = {
			@ApiResponse(responseCode  = "200", description = "OK"),
			@ApiResponse(responseCode  = "400", description = "Client Error", content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode  = "500", description = "Internal error has occurred", content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
	})
	@PutMapping(value = "/", produces={"application/json"})
	public ResponseEntity<ResponseContainer<TaskDto>> updateTask(
			@Validated(Update.class) @RequestBody TaskDto taskDto) throws ResourceNotFoundException, ImproperResourceSpecification {
		return ResponseEntity.ok(taskService.updateTaskResponse(taskDto));
	}

	@Operation(summary = "Create a new task")
	@ApiResponses(value = {
			@ApiResponse(responseCode  = "200", description = "OK"),
			@ApiResponse(responseCode  = "400", description = "Client Error", content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode  = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
	})
	@PostMapping(value = "/", produces={"application/json"})
	public ResponseEntity<ResponseContainer<TaskDto>> createTask(
			@Validated(Create.class) @RequestBody TaskCreationRequest taskCreationRequest) throws ImproperResourceSpecification, ResourceNotFoundException {
		return ResponseEntity.ok(taskService.createTaskResponse(taskCreationRequest));
	}

	@Operation(summary = "Get a list of tasks for a specific user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode  = "500", description = "Internal error has occurred", content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
	})
	@GetMapping(value = "/user/{uuid}", produces={"application/json"})
	public ResponseEntity<ResponseContainer<TaskDto>> getTaskListByUserUuid(
			@Parameter(description ="User uuid to get list of tasks with") @PathVariable UUID uuid) throws ResourceNotFoundException {
		return ResponseEntity.ok(taskService.getAllTasksByUserUuidResponse(uuid));
	}

	@Autowired
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}
}
