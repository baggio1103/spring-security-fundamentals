package com.atomic.coding.domain

import com.atomic.coding.entity.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class SecurityUser(
    private val userEntity: UserEntity
) : UserDetails {

    override fun getAuthorities(): List<GrantedAuthority> =
        userEntity.authorities.map { authority ->
            SimpleGrantedAuthority(authority.name)
        }

    override fun getPassword(): String = userEntity.password

    override fun getUsername(): String = userEntity.username

}