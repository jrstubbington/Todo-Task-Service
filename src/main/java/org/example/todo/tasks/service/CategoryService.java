package org.example.todo.tasks.service;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.common.exceptions.ImproperResourceSpecification;
import org.example.todo.common.exceptions.ResourceNotFoundException;
import org.example.todo.common.kafka.KafkaOperation;
import org.example.todo.common.kafka.KafkaProducer;
import org.example.todo.common.util.ResponseContainer;
import org.example.todo.common.util.ResponseUtils;
import org.example.todo.tasks.dto.CategoryDto;
import org.example.todo.tasks.listener.WorkspaceListener;
import org.example.todo.tasks.model.Category;
import org.example.todo.tasks.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@EnableKafka
public class CategoryService {

	private CategoryRepository categoryRepository;

	private WorkspaceListener workspaceListener;

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



	@Transactional
	public Category createCategory(CategoryDto categoryDto) throws ResourceNotFoundException, ImproperResourceSpecification {
		UUID workspaceUuid = categoryDto.getWorkspaceUuid();
		if(Objects.nonNull(categoryDto.getUuid())) {
			throw new ImproperResourceSpecification("Cannot specify UUID when creating new category");
		}
		if (Objects.nonNull(workspaceUuid) && workspaceListener.contains(workspaceUuid)) { //TODO: perform API call to verify if workspace actually doesn't exist if contains fails
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
		else {

			throw new ResourceNotFoundException(String.format("Could not find a workspace with UUID: %s", workspaceUuid));
		}
	}

	@Autowired
	public void setCategoryRepository(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@Autowired
	public void setWorkspaceListener(WorkspaceListener workspaceListener) {
		this.workspaceListener = workspaceListener;
	}
}
