package com.atomic.coding.config.authorization

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class DeleteAuthorityEvaluator {

    private val logger: Logger = LoggerFactory.getLogger(DeleteAuthorityEvaluator::class.java)

    fun evaluate(customerName: String): Boolean {
        logger.info("Applying Authorization rule for customer $customerName")
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication.name == customerName && authentication.hasAuthority("DELETE")
    }

}

fun Authentication.hasAuthority(authority: String): Boolean {
    return this.authorities.map { it.authority }.contains(authority)
}