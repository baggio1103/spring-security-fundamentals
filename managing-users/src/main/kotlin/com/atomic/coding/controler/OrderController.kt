package com.atomic.coding.controler

import com.atomic.coding.domain.Order
import com.atomic.coding.service.OrderService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {

    private val logger: Logger = LoggerFactory.getLogger(OrderController::class.java)

    @GetMapping
    fun orders(): List<Order> {
        val authentication = SecurityContextHolder.getContext().authentication
        logger.info("AuthenticatedUser: ${authentication.name}")
        authentication.authorities.forEach { grantedAuthority ->
            logger.info("GrantedAuthority: $grantedAuthority")
        }
        return orderService.orders()
    }

}
