package com.atomic.coding.config

import com.atomic.coding.config.userdetails.CustomUserDetails
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableMethodSecurity(
    securedEnabled = true
)
class SecurityConfig {

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain = httpSecurity
        .httpBasic { }
        .csrf { it.disable() }
        .authorizeHttpRequests { request ->
            request.requestMatchers("/health").permitAll()
            request.anyRequest().authenticated()
        }
        .build()

    @Bean
    fun userDetailsService(): UserDetailsService {
        val wayne = CustomUserDetails(
            authorities = listOf("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER"),
            username = "wayne",
            password = passwordEncoder().encode("wayne123"),
            brands = setOf("Apple", "Casio")
        )

        val bill = CustomUserDetails(
            authorities = listOf("READ", "WRITE"),
            username = "bill",
            password = passwordEncoder().encode("bill123"),
            brands = setOf("Uniqlo", "Apple", "Casio", "Sony")
        )

        val james = CustomUserDetails(
            authorities = listOf("READ", "WRITE", "DELETE"),
            username = "james",
            password = passwordEncoder().encode("james123"),
            brands = setOf("Uniqlo", "Apple", "Casio", "Sony", "Parker")
        )

        return UserDetailsService { username ->
            when (username) {
                "wayne" -> wayne
                "bill" -> bill
                "james" -> james
                else -> throw UsernameNotFoundException("User not found: $username")
            }
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

}