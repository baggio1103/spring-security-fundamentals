package com.atomic.coding.config

import com.atomic.coding.config.filter.ApiKeyAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
class SecurityConfig(
    private val apiKeyAuthenticationFilter: ApiKeyAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
    ): SecurityFilterChain = http
        .addFilterBefore(apiKeyAuthenticationFilter, BasicAuthenticationFilter::class.java)
        .httpBasic {  }
        .authorizeHttpRequests { it.anyRequest().authenticated() }
        .build()

}