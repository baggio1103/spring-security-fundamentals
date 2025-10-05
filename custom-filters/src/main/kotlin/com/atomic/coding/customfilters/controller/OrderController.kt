package com.atomic.coding.customfilters.controller

import com.atomic.coding.customfilters.Order
import com.atomic.coding.customfilters.orders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController {

    @GetMapping
    fun orders(): List<Order> = orders

}