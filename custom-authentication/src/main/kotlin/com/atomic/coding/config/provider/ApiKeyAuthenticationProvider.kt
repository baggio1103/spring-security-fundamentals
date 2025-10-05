package com.atomic.coding.config.provider

import com.atomic.coding.config.authentication.ApiKeyAuthentication
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class ApiKeyAuthenticationProvider(
    @Value("\${authentication.api.secret-key}")
    private val apiKey: String,
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val apiKeyAuthentication = authentication as ApiKeyAuthentication
        if (apiKeyAuthentication.apiKey != apiKey) {
            throw BadCredentialsException("Api key is incorrect!")
        }
        return ApiKeyAuthentication(apiKey, true)
    }

    override fun supports(authentication: Class<*>): Boolean =
        authentication == ApiKeyAuthentication::class.java

}