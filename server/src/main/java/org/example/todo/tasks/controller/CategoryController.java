package org.example.todo.tasks.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.example.todo.tasks.generated.controller.CategoryManagementApi;
import org.example.todo.tasks.generated.dto.CategoryDto;
import org.example.todo.tasks.generated.dto.ResponseContainerCategoryDto;
import org.example.todo.tasks.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@Api(tags = "Category Management")
@Validated
@Slf4j
@CrossOrigin
public class CategoryController implements CategoryManagementApi {

	private CategoryService categoryService;

	@Override
	public ResponseEntity<ResponseContainerCategoryDto> createCategory(@Valid CategoryDto categoryDto) {
		return ResponseEntity.ok(categoryService.createCategoryResponse(categoryDto));
	}

	@Override
	public ResponseEntity<ResponseContainerCategoryDto> getCategories(@Valid Integer page, @Valid Integer pageSize) {
		return ResponseEntity.ok(categoryService.getAllCategoriesResponse(PageRequest.of(page, pageSize)));
	}

	@Override
	public ResponseEntity<ResponseContainerCategoryDto> getCategoryByUUID(UUID uuid) {
		return ResponseEntity.ok(categoryService.findCategoryByUuidResponse(uuid));
	}

	/**
	 * GET /v1/categories/workspace/{uuid} : Get a list of categories belonging to a specific workspace
	 *
	 * @param uuid Workspace uuid to get categories list object with (required)
	 * @return OK (status code 200)
	 * or Client Error (status code 400)
	 * or Not Found (status code 404)
	 * or Internal error has occurred (status code 500)
	 */
	@Override
	public ResponseEntity<ResponseContainerCategoryDto> getCategoryByWorkspaceUuid(UUID uuid) {
		return ResponseEntity.ok(categoryService.findCategoriesByWorkspaceUuidResponse(uuid));
	}

	@Autowired
	public void setCategoryService(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
}
