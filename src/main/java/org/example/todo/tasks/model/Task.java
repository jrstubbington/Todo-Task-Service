package org.example.todo.tasks.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
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
	private Long id;

	@Type(type="uuid-binary")
	@NaturalId
	@Column(name = "UUID", nullable = false, updatable = false)
	@GeneratedValue(generator = "hibernate-uuid")
	@NotNull
	private final UUID uuid = UUID.randomUUID();

	@ManyToOne
	@JoinColumn(name="category_id", referencedColumnName = "uuid", nullable=false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Category category;

}
