package org.example.todo.tasks.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "categories")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Type(type="uuid-binary")
	@NaturalId
	@Column(name = "UUID", nullable = false, updatable = false)
	@GeneratedValue(generator = "hibernate-uuid")
	@NotNull
	private final UUID uuid = UUID.randomUUID();

	@NotNull
	private UUID workspaceUuid;

	@NotBlank
	@NotNull
	@Size(max = 50)
	private String name;

	@NotNull
	@Size(max = 254)
	private String description;

	private UUID createdByUserUuid;

	@CreatedDate
	private OffsetDateTime createdDate;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy="category",
			cascade = CascadeType.ALL,
			orphanRemoval = true)
	private Set<Task> tasks;
}
