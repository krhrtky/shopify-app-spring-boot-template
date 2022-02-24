package com.example.shopifyappspringboottemplate.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http
        .csrf().disable()
        .cors().disable()
        .httpBasic().disable()
        .formLogin().disable()
        .addFilterAfter(HMACVerificationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
        .addFilterAfter(SessionStateFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
        .build()
}
