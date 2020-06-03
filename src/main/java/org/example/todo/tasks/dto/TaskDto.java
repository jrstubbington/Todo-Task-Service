package org.example.todo.tasks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.todo.common.dto.DtoEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class TaskDto implements DtoEntity {

	private UUID uuid;

	private String name;

	private String description;

	private String status;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private OffsetDateTime createdDate;

//	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID createdByUserUuid;

	private UUID assignedToUserUuid;

	private UUID workspaceUuid;

	private int priority;

	private OffsetDateTime reminderDate;
}
