package com.atomic.coding.config.manager

import com.atomic.coding.config.provider.ApiKeyAuthenticationProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationManager(
    private val apiKeyAuthenticationProvider: ApiKeyAuthenticationProvider,
    private val daoAuthenticationProvider: DaoAuthenticationProvider,
) : AuthenticationManager {

    override fun authenticate(authentication: Authentication): Authentication = when {
        apiKeyAuthenticationProvider.supports(authentication::class.java) ->
            apiKeyAuthenticationProvider.authenticate(authentication)

        daoAuthenticationProvider.supports(authentication::class.java) ->
            daoAuthenticationProvider.authenticate(authentication)

        else -> throw BadCredentialsException("Unsupported authentication type: ${authentication.javaClass.simpleName}")
    }

}
