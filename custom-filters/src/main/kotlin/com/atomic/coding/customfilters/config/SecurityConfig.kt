package com.atomic.coding.customfilters.config

import com.atomic.coding.customfilters.config.filter.ApiKeyFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
class SecurityConfig(
    @Value("\${spring.security.key}")
    private val apiKey: String
) {

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .addFilterBefore(ApiKeyFilter(apiKey), BasicAuthenticationFilter::class.java)
            .httpBasic { }
            .authorizeHttpRequests { httpRequest -> httpRequest.anyRequest().authenticated() }
            .build()
    }

}