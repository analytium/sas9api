package com.codexsoft.sas;

import com.codexsoft.sas.config.SpringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;


@SpringBootApplication
@EnableSwagger2
@EnableWebMvc
@Import(SpringConfig.class)
public class SasProxyApplication {
	@Autowired
	private ApplicationContext context;

	@Value("${swagger.host}")
	private String swaggerHost;

	public static void main(String[] args) {
		SpringApplication.run(SasProxyApplication.class, args);
	}

//	to see Spring MVC endpoints in Swagger use: .apis(RequestHandlerSelectors.any())
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.protocols(Collections.singleton("http"))
				.host(swaggerHost)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.codexsoft.sas.controllers"))
				.paths(PathSelectors.any())
				.build()
				.produces(new HashSet<String>(Arrays.asList(new String[]{MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})))
		;
	}
}
