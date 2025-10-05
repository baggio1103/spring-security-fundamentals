package com.atomic.coding.service

import com.atomic.coding.domain.Order
import org.springframework.stereotype.Service

@Service
class OrderService {

    private val orders = listOf(
        Order(
            id = "ORD123",
            customerName = "Alice",
            items = listOf("Book", "Pen"),
            totalAmount = 29.99,
            status = "PENDING"
        )
    )

    fun orders(): List<Order> = orders

}