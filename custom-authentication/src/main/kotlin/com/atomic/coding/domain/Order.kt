package com.atomic.coding.domain

val orders = listOf(
    Order(
        id = "ORD123",
        customerName = "Alice",
        items = listOf("Book", "Pen"),
        totalAmount = 29.99,
        status = "PENDING"
    )
)

data class Order(
    val id: String,
    val customerName: String,
    val items: List<String>,
    val totalAmount: Double,
    val status: String
)
