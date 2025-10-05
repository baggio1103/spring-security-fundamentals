package com.atomic.coding.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "authorities")
data class AuthorityEntity(

    @Id
    val id: Long,

    @Column
    val name: String,

    @ManyToMany(mappedBy = "authorities")
    val users: List<UserEntity>

)
