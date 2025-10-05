package com.atomic.coding.service

import com.atomic.coding.domain.*
import org.springframework.stereotype.Service

@Service
class OrderService {

    fun orders(): List<Order> = orders

}

@Service
class ItemService {

    fun items(): List<Item> = items

    fun deleteItem(id: Int) {
        items.removeIf { it.id == id }
    }

    fun addItem(name: String) {
        items.add(Item(name))
    }

}

@Service
class StatisticsService {

    fun statistics(): Statistics = statistics

    fun uploadStatistics() {
        println("Upload Statistics")
    }

}