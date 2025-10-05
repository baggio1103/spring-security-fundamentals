package com.atomic.coding.basics.controller

import com.atomic.coding.basics.domain.Order
import com.atomic.coding.basics.service.OrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {

    @GetMapping
    fun orders(): List<Order> = orderService.orders()

}