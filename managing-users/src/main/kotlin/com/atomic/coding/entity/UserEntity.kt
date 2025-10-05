package com.atomic.coding.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class UserEntity(

    @Id
    @Column(name = "id")
    val id: Long,

    @Column(name = "username")
    val username: String,

    @Column(name = "password")
    val password: String,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_authorities",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "authority_id")]
    )
    val authorities: List<AuthorityEntity>,
)
