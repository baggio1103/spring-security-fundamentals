package com.atomic.coding.domain

import java.time.LocalDate
import kotlin.random.Random

val orders = mutableListOf(
    Order(
        id = "ORD123",
        items = listOf("Book", "Pen"),
        totalAmount = 29.99,
        status = "PENDING"
    )
)

data class Order(
    val id: String,
    val items: List<String>,
    val totalAmount: Double,
    val status: String
)


val items = mutableListOf(
    Item(
        id = 1,
        name = "Macbook Air M1"
    ),
    Item(
        id = 2,
        name = "Macbook Pro M1"
    ),
    Item(
        id = 3,
        name = "Macbook Air M3"
    ),
    Item(
        id = 4,
        name = "Iphone 16 Pro"
    ),
)

data class Item(
    val id: Int,
    val name: String
)

fun Item(name: String): Item = Item(
    id = Random.nextInt(0, 100),
    name
)

val statistics = Statistics(
    orderId = "ORD_123",
    count = 10,
    date = LocalDate.now()
)

data class Statistics(
    val orderId: String,
    val count: Int,
    val date: LocalDate,
)