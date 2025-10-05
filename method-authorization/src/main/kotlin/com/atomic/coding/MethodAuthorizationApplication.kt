package com.atomic.coding

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MethodAuthorizationApplication

fun main(args: Array<String>) {
    runApplication<MethodAuthorizationApplication>(*args)
}
