package org.example.todo.tasks.repository;

import org.example.todo.tasks.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	Optional<Category> findByUuid(UUID uuid);
}
