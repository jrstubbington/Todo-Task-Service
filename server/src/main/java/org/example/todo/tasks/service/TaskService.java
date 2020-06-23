package org.example.todo.tasks.service;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.common.exceptions.ImproperResourceSpecification;
import org.example.todo.common.exceptions.ResourceNotFoundException;
import org.example.todo.common.kafka.KafkaOperation;
import org.example.todo.common.kafka.KafkaProducer;
import org.example.todo.common.util.ResponseUtils;
import org.example.todo.tasks.generated.dto.ResponseContainerTaskDto;
import org.example.todo.tasks.generated.dto.TaskCreationRequest;
import org.example.todo.tasks.generated.dto.TaskDto;
import org.example.todo.tasks.listener.UserListener;
import org.example.todo.tasks.listener.WorkspaceListener;
import org.example.todo.tasks.model.Category;
import org.example.todo.tasks.model.Task;
import org.example.todo.tasks.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class TaskService {

	private TaskRepository taskRepository;

	private KafkaProducer<TaskDto> kafkaProducer;

	private static final String KAFKA_TOPIC = "tasks";

	private CategoryService categoryService;

	private WorkspaceListener workspaceListener;

	private UserListener userListener;

	//TODO: Enable filtering and sorting
	public List<Task> getAllTasks() {
		return taskRepository.findAll();
	}

	//TODO: Enable filtering and sorting
	public ResponseContainerTaskDto getAllTasksResponse(PageRequest pageRequest) {
		return ResponseUtils.convertToDtoResponseContainer(taskRepository.findAll(pageRequest), TaskDto.class, ResponseContainerTaskDto.class);
	}

	public Task findTaskByUuid(UUID uuid) {
		return taskRepository.findByUuid(uuid).orElseThrow(() -> new ResourceNotFoundException(String.format("Task not found with id: %s", uuid)));
	}

	public ResponseContainerTaskDto findTaskByUuidResponse(UUID uuid) {
		return ResponseUtils.convertToDtoResponseContainer(findTaskByUuid(uuid), TaskDto.class, ResponseContainerTaskDto.class);
	}

	@Transactional
	public Task updateTask(TaskDto taskDto){
		if (Objects.nonNull(taskDto.getUuid())) {
			// Task is being updated
			log.debug("Updating task {}", taskDto);
			Task task = findTaskByUuid(taskDto.getUuid());

			if (userListener.doesNotContain(taskDto.getAssignedToUserUuid())) {
				throw new ResourceNotFoundException(String.format("User with id, %s could not be found to assign task to.", taskDto.getAssignedToUserUuid()));
			}
			if (userListener.doesNotContain(taskDto.getCreatedByUserUuid())) {
				throw new ResourceNotFoundException(String.format("User with id, %s could not be found to assign task created by.", taskDto.getAssignedToUserUuid()));
			}
			if(workspaceListener.doesNotContain(taskDto.getWorkspaceUuid())) {
				throw new ResourceNotFoundException(String.format("Workspace with id, %s could not be found to assign task to.", taskDto.getWorkspaceUuid()));
			}

			task.setAssignedToUserUuid(taskDto.getAssignedToUserUuid());
			task.setName(taskDto.getName());
			task.setDescription(taskDto.getDescription());
			task.setPriority(taskDto.getPriority());
			task.setWorkspaceUuid(taskDto.getWorkspaceUuid());
			task.setReminderDate(taskDto.getReminderDate());

			Task savedTask = taskRepository.saveAndFlush(task);

			kafkaProducer.sendMessage(KAFKA_TOPIC, KafkaOperation.UPDATE,
					ResponseUtils.convertToDto(savedTask, TaskDto.class));

			return savedTask;
		}
		else {
			throw new ImproperResourceSpecification("Must specify a UUID when updating a task");
		}
	}

	@Transactional
	public ResponseContainerTaskDto updateTaskResponse(TaskDto taskUpdate) {
		return ResponseUtils.convertToDtoResponseContainer(updateTask(taskUpdate), TaskDto.class, ResponseContainerTaskDto.class);
	}

	@Transactional
	public Task createTask(TaskCreationRequest taskCreationRequest) {
		TaskDto taskDto = Objects.requireNonNull(taskCreationRequest.getTask());
		UUID categoryUuid = Objects.requireNonNull(taskCreationRequest.getCategoryUuid());

		if (Objects.nonNull(taskDto.getUuid())) {
			throw new ImproperResourceSpecification("Cannot specify UUID of task when creating new task");
		}

		if (userListener.doesNotContain(taskDto.getAssignedToUserUuid())) {
			throw new ResourceNotFoundException(String.format("User with id, %s could not be found to assign task to.", taskDto.getAssignedToUserUuid()));
		}
		if(workspaceListener.doesNotContain(taskDto.getWorkspaceUuid())) {
			throw new ResourceNotFoundException(String.format("Workspace with id, %s could not be found to assign task to.", taskDto.getWorkspaceUuid()));
		}

		Category category = categoryService.findCategoryByUuid(categoryUuid);
		Task task = new Task();
		task.setCategory(category);
		task.setCreatedByUserUuid(taskDto.getCreatedByUserUuid());
		task.setAssignedToUserUuid(taskDto.getAssignedToUserUuid());
		task.setStatus(taskDto.getStatus());
		task.setName(taskDto.getName());
		task.setDescription(taskDto.getDescription());
		task.setPriority(taskDto.getPriority());
		task.setWorkspaceUuid(taskDto.getWorkspaceUuid());
		task.setReminderDate(taskDto.getReminderDate());

		Task savedTask = taskRepository.saveAndFlush(task);

		categoryService.addTaskToCategory(category, savedTask);

		kafkaProducer.sendMessage(KAFKA_TOPIC, KafkaOperation.CREATE,
				ResponseUtils.convertToDto(savedTask, TaskDto.class));

		return savedTask;
	}

	@Transactional
	public ResponseContainerTaskDto createTaskResponse(TaskCreationRequest taskCreationRequest) {
		return ResponseUtils.convertToDtoResponseContainer(createTask(taskCreationRequest), TaskDto.class, ResponseContainerTaskDto.class);
	}

	@Transactional
	public void reassignAllUserTasks(UUID assignedUUID, UUID newUuid) {
		Set<Task> assignedTasks = taskRepository.findAllByAssignedToUserUuid(assignedUUID);
		assignedTasks.forEach(task -> task.setAssignedToUserUuid(newUuid));
		taskRepository.saveAll(assignedTasks);
	}

	@Transactional
	public Set<Task> getAllTasksByUserUuid(UUID userUuid) {
		return taskRepository.findAllByAssignedToUserUuid(userUuid);
	}

	@Transactional
	public ResponseContainerTaskDto getAllTasksByUserUuidResponse(UUID userUuid) {
		return ResponseUtils.convertToDtoResponseContainer(new ArrayList<>(getAllTasksByUserUuid(userUuid)), TaskDto.class, ResponseContainerTaskDto.class);
	}

	@Autowired
	public void setTaskRepository(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@Autowired
	public void setCategoryService(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@Autowired
	public void setKafkaProducer(KafkaProducer<TaskDto> kafkaProducer) {
		this.kafkaProducer = kafkaProducer;
	}

	@Autowired
	public void setWorkspaceListener(WorkspaceListener workspaceListener) {
		this.workspaceListener = workspaceListener;
	}

	@Autowired
	public void setUserListener(UserListener userListener) {
		this.userListener = userListener;
	}
}
