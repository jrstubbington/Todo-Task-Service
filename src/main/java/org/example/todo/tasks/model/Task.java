package org.example.todo.tasks.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Task implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Getter(AccessLevel.NONE)
	private Long id;

	@Type(type="uuid-binary")
	@NaturalId
	@Column(name = "UUID", nullable = false, updatable = false)
	@GeneratedValue(generator = "hibernate-uuid")
	@NotNull
	private final UUID uuid = UUID.randomUUID();

	@ManyToOne
	@JoinColumn(name="category_uuid", referencedColumnName = "uuid", nullable=false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@NotNull
	private Category category;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy="task")
	private Set<WorkLogEntry> workLogs;

	@NotNull
	@NotBlank
	@Size(max = 50)
	private String name;

	@Size(max = 254)
	@NotNull
	private String description;

	@NotNull
	@Size(max = 10)
	private String status;

	private OffsetDateTime createdDate;

	@NotNull
	private UUID createdByUserUuid;

	private UUID assignedToUserUuid;

	@NotNull
	private UUID workspaceUuid;

	private int priority;

	private OffsetDateTime reminderDate;


}
