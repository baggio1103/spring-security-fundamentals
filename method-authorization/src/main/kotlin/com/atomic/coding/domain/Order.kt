package com.atomic.coding.domain

val orders = mutableListOf(
    Order(
        id = "ORD123",
        customerName = "james",
        items = listOf("Book", "Note"),
        totalAmount = 29.99,
        status = "PENDING"
    ),
    Order(
        id = "ORD456",
        customerName = "bill",
        items = listOf("Bag", "Pen"),
        totalAmount = 49.99,
        status = "PENDING"
    ),
    Order(
        id = "ORD678",
        customerName = "wayne",
        items = listOf("Pen"),
        totalAmount = 2.99,
        status = "PENDING"
    ),
)

data class Order(
    val id: String,
    val customerName: String,
    val items: List<String>,
    val totalAmount: Double,
    val status: String
)

data class Item(
    val name: String,
    val type: ItemType,
    val brand: String,
)

enum class ItemType {
    ELECTRONICS,
    STATIONERY,
    CLOTHING,
    ACCESSORY,
    TOY,
    BOOK
}

val items = mutableListOf(
    Item("iPhone", ItemType.ELECTRONICS, "Apple"),
    Item("MacBook", ItemType.ELECTRONICS, "Apple"),
    Item("Pen", ItemType.STATIONERY, "Parker"),
    Item("Notebook", ItemType.STATIONERY, "Moleskine"),
    Item("T-Shirt", ItemType.CLOTHING, "Uniqlo"),
    Item("Backpack", ItemType.ACCESSORY, "Herschel"),
    Item("Wristwatch", ItemType.ACCESSORY, "Casio"),
    Item("Toy Car", ItemType.TOY, "Hot Wheels"),
    Item("LEGO Set", ItemType.TOY, "LEGO"),
    Item("Novel", ItemType.BOOK, "Penguin Books"),
    Item("Sticker Pack", ItemType.STATIONERY, "Sticker Mule"),
    Item("Headphones", ItemType.ELECTRONICS, "Sony"),
    Item("Sweater", ItemType.CLOTHING, "H&M"),
    Item("Eraser", ItemType.STATIONERY, "Staedtler"),
    Item("Bookmark", ItemType.STATIONERY, "Bookish Co.")
)
