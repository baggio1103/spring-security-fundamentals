package com.atomic.coding.controller

import com.atomic.coding.domain.ItemType
import com.atomic.coding.domain.Statistics
import com.atomic.coding.service.StatisticsService
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/statistics")
class StatisticsController(
    private val statisticsService: StatisticsService
) {

    @GetMapping("/{itemType}")
    @PostAuthorize("authentication.name == returnObject.customerName")
    fun statistics(@PathVariable("itemType") itemType: ItemType): Statistics {
        return statisticsService.statistics(itemType)
    }

    @PutMapping("/{itemType}")
    @PostAuthorize("authentication.name == returnObject.customerName")
    fun updateStatistics(@PathVariable("itemType") itemType: ItemType): Statistics {
        return statisticsService.updateStatistics(itemType)
    }

}