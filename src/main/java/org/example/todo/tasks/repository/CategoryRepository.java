package org.example.todo.tasks.repository;

import org.example.todo.tasks.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
