package org.example.todo.tasks.config;

import lombok.extern.slf4j.Slf4j;
import org.example.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
public class ApiClientConfig {

	@Bean
	public ApiClient apiClient(RestTemplate restTemplate) {
		log.trace("Creating new ApiClient");
		return new ApiClient(restTemplate);
	}
}
