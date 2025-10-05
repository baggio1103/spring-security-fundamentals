package com.atomic.coding

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ManagingUsersApplication {

//    @Bean
//    fun commandLineRunner(passwordEncoder: PasswordEncoder): CommandLineRunner= CommandLineRunner {
//        val (username, password) = "baggio" to "qwerty"
//        println("Credentials: $username and Password: ${passwordEncoder.encode(password)}")
//    }

}

fun main(args: Array<String>) {
    runApplication<ManagingUsersApplication>(*args)
}
