package com.rotomoo.api.config

import org.h2.server.web.JakartaWebServlet
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("local")
class H2ConsoleConfig {

    @Bean
    fun h2ServletRegistration(): ServletRegistrationBean<JakartaWebServlet> {
        return ServletRegistrationBean(JakartaWebServlet(), "/h2-console/*")
    }
}
