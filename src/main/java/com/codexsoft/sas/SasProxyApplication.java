package com.codexsoft.sas;

import com.codexsoft.sas.config.SpringConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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
				.produces(new HashSet<>(Arrays.asList(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE)))
		;
	}

	//Server configuration for https endpoints
//	@Bean
//	public EmbeddedServletContainerFactory servletContainer() {
//		TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory() {
//			@Override
//			protected void postProcessContext(Context context) {
//				SecurityConstraint securityConstraint = new SecurityConstraint();
//				securityConstraint.setUserConstraint("CONFIDENTIAL");
//				SecurityCollection collection = new SecurityCollection();
//				collection.addPattern("/*");
//				securityConstraint.addCollection(collection);
//				context.addConstraint(securityConstraint);
//			}
//		};
//		tomcat.addAdditionalTomcatConnectors(redirectConnector());
//		return tomcat;
//	}
//
//	@Value("${server.port.http}") //Defined in application.properties file
//	int httpPort;
//	@Value("${server.port}") //Defined in application.properties file
//	int httpsPort;
//
//	private Connector redirectConnector() {
//		Connector connector = new Connector(TomcatEmbeddedServletContainerFactory.DEFAULT_PROTOCOL);
//		connector.setScheme("http");
//		connector.setPort(httpPort);
//		connector.setSecure(true);
//		connector.setRedirectPort(httpsPort);
//		return connector;
//	}
}
