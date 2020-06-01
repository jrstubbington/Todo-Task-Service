package org.example.todo.tasks.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "work_logs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkLogEntry implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private UUID workspaceUuid;

	@ManyToOne
	@JoinColumn(name="task_uuid", referencedColumnName = "uuid", nullable=false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Task task;
}
