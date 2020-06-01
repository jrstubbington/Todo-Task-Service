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

	@Autowired
	public void setTaskRepository(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}
}
