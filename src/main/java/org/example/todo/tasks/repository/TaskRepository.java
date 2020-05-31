package org.example.todo.tasks.repository;

import org.example.todo.tasks.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
