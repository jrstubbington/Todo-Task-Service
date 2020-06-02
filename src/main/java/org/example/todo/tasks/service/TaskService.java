package org.example.todo.tasks.service;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.common.util.ResponseContainer;
import org.example.todo.common.util.ResponseUtils;
import org.example.todo.tasks.dto.TaskDto;
import org.example.todo.tasks.model.Task;
import org.example.todo.tasks.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class TaskService {
	private TaskRepository taskRepository;

	//TODO: Enable filtering and sorting
	public List<Task> getAllTasks() {
		return taskRepository.findAll();
	}

	//TODO: Enable filtering and sorting
	public ResponseContainer<TaskDto> getAllTasksResponse(PageRequest pageRequest) {
		return ResponseUtils.pageToDtoResponseContainer(taskRepository.findAll(pageRequest), TaskDto.class);
	}

	@Transactional
	public Task createTask() {
		return null;
	}

	@Transactional
	public void reassignAllUserTasks(UUID assignedUUID, UUID newUuid) {
		Set<Task> assignedTasks = taskRepository.findDistinctByAssignedToUserUuid(assignedUUID);
		assignedTasks.forEach(task -> task.setAssignedToUserUuid(newUuid));
		taskRepository.saveAll(assignedTasks);
	}

	@Autowired
	public void setTaskRepository(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}
}
