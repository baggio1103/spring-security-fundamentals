package com.atomic.coding.controller

import com.atomic.coding.domain.Order
import com.atomic.coding.domain.orders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController {

    @GetMapping
    fun getOrders(): List<Order> = orders

}