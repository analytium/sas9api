package com.codexsoft.sas.config;


import com.codexsoft.sas.config.models.ProxyConfigModel;
import com.codexsoft.sas.secure.ApiKeyRequestInterceptor;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;
import java.util.Optional;

@Configuration
public class SpringConfig extends WebMvcConfigurerAdapter {
    private final ProxyConfigModel proxyConfigModel;

    public SpringConfig(ProxyConfigModel proxyConfigModel) {
        this.proxyConfigModel = proxyConfigModel;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApiKeyRequestInterceptor(proxyConfigModel));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    /*    A a = beanFactory.getBean(A.class);
        beanFactory.autowireBean(a);*/
        configurer.favorPathExtension(false).
                favorParameter(true).
                defaultContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        Optional<HttpMessageConverter<?>> converter = converters.stream()
                .filter(conv -> conv instanceof MappingJackson2XmlHttpMessageConverter)
                .findAny();
        if (converter.isPresent()) {
            MappingJackson2XmlHttpMessageConverter xmlConverter = (MappingJackson2XmlHttpMessageConverter) converter.get();
            XmlMapper xmlMapper = (XmlMapper) xmlConverter.getObjectMapper();
            xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
            xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
        }
    }
}
