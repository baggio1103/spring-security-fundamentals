package com.atomic.coding.config.authentication

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class ApiKeyAuthentication(
    val apiKey: String,
    private val authenticated: Boolean
) : Authentication {

    override fun getName(): String = apiKey

    override fun getAuthorities(): List<GrantedAuthority> = emptyList()

    override fun getCredentials(): Any? = null

    override fun getDetails(): Any? = null

    override fun getPrincipal(): Any? = null

    override fun isAuthenticated(): Boolean = authenticated

    override fun setAuthenticated(isAuthenticated: Boolean) {}
}