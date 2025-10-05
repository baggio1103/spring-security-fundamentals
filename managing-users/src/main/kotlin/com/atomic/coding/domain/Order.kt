package com.atomic.coding.domain

data class Order(
    val id: String,
    val customerName: String,
    val items: List<String>,
    val totalAmount: Double,
    val status: String
)
