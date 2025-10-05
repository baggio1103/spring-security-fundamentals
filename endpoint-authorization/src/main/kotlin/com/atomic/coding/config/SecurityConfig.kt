package com.atomic.coding.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
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
    fun securityFilterChain(
        http: HttpSecurity
    ): SecurityFilterChain = http
        .httpBasic { }
        .csrf { it.disable() }
        .authorizeHttpRequests { request ->
            request.requestMatchers("/health").permitAll()
            request.requestMatchers("/orders").hasAnyAuthority("READ", "WRITE")
            request.requestMatchers(HttpMethod.GET, "/items").hasAuthority("READ")
            request.requestMatchers(HttpMethod.POST, "/items").hasAuthority("WRITE")
            request.requestMatchers(HttpMethod.DELETE, "/items/**").hasAuthority("DELETE")
            request.requestMatchers("/statistics/**").hasAuthority("STATISTICS")
            request.anyRequest().authenticated()
        }
        .build()

    @Bean
    fun userDetailsService(): UserDetailsService {
        val wayne = User
            .withUsername("wayne")
            .password(passwordEncoder().encode("wayne123"))
            .authorities("READ", "STATISTICS")
            .build()

        val james = User
            .withUsername("james")
            .password(passwordEncoder().encode("james123"))
            .authorities("WRITE")
            .build()

        val bill = User
            .withUsername("bill")
            .password(passwordEncoder().encode("bill123"))
            .authorities("DELETE")
            .build()
        return InMemoryUserDetailsManager(wayne, james, bill)
    }


    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

}