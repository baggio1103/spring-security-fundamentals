package com.atomic.coding.customfilters.config

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class ApiKeyAuthentication(
    private val apiKey: String,
    private var authenticated: Boolean
): Authentication {


    override fun isAuthenticated(): Boolean =authenticated

    override fun setAuthenticated(isAuthenticated: Boolean)  {
        authenticated = isAuthenticated
    }

    override fun getName(): String = "API_KEY_USER"

    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList()

    override fun getCredentials(): Any = apiKey // or "N/A"

    override fun getDetails(): Any = "Authenticated with API key"

    override fun getPrincipal(): Any = apiKey // or a constant like "API_KEY"

}