package com.codexsoft.sas.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class SwaggerSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(SwaggerSecurityConfig.class);

    @Autowired SwaggerAuthProperties authProperties;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requiresChannel().anyRequest().requiresSecure();

        http.sessionManagement().disable();

        http.httpBasic().disable().formLogin().defaultSuccessUrl("/swagger-ui.html");

        log.debug("swagger auth is enabled: {}", authProperties.isEnabled());
        if(authProperties.isEnabled()){   
            http.authorizeRequests().antMatchers("/swagger-ui.html").authenticated()
            .anyRequest().permitAll();
        }else {            
            http.authorizeRequests().anyRequest().permitAll();
        }

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        if(authProperties.isEnabled()){
            log.debug("swagger auth users: {}", authProperties.getUsers());
            authProperties.getUsers().forEach((user, password) -> {
                log.debug("loading uesr/password: {}:{}", user, password);
                try {
                    auth.inMemoryAuthentication().withUser(user).password("{noop}"+password).roles("USER");
                } catch (Exception e) {
                  log.error("errer:"+ e.getMessage());
                }
            });
        }
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    @Bean
    public UserDetailsService userDetailsServiceBean() throws Exception {
        return super.userDetailsServiceBean();
    }
    
}
