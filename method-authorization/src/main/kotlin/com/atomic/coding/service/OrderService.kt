package com.atomic.coding.service

import com.atomic.coding.domain.Order
import com.atomic.coding.domain.orders
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OrderService {

    private val logger: Logger = LoggerFactory.getLogger(OrderService::class.java)

    fun listOrderByCustomerName(customerName: String): Order {
       return orders.first { it.customerName == customerName }
    }

    fun deleteOrderByCustomerName(customerName: String) {
        logger.info("Deleting order for customer: $customerName")
        orders.removeIf { it.customerName == customerName }
    }

}
