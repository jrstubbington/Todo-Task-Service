package org.example.todo.tasks.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TaskCreationRequest {
	private TaskDto task;
	private UUID categoryUuid;
}
