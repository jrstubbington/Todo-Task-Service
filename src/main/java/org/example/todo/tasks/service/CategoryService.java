package org.example.todo.tasks.service;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.common.dto.WorkspaceDto;
import org.example.todo.common.kafka.KafkaOperation;
import org.example.todo.common.util.ResponseContainer;
import org.example.todo.common.util.ResponseUtils;
import org.example.todo.common.util.Status;
import org.example.todo.tasks.dto.CategoryDto;
import org.example.todo.tasks.model.Category;
import org.example.todo.tasks.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@EnableKafka
public class CategoryService {

	private CategoryRepository categoryRepository;

	private Set<UUID> workspaceUuids = new HashSet<>();

	//TODO: Enable filtering and sorting
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	//TODO: Enable filtering and sorting
	public ResponseContainer<CategoryDto> getAllCategoriesResponse(PageRequest pageRequest) {
		return ResponseUtils.pageToDtoResponseContainer(categoryRepository.findAll(pageRequest), CategoryDto.class);
	}

	//Service cannot receive messages from itself, but this would be the basic config for setting up the listener
    @KafkaListener(topics = "workspaces")
    public void listen(WorkspaceDto workspaceDto, Acknowledgment acknowledgment,
                       @Header(value = "operation") KafkaOperation kafkaOperation
    ) {
        log.trace("Received Message: {}", workspaceDto);
        UUID workspaceUuid = workspaceDto.getUuid();
        switch (Objects.requireNonNull(kafkaOperation)) {
	        case CREATE:
	        	log.debug("Adding workspaceUuid {}", workspaceUuid);
	        	workspaceUuids.add(workspaceUuid);
	        	break;
	        case DELETE:
		        log.debug("Removing workspaceUuid {}", workspaceUuid);
	        	workspaceUuids.remove(workspaceUuid);
	        	break;
	        case UPDATE:
	        	if (workspaceDto.getStatus() != Status.ACTIVE) {
			        log.debug("Removing workspaceUuid {}", workspaceUuid);
			        workspaceUuids.remove(workspaceUuid);
		        }
	        	break;
	        default:
	        	break;

        }

        acknowledgment.acknowledge();
    }


	@Transactional
	public Category createCategory() {
		return null;
	}

	@Autowired
	public void setCategoryRepository(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}
}
