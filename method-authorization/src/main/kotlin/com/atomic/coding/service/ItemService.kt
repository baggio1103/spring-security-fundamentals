package com.atomic.coding.service

import com.atomic.coding.domain.Item
import com.atomic.coding.domain.ItemType
import com.atomic.coding.domain.items
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class ItemService {

    private val logger: Logger = LoggerFactory.getLogger(ItemService::class.java)

    fun listItems(): List<Item> = items

    fun addItem(itemName: String, company: String, type: ItemType) {
        items.add(Item(name = itemName, brand = company, type = type))
    }

    fun deleteItem(itemName: String) {
        items.removeIf { it.name == itemName }
    }

    fun listItemsByCompanyIn(companies: Set<String>): List<Item> {
        logger.info("Filtering items by company: $companies")
        return items.filter { it.brand in companies }
    }

}