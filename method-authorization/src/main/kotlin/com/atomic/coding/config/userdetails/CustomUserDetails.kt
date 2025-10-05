package com.atomic.coding.config.userdetails

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    authorities: List<String>,
    private val username: String,
    private val password: String,
    val brands: Set<String>,
) : UserDetails {

    private val grantedAuthorities: List<GrantedAuthority> = authorities.map { auth -> SimpleGrantedAuthority(auth) }

    override fun getUsername(): String = username

    override fun getPassword(): String = password

    override fun getAuthorities(): List<GrantedAuthority> = grantedAuthorities

}

