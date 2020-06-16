package org.example.todo.tasks;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.accounts.controller.UserManagementApi;
import org.example.todo.accounts.dto.ResponseContainerUserDto;
import org.example.todo.common.exceptions.ResourceNotFoundException;
import org.example.todo.common.kafka.KafkaOperation;
import org.example.todo.common.kafka.KafkaProducer;
import org.example.todo.common.util.ResponseUtils;
import org.example.todo.tasks.generated.dto.TaskDto;
import org.example.todo.tasks.model.Category;
import org.example.todo.tasks.model.Task;
import org.example.todo.tasks.repository.CategoryRepository;
import org.example.todo.tasks.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
@Profile("local")
public class Setup {


	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private KafkaProducer<TaskDto> kafkaProducer;

	@Autowired
	private UserManagementApi userManagementApi;


	public void functionTest() {
		try {
			ResponseContainerUserDto responseContainerUserDto = userManagementApi.getUserByUUID(UUID.randomUUID());
//			ResponseContainerUserDto responseContainerUserDto = userManagementApi.getUsersV1(0, 10);
			log.info("USERS: {}", responseContainerUserDto.getData());
		}
		catch (ResourceNotFoundException e) {
			log.error("{}", e.getMessage());
			log.trace("error", e);
		}
		catch (RestClientException e) {
			log.info("Failed to do the thing");
			log.trace("Error:", e);
		}

	}


	@Bean
	@Transactional
	public CommandLineRunner demo(){


		return args -> {
			try {
				UUID workspaceUuid = UUID.randomUUID();
				UUID userUuid = UUID.randomUUID();


				log.info("UserUUID {}", userUuid);
				log.info("WorkspaceUUID {}", workspaceUuid);

				Category category = Category.builder()
						.name("My Test Category")
						.description("This is a testing category for testing")
						.createdByUserUuid(userUuid)
						.workspaceUuid(workspaceUuid)
						.build();

				Task task = Task.builder()
						.name("My Test Task")
						.description("testing description")
						.assignedToUserUuid(userUuid)
						.createdByUserUuid(userUuid)
						.workspaceUuid(workspaceUuid)
						.reminderDate(OffsetDateTime.now().plusMinutes(5))
						.status("active")
						.build();

				Set<Task> newTasks = new HashSet<>();
				newTasks.add(task);

				category.setTasks(newTasks);
				task.setCategory(category);

				categoryRepository.save(category);
				taskRepository.save(task);

				kafkaProducer.sendMessage("tasks", KafkaOperation.CREATE,
						ResponseUtils.convertToDto(task, TaskDto.class));

			}
			catch (Exception e) {
				log.error("", e);
			}
			functionTest();
		};
	}


}
