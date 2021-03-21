package com.codexsoft.sas;

import com.codexsoft.sas.config.SpringConfig;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
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

	@Bean
	public ServletWebServerFactory servletContainer(@Value("${server.port.http}") int httpPort,
													@Value("${server.port}") int httpsPort) {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
			@Override
			protected void postProcessContext(Context context) {
				SecurityConstraint securityConstraint = new SecurityConstraint();
				securityConstraint.setUserConstraint("CONFIDENTIAL");
				SecurityCollection collection = new SecurityCollection();
				collection.addPattern("/*");
				securityConstraint.addCollection(collection);
				context.addConstraint(securityConstraint);
			}
		};
		tomcat.addAdditionalTomcatConnectors(redirectConnector(httpPort, httpsPort));
		return tomcat;
	}

	private Connector redirectConnector(int httpPort, int httpsPort) {
		Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
		connector.setScheme("http");
		connector.setPort(httpPort);
		connector.setSecure(true);
		connector.setRedirectPort(httpsPort);
		return connector;
	}
}
