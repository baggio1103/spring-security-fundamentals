package com.atomic.coding.basics.service

import com.atomic.coding.basics.domain.Order
import com.atomic.coding.basics.domain.orders
import org.springframework.stereotype.Service

@Service
class OrderService {

    fun orders(): List<Order> = orders

}