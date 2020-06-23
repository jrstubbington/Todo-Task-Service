package org.example.todo.tasks.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "work_log")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkLogEntry implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private Long id;

	@Type(type="uuid-binary")
	@NaturalId
	@Column(name = "UUID", unique = true, nullable = false, updatable = false)
	@GeneratedValue(generator = "hibernate-uuid")
	@NotNull
	private final UUID uuid = UUID.randomUUID();

	private UUID workspaceUuid;

	private UUID userUuid;

	@ManyToOne
	@JoinColumn(name="task_uuid", referencedColumnName = "uuid", nullable=false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Task task;

	private OffsetDateTime startedDate;

	private OffsetDateTime endedDate;

	private String comment;
}
