package org.example.todo.tasks;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.tasks.model.Category;
import org.example.todo.tasks.model.Task;
import org.example.todo.tasks.repository.CategoryRepository;
import org.example.todo.tasks.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
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
				Category category = new Category();

				Task task = new Task();

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
