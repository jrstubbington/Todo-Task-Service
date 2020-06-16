package org.example.todo.tasks.config;

import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.hateoas.mediatype.collectionjson.CollectionJsonLinkDiscoverer;
import org.springframework.plugin.core.SimplePluginRegistry;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.OperationsSorter;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
@Profile("!prod")
public class SpringFoxConfig {


	private final String packageName;

	private final String packageVersion;

	private final String projectName;

	private final String projectDescription;

	@Autowired
	public SpringFoxConfig(@Value("${project.package}") String packageName,
	                       @Value("${project.version}") String packageVersion,
	                       @Value("${project.name}") String projectName,
	                       @Value("${project.description}") String projectDescription){
		this.packageName = packageName;
		this.packageVersion = packageVersion;
		this.projectName = WordUtils.capitalizeFully(projectName.replace('-', ' '));
		this.projectDescription = projectDescription;
	}

	private ApiInfo getApiInfo() {
		return new ApiInfo(
				projectName,
				projectDescription,
				packageVersion,
				"TERMS OF SERVICE URL",
				new Contact("NAME","URL","EMAIL"),
				"LICENSE",
				"LICENSE URL",
				Collections.emptyList()
		);
	}

	@Bean
	public LinkDiscoverers discoverers() {
		List<LinkDiscoverer> plugins = new ArrayList<>();
		plugins.add(new CollectionJsonLinkDiscoverer());
		return new LinkDiscoverers(SimplePluginRegistry.create(plugins));
	}


	@Bean
	public UiConfiguration uiConfig() {
		return UiConfigurationBuilder
				.builder()
				.operationsSorter(OperationsSorter.METHOD)
				.docExpansion(DocExpansion.NONE)
				.build();
	}

	@Bean
	public Docket apiDocket() {
		return new Docket(DocumentationType.SWAGGER_2)
				.forCodeGeneration(false)
				.useDefaultResponseMessages(false)
				.select()
				.apis(RequestHandlerSelectors.basePackage(packageName))
				.paths(PathSelectors.any())
				.build()
				.apiInfo(getApiInfo());
	}
}
