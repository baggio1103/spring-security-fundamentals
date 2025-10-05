package com.atomic.coding.controller

import com.atomic.coding.config.userdetails.CustomUserDetails
import com.atomic.coding.domain.Item
import com.atomic.coding.domain.ItemRequest
import com.atomic.coding.service.ItemService
import jakarta.annotation.security.RolesAllowed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.access.prepost.PreFilter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/items")
class ItemController(
    private val itemService: ItemService
) {

    private val logger: Logger = LoggerFactory.getLogger(ItemController::class.java)

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    fun items(): List<Item> {
        return itemService.listItems()
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE')")
    fun addItem(@RequestBody itemRequest: ItemRequest) {
        itemService.addItem(itemRequest.name, itemRequest.brand, itemRequest.type)
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('DELETE')")
    fun deleteItem(itemName: String) {
        itemService.deleteItem(itemName)
    }

    @GetMapping("/pre-filter")
    @PreFilter("authentication.principal.brands.contains(filterObject)")
    fun preFilterItems(@RequestBody companies: Set<String>): List<Item> {
        val customUserDetails = SecurityContextHolder.getContext().authentication.principal as CustomUserDetails
        logger.info("Authenticated user has only relation to companies: ${customUserDetails.brands}")
        return itemService.listItemsByCompanyIn(companies)
    }

    @GetMapping("/post-filter")
    @PostFilter("authentication.principal.brands.contains(filterObject.brand)")
    fun postFilterItems(): List<Item> {
        val items = itemService.listItems()
        items.forEach { item -> logger.info("Items retrieved: $item") }
        return items
    }

}