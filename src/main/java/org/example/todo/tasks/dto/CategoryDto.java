package org.example.todo.tasks.dto;

import lombok.Data;
import org.example.todo.common.dto.DtoEntity;

import java.util.UUID;

@Data
public class CategoryDto implements DtoEntity {

	private UUID uuid;
}
