package org.example.todo.tasks.repository;

import org.example.todo.tasks.model.WorkLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkLogRepository  extends JpaRepository<WorkLogEntry, Long> {

	Optional<WorkLogEntry> findByUuid(UUID uuid);

	List<WorkLogEntry> findAllByTaskUuid(UUID uuid);
}
