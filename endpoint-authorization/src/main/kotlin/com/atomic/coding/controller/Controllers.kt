package com.atomic.coding.controller

import com.atomic.coding.domain.Item
import com.atomic.coding.domain.Order
import com.atomic.coding.domain.Statistics
import com.atomic.coding.service.ItemService
import com.atomic.coding.service.OrderService
import com.atomic.coding.service.StatisticsService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/health")
class HealthController {

    @GetMapping
    fun healthCheck(): String = "OK, and Running!"

}

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
) {

    @GetMapping
    fun getOrders(): List<Order> = orderService.orders()

}

@RestController
@RequestMapping("/statistics")
class StatisticsController(
    private val statisticsService: StatisticsService
) {

    @GetMapping
    fun getStatistics(): Statistics = statisticsService.statistics()

    @PostMapping("/upload")
    fun uploadStatistics() = statisticsService.uploadStatistics()

}


@RestController
@RequestMapping("/items")
class ItemController(
    private val itemService: ItemService
) {

    @GetMapping
    fun getItems(): List<Item> = itemService.items()

    @PostMapping
    fun addItem(@RequestParam("name") name: String){
        itemService.addItem(name = name)
    }

    @DeleteMapping("/{id}")
    fun deleteItem(@PathVariable("id")id: Int) = itemService.deleteItem(id)

}
