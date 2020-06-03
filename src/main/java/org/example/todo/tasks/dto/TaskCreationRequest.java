package org.example.todo.tasks.dto;

import lombok.Data;

@Data
public class TaskCreationRequest {
	private TaskDto task;
	private CategoryDto category;
}
