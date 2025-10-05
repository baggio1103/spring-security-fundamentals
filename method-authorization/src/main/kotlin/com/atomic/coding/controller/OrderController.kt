package com.atomic.coding.controller

import com.atomic.coding.domain.Order
import com.atomic.coding.service.OrderService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
) {

    @GetMapping("/{customerName}")
    @PreAuthorize("hasAuthority('READ') && authentication.name == #customerName")
    fun getOrdersByCustomerName(@PathVariable("customerName") customerName: String): Order {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication.name != customerName) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        return orderService.listOrderByCustomerName(customerName)
    }

    @DeleteMapping("/{customerName}")
    @PreAuthorize("@deleteAuthorityEvaluator.evaluate(#customerName)")
    fun deleteOrder(@PathVariable("customerName") customerName: String) {
        orderService.deleteOrderByCustomerName(customerName)
    }

}