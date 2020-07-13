package org.example.todo.tasks.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.example.todo.tasks.generated.controller.WorkLogManagementApi;
import org.example.todo.tasks.generated.dto.ResponseContainerWorkLogEntryDto;
import org.example.todo.tasks.generated.dto.WorkLogEntryDto;
import org.example.todo.tasks.service.WorkLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Api(tags = "WorkLog Management")
@Validated
@Slf4j
public class WorkLogController implements WorkLogManagementApi {

	private WorkLogService workLogService;

	/**
	 * POST /v1/worklogs/ : Create a new work log
	 *
	 * @param workLogEntryDto (required)
	 * @return OK (status code 200)
	 * or Client Error (status code 400)
	 * or Not Found (status code 404)
	 * or Internal error has occurred (status code 500)
	 */
	@Override
	public ResponseEntity<ResponseContainerWorkLogEntryDto> createWorkLog(@Valid WorkLogEntryDto workLogEntryDto) {
		return ResponseEntity.ok(workLogService.createWorkLogResponse(workLogEntryDto));
	}

	/**
	 * PUT /v1/worklogs/ : Update an existing work log
	 *
	 * @param workLogEntryDto (required)
	 * @return OK (status code 200)
	 * or Client Error (status code 400)
	 * or Not Found (status code 404)
	 * or Internal error has occurred (status code 500)
	 */
	@Override
	public ResponseEntity<ResponseContainerWorkLogEntryDto> updateWorkLog(@Valid WorkLogEntryDto workLogEntryDto) {
		return ResponseEntity.ok(workLogService.updateWorkLogEntryResponse(workLogEntryDto));
	}

	@Autowired
	public void setWorkLogService(WorkLogService workLogService) {
		this.workLogService = workLogService;
	}
}
