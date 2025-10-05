package com.atomic.coding.domain

data class Statistics(
    val customerName: String,
    val count: Int,
    val itemType: ItemType,
)

val wayneStatistics = Statistics(
    customerName = "wayne",
    count = 10,
    itemType = ItemType.ELECTRONICS
)

val jamesStatistics = Statistics(
    customerName = "james",
    count = 10,
    itemType = ItemType.ACCESSORY
)

val statistics = mutableListOf(
    wayneStatistics,
    jamesStatistics
)