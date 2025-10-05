package com.atomic.coding

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MultiAuthProvidersApplication

fun main(args: Array<String>) {
    runApplication<MultiAuthProvidersApplication>(*args)
}
