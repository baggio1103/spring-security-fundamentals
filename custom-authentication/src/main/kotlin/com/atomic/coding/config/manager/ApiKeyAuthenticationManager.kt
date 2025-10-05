package com.atomic.coding.config.manager

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class ApiKeyAuthenticationManager(
    private val authenticationProvider: AuthenticationProvider
) : AuthenticationManager {

    override fun authenticate(authentication: Authentication): Authentication {
        if (authenticationProvider.supports(authentication.javaClass)) {
            return authenticationProvider.authenticate(authentication)
        }
        throw AuthenticationServiceException("AuthenticationProvider not found")
    }

}