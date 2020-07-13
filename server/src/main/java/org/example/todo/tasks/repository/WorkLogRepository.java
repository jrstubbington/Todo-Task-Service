package org.example.todo.tasks.repository;

import org.example.todo.tasks.model.WorkLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkLogRepository  extends JpaRepository<WorkLogEntry, Long> {

	Optional<WorkLogEntry> findByUuid(UUID uuid);

	List<WorkLogEntry> findAllByTaskUuid(UUID uuid);
}
