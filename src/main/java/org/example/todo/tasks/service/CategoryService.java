package org.example.todo.tasks.service;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.common.dto.CategoryDto;
import org.example.todo.common.exceptions.ImproperResourceSpecification;
import org.example.todo.common.exceptions.ResourceNotFoundException;
import org.example.todo.common.kafka.KafkaOperation;
import org.example.todo.common.kafka.KafkaProducer;
import org.example.todo.common.util.ResponseContainer;
import org.example.todo.common.util.ResponseUtils;
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

import java.util.Collections;
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

	private KafkaProducer<CategoryDto> kafkaProducer;

	private static final String KAFKA_TOPIC = "categories";

	//TODO: Enable filtering and sorting
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	//TODO: Enable filtering and sorting
	public ResponseContainer<CategoryDto> getAllCategoriesResponse(PageRequest pageRequest) {
		return ResponseUtils.pageToDtoResponseContainer(categoryRepository.findAll(pageRequest), CategoryDto.class);
	}

	public Category findCategoryByUuid(UUID uuid) throws ResourceNotFoundException {
		return categoryRepository.findByUuid(uuid).orElseThrow(() -> new ResourceNotFoundException(String.format("Category not found with id: %s", uuid)));
	}

	public ResponseContainer<CategoryDto> findCategoryByUuidResponse(UUID uuid) throws ResourceNotFoundException {
		return ResponseUtils.pageToDtoResponseContainer(Collections.singletonList(findCategoryByUuid(uuid)), CategoryDto.class);
	}

	public void addTaskToCategory(UUID categoryUuid, Task task) throws ResourceNotFoundException {
		Category category = findCategoryByUuid(categoryUuid);

		Set<Task> tasks = category.getTasks();
		tasks.add(task);

		categoryRepository.saveAndFlush(category);
	}

	@Transactional
	public void addTaskToCategory(Category category, Task task) {

		Set<Task> tasks = category.getTasks();
		tasks.add(task);

		categoryRepository.saveAndFlush(category);
	}

	@Transactional
	public Category createCategory(CategoryDto categoryDto) throws ResourceNotFoundException, ImproperResourceSpecification {
		UUID workspaceUuid = categoryDto.getWorkspaceUuid();
		if(Objects.nonNull(categoryDto.getUuid())) {
			throw new ImproperResourceSpecification("Cannot specify UUID when creating new category");
		}

		if (!userListener.contains(categoryDto.getCreatedByUserUuid())) {
			throw new ResourceNotFoundException(String.format("user with id, %s could not be found to create category to.", categoryDto.getCreatedByUserUuid()));
		}
		if(!workspaceListener.contains(categoryDto.getWorkspaceUuid())) {
			throw new ResourceNotFoundException(String.format("Workspace with id, %s could not be found to create category to.", categoryDto.getWorkspaceUuid()));
		}

		Category category = Category.builder()
				.name(categoryDto.getName())
				.description(categoryDto.getDescription())
				.createdByUserUuid(categoryDto.getCreatedByUserUuid())
				.workspaceUuid(workspaceUuid)
				.build();
		Category savedCategory = categoryRepository.saveAndFlush(category);

		kafkaProducer.sendMessage(KAFKA_TOPIC, KafkaOperation.UPDATE,
				ResponseUtils.convertToDto(savedCategory, CategoryDto.class));

		return savedCategory;

	}

	@Transactional
	public ResponseContainer<CategoryDto> createCategoryResponse(CategoryDto categoryDto) throws ResourceNotFoundException, ImproperResourceSpecification {
		return ResponseUtils.pageToDtoResponseContainer(Collections.singletonList(createCategory(categoryDto)), CategoryDto.class);
	}

	@Autowired
	public void setCategoryRepository(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
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
