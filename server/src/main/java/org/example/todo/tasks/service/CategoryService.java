package org.example.todo.tasks.service;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.common.exceptions.ImproperResourceSpecification;
import org.example.todo.common.exceptions.ResourceNotFoundException;
import org.example.todo.common.kafka.KafkaOperation;
import org.example.todo.common.kafka.KafkaProducer;
import org.example.todo.common.util.ResponseUtils;
import org.example.todo.tasks.generated.dto.CategoryDto;
import org.example.todo.tasks.generated.dto.ResponseContainerCategoryDto;
import org.example.todo.tasks.generated.dto.ResponseContainerTaskDto;
import org.example.todo.tasks.generated.dto.TaskDto;
import org.example.todo.tasks.listener.UserListener;
import org.example.todo.tasks.listener.WorkspaceListener;
import org.example.todo.tasks.model.Category;
import org.example.todo.tasks.model.Task;
import org.example.todo.tasks.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@EnableKafka
public class CategoryService {

	private CategoryRepository categoryRepository;

	private WorkspaceListener workspaceListener;

	private UserListener userListener;

	private TaskService taskService;

	private KafkaProducer<CategoryDto> kafkaProducer;

	private static final String KAFKA_TOPIC = "categories";

	//TODO: Enable filtering and sorting
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	//TODO: Enable filtering and sorting
	public ResponseContainerCategoryDto getAllCategoriesResponse(PageRequest pageRequest) {
		return ResponseUtils.convertToDtoResponseContainer(categoryRepository.findAll(pageRequest), CategoryDto.class, ResponseContainerCategoryDto.class);
	}

	public Category findCategoryByUuid(UUID uuid) {
		return categoryRepository.findByUuid(uuid).orElseThrow(() -> new ResourceNotFoundException(String.format("Category not found with id: %s", uuid)));
	}

	public ResponseContainerCategoryDto findCategoryByUuidResponse(UUID uuid) {
		return ResponseUtils.convertToDtoResponseContainer(findCategoryByUuid(uuid), CategoryDto.class, ResponseContainerCategoryDto.class);
	}

	public void addTaskToCategory(UUID categoryUuid, Task task) {
		Category category = findCategoryByUuid(categoryUuid);

		Set<Task> tasks = category.getTasks();
		tasks.add(task);

		categoryRepository.saveAndFlush(category);
	}

	public List<Category> findCategoriesByWorkspaceUuid(UUID uuid) {
		return categoryRepository.findByWorkspaceUuid(uuid);
	}

	public ResponseContainerCategoryDto findCategoriesByWorkspaceUuidResponse(UUID uuid) {
		return ResponseUtils.convertToDtoResponseContainer(findCategoriesByWorkspaceUuid(uuid), CategoryDto.class, ResponseContainerCategoryDto.class);

	}

	@Transactional
	public void addTaskToCategory(Category category, Task task) {

		Set<Task> tasks = category.getTasks();
		tasks.add(task);

		categoryRepository.saveAndFlush(category);
	}

	@Transactional
	public Category createCategory(CategoryDto categoryDto) {
		UUID workspaceUuid = categoryDto.getWorkspaceUuid();
		if(Objects.nonNull(categoryDto.getUuid())) {
			throw new ImproperResourceSpecification("Cannot specify UUID when creating new category");
		}

		if (userListener.doesNotContain(categoryDto.getCreatedByUserUuid())) {
			throw new ResourceNotFoundException(String.format("user with id, %s could not be found to create category to.", categoryDto.getCreatedByUserUuid()));
		}
		if(workspaceListener.doesNotContain(categoryDto.getWorkspaceUuid())) {
			throw new ResourceNotFoundException(String.format("Workspace with id, %s could not be found to create category to.", categoryDto.getWorkspaceUuid()));
		}

		Category category = Category.builder()
				.name(categoryDto.getName())
				.description(categoryDto.getDescription())
				.createdByUserUuid(UUID.randomUUID()) //TODO: Change to authenticated user id
				.workspaceUuid(workspaceUuid)
				.color(categoryDto.getColor())
				.build();
		Category savedCategory = categoryRepository.saveAndFlush(category);

		kafkaProducer.sendMessage(KAFKA_TOPIC, KafkaOperation.UPDATE,
				ResponseUtils.convertToDto(savedCategory, CategoryDto.class));

		return savedCategory;

	}

	@Transactional
	public ResponseContainerCategoryDto createCategoryResponse(CategoryDto categoryDto) {
		return ResponseUtils.convertToDtoResponseContainer(createCategory(categoryDto), CategoryDto.class, ResponseContainerCategoryDto.class);
	}

	public ResponseContainerTaskDto getTasksByCategoryUUIDResponse(UUID uuid) {
		return ResponseUtils.convertToDtoResponseContainer(taskService.getTasksByCategoryUUID(uuid), TaskDto.class, ResponseContainerTaskDto.class);
	}

	@Autowired
	public void setCategoryRepository(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}


	@Autowired
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	@Autowired
	public void setUserListener(UserListener userListener) {
		this.userListener = userListener;
	}

	@Autowired
	public void setKafkaProducer(KafkaProducer<CategoryDto> kafkaProducer) {
		this.kafkaProducer = kafkaProducer;
	}

	@Autowired
	public void setWorkspaceListener(WorkspaceListener workspaceListener) {
		this.workspaceListener = workspaceListener;
	}
}
