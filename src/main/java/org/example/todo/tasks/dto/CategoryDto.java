package org.example.todo.tasks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
//import org.example.todo.common.dto.DtoEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class CategoryDto /*implements DtoEntity*/ {

	private UUID uuid;

	private UUID workspaceUuid;

	private String name;

	private String description;

	private UUID createdByUserUuid;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private OffsetDateTime createdDate;
}
