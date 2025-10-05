package com.atomic.coding.customfilters

data class Order(
    val id: String,
    val customerName: String,
    val items: List<String>,
    val totalAmount: Double,
    val status: OrderStatus
)

enum class OrderStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    CANCELLED
}


val orders = listOf(
    Order(
        id = "ORD123",
        customerName = "Alice",
        items = listOf("Book", "Pen"),
        totalAmount = 29.99,
        status = OrderStatus.PENDING
    )
)
