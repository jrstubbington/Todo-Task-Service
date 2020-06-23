package org.example.todo.tasks.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.example.todo.tasks.generated.controller.TaskManagementApi;
import org.example.todo.tasks.generated.dto.ResponseContainerTaskDto;
import org.example.todo.tasks.generated.dto.ResponseContainerWorkLogEntryDto;
import org.example.todo.tasks.generated.dto.TaskCreationRequest;
import org.example.todo.tasks.generated.dto.TaskDto;
import org.example.todo.tasks.service.TaskService;
import org.example.todo.tasks.service.WorkLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@Api(tags = "Task Management")
@Validated
@Slf4j
public class TaskController implements TaskManagementApi {

	private TaskService taskService;

	private WorkLogService workLogService;

	@Override
	public ResponseEntity<ResponseContainerTaskDto> createTask(@Valid TaskCreationRequest taskCreationRequest) {
		return ResponseEntity.ok(taskService.createTaskResponse(taskCreationRequest));
	}

	@Override
	public ResponseEntity<ResponseContainerTaskDto> getTaskByUUID(UUID uuid) {
		return ResponseEntity.ok(taskService.findTaskByUuidResponse(uuid));
	}

	@Override
	public ResponseEntity<ResponseContainerTaskDto> getTaskListByUserUuid(UUID uuid) {
		return ResponseEntity.ok(taskService.getAllTasksByUserUuidResponse(uuid));
	}

	@Override
	public ResponseEntity<ResponseContainerTaskDto> getTasks(@Valid Integer page, @Valid Integer pageSize) {
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

	@Autowired
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	@Autowired
	public void setWorkLogService(WorkLogService workLogService) {
		this.workLogService = workLogService;
	}
}
