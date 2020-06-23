package org.example.todo.tasks.service;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.common.exceptions.ImproperResourceSpecification;
import org.example.todo.common.exceptions.ResourceNotFoundException;
import org.example.todo.common.util.ResponseUtils;
import org.example.todo.tasks.generated.dto.ResponseContainerWorkLogEntryDto;
import org.example.todo.tasks.generated.dto.WorkLogEntryDto;
import org.example.todo.tasks.listener.UserListener;
import org.example.todo.tasks.listener.WorkspaceListener;
import org.example.todo.tasks.model.Task;
import org.example.todo.tasks.model.WorkLogEntry;
import org.example.todo.tasks.repository.WorkLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class WorkLogService {

	private WorkLogRepository workLogRepository;

	private TaskService taskService;

	private UserListener userListener;

	private WorkspaceListener workspaceListener;

	//TODO: Enable filtering and sorting
	public List<WorkLogEntry> getAllTasks() {
		return workLogRepository.findAll();
	}

	//TODO: Enable filtering and sorting
	public ResponseContainerWorkLogEntryDto getAllTasksResponse(PageRequest pageRequest) {
		return ResponseUtils.convertToDtoResponseContainer(workLogRepository.findAll(pageRequest), WorkLogEntry.class, ResponseContainerWorkLogEntryDto.class);
	}

	public WorkLogEntry findWorkLogByUUID(UUID uuid) {
		return workLogRepository.findByUuid(uuid).orElseThrow(() -> new ResourceNotFoundException(String.format("Work Log not found with id: %s", uuid)));
	}

	public WorkLogEntry createWorkLog(WorkLogEntryDto workLogEntryDto) {
		Task task = taskService.findTaskByUuid(workLogEntryDto.getTaskUuid());
		WorkLogEntry workLogEntry = WorkLogEntry.builder()
				.task(task)
				.userUuid(workLogEntryDto.getUserUuid())
				.workspaceUuid(workLogEntryDto.getWorkspaceUuid())
				.startedDate(workLogEntryDto.getStartedDate())
				.endedDate(workLogEntryDto.getEndedDate())
				.comment(workLogEntryDto.getComment())
				.build();

		return workLogRepository.save(workLogEntry);
	}

	//TODO: Can these response methods be automatically generated or potentially implemented via Spring AOP?
	public ResponseContainerWorkLogEntryDto createWorkLogResponse(WorkLogEntryDto workLogEntryDto) {
		return ResponseUtils.convertToDtoResponseContainer(createWorkLog(workLogEntryDto), WorkLogEntryDto.class, ResponseContainerWorkLogEntryDto.class);
	}

	@Transactional
	public WorkLogEntry updateWorkLogEntry(WorkLogEntryDto workLogEntryDto) {
		if (Objects.nonNull(workLogEntryDto.getUuid())) {
			// Task is being updated
			log.debug("Updating task {}", workLogEntryDto);
			WorkLogEntry workLog = findWorkLogByUUID(workLogEntryDto.getUuid());

			if (userListener.doesNotContain(workLogEntryDto.getUserUuid())) {
				throw new ResourceNotFoundException(String.format("User with id, %s could not be found to assign task to.", workLogEntryDto.getUserUuid()));
			}
			if (workspaceListener.doesNotContain(workLogEntryDto.getWorkspaceUuid())) {
				throw new ResourceNotFoundException(String.format("Workspace with id, %s could not be found to assign task to.", workLogEntryDto.getWorkspaceUuid()));
			}

			workLog.setStartedDate(workLogEntryDto.getStartedDate());
			workLog.setEndedDate(workLogEntryDto.getEndedDate());
			workLog.setComment(workLogEntryDto.getComment());

			WorkLogEntry savedWorkLogEntry = workLogRepository.saveAndFlush(workLog);

//			kafkaProducer.sendMessage(KAFKA_TOPIC, KafkaOperation.UPDATE,
//					ResponseUtils.convertToDto(savedTask, TaskDto.class));

			return savedWorkLogEntry;
		}
		else {
			throw new ImproperResourceSpecification("Must specify a UUID when updating a worklog");
		}
	}

	@Transactional
	public ResponseContainerWorkLogEntryDto updateWorkLogEntryResponse(WorkLogEntryDto workLogEntryDto) {
		return ResponseUtils.convertToDtoResponseContainer(updateWorkLogEntry(workLogEntryDto), WorkLogEntry.class, ResponseContainerWorkLogEntryDto.class);
	}

	@Transactional
	public List<WorkLogEntry> getWorkLogEntriesForTaskUuid(UUID uuid) {
		return workLogRepository.findAllByTaskUuid(uuid);
	}

	@Transactional
	public ResponseContainerWorkLogEntryDto getWorkLogEntriesForTaskUuidResponse(UUID uuid) {
		return ResponseUtils.convertToDtoResponseContainer(getWorkLogEntriesForTaskUuid(uuid), WorkLogEntry.class, ResponseContainerWorkLogEntryDto.class);
	}


	@Autowired
	public void setWorkLogRepository(WorkLogRepository workLogRepository) {
		this.workLogRepository = workLogRepository;
	}

	@Autowired
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	@Autowired
	public void setUserListener(UserListener userListener) {
		this.userListener = userListener;
	}

	@Autowired
	public void setWorkspaceListener(WorkspaceListener workspaceListener) {
		this.workspaceListener = workspaceListener;
	}
}
