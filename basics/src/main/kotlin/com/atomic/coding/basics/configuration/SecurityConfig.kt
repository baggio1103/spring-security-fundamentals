package com.atomic.coding.basics.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .httpBasic {}
            .authorizeHttpRequests { httpRequest ->
                httpRequest.anyRequest().authenticated()
            }
            .build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val inMemoryUser = User
            .withUsername("atomic.coding")
            .password("\$2a\$10\$kEQxusWgs1ncnA.f.IuedeZlvtCNSu4zVT3XovHFFmPWRcaYwrlzu")
            .authorities("read")
            .build()
        return InMemoryUserDetailsManager(inMemoryUser)
    }

}