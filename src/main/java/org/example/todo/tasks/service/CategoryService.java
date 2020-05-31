package org.example.todo.tasks.service;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.tasks.model.Category;
import org.example.todo.tasks.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CategoryService {

	private CategoryRepository categoryRepository;

	@Transactional
	public Category createCategory() {
		return null;
	}

	@Autowired
	public void setCategoryRepository(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}
}
