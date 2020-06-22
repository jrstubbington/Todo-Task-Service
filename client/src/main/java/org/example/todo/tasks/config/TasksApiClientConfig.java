package org.example.todo.tasks.config;

import org.example.todo.tasks.ApiClient;
import org.example.todo.tasks.generated.controller.TaskManagementApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TasksApiClientConfig {

	@Bean
	public TaskManagementApi taskManagementApi(RestTemplate restTemplate) {
		TaskManagementApi taskManagementApi = new TaskManagementApi();
		taskManagementApi.setApiClient(new ApiClient(restTemplate));
		return taskManagementApi;
	}
}
