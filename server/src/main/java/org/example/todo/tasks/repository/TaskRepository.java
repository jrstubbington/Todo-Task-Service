package org.example.todo.tasks.repository;

import org.example.todo.tasks.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, Long> {

	Optional<Task> findByUuid(UUID uuid);

	Set<Task> findAllByAssignedToUserUuid(UUID uuid);
}
