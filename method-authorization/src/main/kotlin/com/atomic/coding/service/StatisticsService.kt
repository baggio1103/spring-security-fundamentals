package com.atomic.coding.service

import com.atomic.coding.domain.ItemType
import com.atomic.coding.domain.Statistics
import com.atomic.coding.domain.statistics
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class StatisticsService {

    private val logger: Logger = LoggerFactory.getLogger(Statistics::class.java)

    fun statistics(itemType: ItemType = ItemType.ELECTRONICS): Statistics {
        logger.info("Searching statistics for item type: $itemType")
        return statistics.first { it.itemType == itemType }
    }

    fun updateStatistics(itemType: ItemType): Statistics {
        logger.info("Updating statistics for item type: $itemType")
        return statistics.first { it.itemType == itemType }
    }

}