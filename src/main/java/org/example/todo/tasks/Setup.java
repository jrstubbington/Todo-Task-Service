package org.example.todo.tasks;

import lombok.extern.slf4j.Slf4j;
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

	@Bean
	@Transactional
	public CommandLineRunner demo() {
		return args -> {
			try {
				UUID workspaceUuid = UUID.randomUUID();
				UUID userUuid = UUID.randomUUID();

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
						.status("active")
						.build();

				Set<Task> newTasks = new HashSet<>();
				newTasks.add(task);

				category.setTasks(newTasks);
				task.setCategory(category);

				categoryRepository.save(category);
				taskRepository.save(task);

			}
			catch (Exception e) {
				log.error("", e);
			}
		};
	}
}