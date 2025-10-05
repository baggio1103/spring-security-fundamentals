package com.atomic.coding.config.provider

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager

@Configuration
class DaoAuthenticationProviderConfig {

    @Bean
    fun inMemoryUserDetailsManager(): UserDetailsService {
        val user = User.builder()
            .username("alice")
            .password(passwordEncoder().encode("qwerty"))
            .build()
        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun daoAuthenticationProvider(userDetailsService: UserDetailsService): DaoAuthenticationProvider =
        DaoAuthenticationProvider(userDetailsService)

}
