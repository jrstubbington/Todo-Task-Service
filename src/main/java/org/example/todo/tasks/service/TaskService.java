package org.example.todo.tasks.service;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.common.exceptions.ImproperResourceSpecification;
import org.example.todo.common.exceptions.ResourceNotFoundException;
import org.example.todo.common.kafka.KafkaOperation;
import org.example.todo.common.kafka.KafkaProducer;
import org.example.todo.common.util.ResponseContainer;
import org.example.todo.common.util.ResponseUtils;
import org.example.todo.tasks.dto.CategoryDto;
import org.example.todo.tasks.dto.TaskCreationRequest;
import org.example.todo.tasks.dto.TaskDto;
import org.example.todo.tasks.listener.UserListener;
import org.example.todo.tasks.listener.WorkspaceListener;
import org.example.todo.tasks.model.Category;
import org.example.todo.tasks.model.Task;
import org.example.todo.tasks.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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
	public ResponseContainer<TaskDto> getAllTasksResponse(PageRequest pageRequest) {
		return ResponseUtils.pageToDtoResponseContainer(taskRepository.findAll(pageRequest), TaskDto.class);
	}

	public Task findTaskByUuid(UUID uuid) throws ResourceNotFoundException {
		return taskRepository.findByUuid(uuid).orElseThrow(() -> new ResourceNotFoundException(String.format("User not found with id: %s", uuid)));
	}

	public ResponseContainer<TaskDto> findTaskByUuidResponse(UUID uuid) throws ResourceNotFoundException {
		return ResponseUtils.pageToDtoResponseContainer(Collections.singletonList(findTaskByUuid(uuid)), TaskDto.class);
	}

	@Transactional
	public Task updateTask(TaskDto taskDto) throws ResourceNotFoundException, ImproperResourceSpecification {
		if (Objects.nonNull(taskDto.getUuid())) {
			// Task is being updated
			log.debug("Updating task {}", taskDto);
			Task task = findTaskByUuid(taskDto.getUuid());

			if (!userListener.contains(taskDto.getAssignedToUserUuid())) {
				throw new ResourceNotFoundException(String.format("User with id, %s could not be found to assign task to.", taskDto.getAssignedToUserUuid()));
			}
			if(!workspaceListener.contains(taskDto.getWorkspaceUuid())) {
				throw new ResourceNotFoundException(String.format("Workspace with id, %s could not be found to assign task to.", taskDto.getWorkspaceUuid()));
			}

			task.setAssignedToUserUuid(taskDto.getAssignedToUserUuid());
			task.setName(taskDto.getName());
			task.setDescription(taskDto.getDescription());
			task.setPriority(taskDto.hashCode());
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
	public ResponseContainer<TaskDto> updateTaskResponse(TaskDto taskUpdate) throws ResourceNotFoundException, ImproperResourceSpecification {
		return ResponseUtils.pageToDtoResponseContainer(Collections.singletonList(updateTask(taskUpdate)), TaskDto.class);
	}

	@Transactional
	public Task createTask(TaskCreationRequest taskCreationRequest) throws ImproperResourceSpecification, ResourceNotFoundException {
		TaskDto taskDto = Objects.requireNonNull(taskCreationRequest.getTask());
		CategoryDto categoryDto = Objects.requireNonNull(taskCreationRequest.getCategory());

		if (Objects.nonNull(taskDto.getUuid())) {
			throw new ImproperResourceSpecification("Cannot specify UUID of task when creating new task");
		}

		if (Objects.isNull(categoryDto.getUuid())) {
			throw new ImproperResourceSpecification("Must specify UUID of category when creating new task. Create a new category before creating task.");
		}

		if (!userListener.contains(taskDto.getAssignedToUserUuid())) {
			throw new ResourceNotFoundException(String.format("User with id, %s could not be found to assign task to.", taskDto.getAssignedToUserUuid()));
		}
		if(!workspaceListener.contains(taskDto.getWorkspaceUuid())) {
			throw new ResourceNotFoundException(String.format("Workspace with id, %s could not be found to assign task to.", taskDto.getWorkspaceUuid()));
		}

		Category category = categoryService.findCategoryByUuid(categoryDto.getUuid());
		Task task = new Task();
		task.setCategory(category);
		task.setAssignedToUserUuid(taskDto.getAssignedToUserUuid());
		task.setName(taskDto.getName());
		task.setDescription(taskDto.getDescription());
		task.setPriority(taskDto.hashCode());
		task.setWorkspaceUuid(taskDto.getWorkspaceUuid());
		task.setReminderDate(taskDto.getReminderDate());

		Task savedTask = taskRepository.saveAndFlush(task);

		categoryService.addTaskToCategory(category, savedTask);

		kafkaProducer.sendMessage(KAFKA_TOPIC, KafkaOperation.CREATE,
				ResponseUtils.convertToDto(savedTask, TaskDto.class));

		return savedTask;
	}

	@Transactional
	public ResponseContainer<TaskDto> createTaskResponse(TaskCreationRequest taskCreationRequest) throws ImproperResourceSpecification, ResourceNotFoundException {
		return ResponseUtils.pageToDtoResponseContainer(Collections.singletonList(createTask(taskCreationRequest)), TaskDto.class);
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
