package org.example.todo.tasks.repository;

import org.example.todo.tasks.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, Long> {

	Set<Task> findDistinctByAssignedToUserUuid(UUID uuid);
}
