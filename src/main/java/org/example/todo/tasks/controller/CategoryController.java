package org.example.todo.tasks.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.todo.common.dto.CategoryDto;
import org.example.todo.common.exceptions.ErrorDetails;
import org.example.todo.common.exceptions.ImproperResourceSpecification;
import org.example.todo.common.exceptions.ResourceNotFoundException;
import org.example.todo.common.util.ResponseContainer;
import org.example.todo.common.util.verification.group.Create;
import org.example.todo.tasks.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/categories")
@Tag(name = "Category Management", description = "A collection of APIs designed to handle functions related to category management")
@Validated
@Slf4j
public class CategoryController {

	private CategoryService categoryService;

	@Operation(summary = "View a list of available categories")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "500", description = "Internal error has occurred", content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
	})
	@GetMapping(value = "/", produces={"application/json"})
	public ResponseEntity<ResponseContainer<CategoryDto>> getCategories(
			@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
		return ResponseEntity.ok(categoryService.getAllCategoriesResponse(PageRequest.of(page, pageSize)));
	}

	@Operation(summary = "Get a specific task's information")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode  = "500", description = "Internal error has occurred", content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
	})
	@GetMapping(value = "/{uuid}", produces={"application/json"})
	public ResponseEntity<ResponseContainer<CategoryDto>> getCategoryByUUID(
			@Parameter(description ="Category uuid to get task object with") @PathVariable UUID uuid) throws ResourceNotFoundException {
		return ResponseEntity.ok(categoryService.findCategoryByUuidResponse(uuid));
	}

/*	@Operation(summary = "Update an existing task")
	@ApiResponses(value = {
			@ApiResponse(responseCode  = "200", description = "OK"),
			@ApiResponse(responseCode  = "400", description = "Client Error", content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode  = "500", description = "Internal error has occurred", content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
	})
	@PutMapping(value = "/", produces={"application/json"})
	public ResponseEntity<ResponseContainer<CategoryDto>> updateCategory(
			@Validated(Update.class) @RequestBody CategoryDto categoryDtoDto) throws ResourceNotFoundException, ImproperResourceSpecification {
		return ResponseEntity.ok(categoryService.updateCategoryResponse(categoryDto));
	}*/

	@Operation(summary = "Create a new task")
	@ApiResponses(value = {
			@ApiResponse(responseCode  = "200", description = "OK"),
			@ApiResponse(responseCode  = "400", description = "Client Error", content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode  = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
	})
	@PostMapping(value = "/", produces={"application/json"})
	public ResponseEntity<ResponseContainer<CategoryDto>> createCategory(
			@Validated(Create.class) @RequestBody CategoryDto categoryDto) throws ImproperResourceSpecification, ResourceNotFoundException {
		return ResponseEntity.ok(categoryService.createCategoryResponse(categoryDto));
	}

	@Autowired
	public void setCategoryService(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
}
