package org.example.todo.tasks.config;

import lombok.extern.slf4j.Slf4j;
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
import springfox.documentation.builders.AuthorizationScopeBuilder;
import springfox.documentation.builders.LoginEndpointBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.GrantType;
import springfox.documentation.service.ImplicitGrant;
import springfox.documentation.service.LoginEndpoint;
import springfox.documentation.service.OAuth;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.OperationsSorter;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
@Slf4j
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
				.apiInfo(getApiInfo())
				.securitySchemes(buildSecurityScheme())
				.securityContexts(buildSecurityContext());
	}

	@Bean
	public SecurityConfiguration securityConfiguration() {

		//https://stackoverflow.com/questions/41918845/keycloak-integration-in-swagger
		Map<String, Object> additionalQueryStringParams=new HashMap<>();
		additionalQueryStringParams.put("nonce","123456");

		return SecurityConfigurationBuilder.builder()
				.clientId("swagger-ui").realm("TodoProject").appName(projectName) //TODO get client id and realm from properties files
				.additionalQueryStringParams(additionalQueryStringParams)
				.build();
	}

	private List<SecurityContext> buildSecurityContext() {
		List<SecurityReference> securityReferences = new ArrayList<>();

		securityReferences.add(SecurityReference.builder().reference("oauth2").scopes(scopes().toArray(new AuthorizationScope[]{})).build());

		SecurityContext context = SecurityContext.builder().forPaths(s -> true).securityReferences(securityReferences).build();

		List<SecurityContext> ret = new ArrayList<>();
		ret.add(context);
		return ret;
	}

	private List<? extends SecurityScheme> buildSecurityScheme() {
		List<SecurityScheme> lst = new ArrayList<>();
		// lst.add(new ApiKey("api_key", "X-API-KEY", "header"));

		LoginEndpoint login = new LoginEndpointBuilder().url("http://keycloak.trullingham.com/auth/realms/TodoProject/protocol/openid-connect/auth").build();

		List<GrantType> gTypes = new ArrayList<>();
		gTypes.add(new ImplicitGrant(login, "access_token"));
//		gTypes.add(new Grant));

		lst.add(new OAuth("oauth2", scopes(), gTypes));
		return lst;
	}

	private List<AuthorizationScope> scopes() {
		List<AuthorizationScope> scopes = new ArrayList<>();
		for (String scopeItem : new String[]{"openid=openid", "profile=profile"}) {
			String[] scope = scopeItem.split("=");
			if (scope.length == 2) {
				scopes.add(new AuthorizationScopeBuilder().scope(scope[0]).description(scope[1]).build());
			}
			else {
				log.warn("Scope '{}' is not valid (format is scope=description)", scopeItem);
			}
		}

		return scopes;
	}
}
