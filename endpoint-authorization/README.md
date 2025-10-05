# 🛡️ Spring Security, Part VI – Exploring Authorization

Welcome back to the **Spring Security Series**!

If you haven't read the earlier tutorials, I recommend checking them out first — they lay the groundwork for what we'll
build in this post.

---

In the previous tutorials, we covered the **fundamentals** of how Spring Security works — such
as `Authentication`, `AuthenticationManager`, `UserDetailsService`, and more.
Now, it's time to take the next step: **configuring Authorization**.

In this tutorial, we will:

* ✅ Implement several endpoints requiring different types of authorities/roles
* ✅ Define several users with different types of authorities and roles

---

## ✅ What You Will Learn

* Authentication vs Authorization
* Roles & Authorities
* Configuring Authorization on the Endpoint Level

---

## 🔄 Authentication vs Authorization

**Authentication** is the process of validating a user request. It answers the question: *"Who is the user?"*

We achieve this by checking whether the user exists in the database (in the case of Basic Auth or Form Login), or by
validating a token (in the case of token-based authentication), etc.

As discussed in earlier tutorials, Authentication occurs in the `SecurityFilterChain`, which delegates
to `AuthenticationManager → AuthenticationProvider → UserDetailsService`, etc.

Once the request is authenticated, the `SecurityContextHolder` is populated with an object of type `Authentication`,
which holds all necessary information about the user — including authorities and roles.

From this point onward, **Authorization** rules can be applied to check if the user has access to specific endpoints.

> ⚠️ In short:
> **Authentication happens first, then Authorization is applied.**

---

### 🔐 Authorization in Spring Security

In Spring Security, we can apply authorization rules in two main ways:

* **Endpoint-Level Authorization**

  ```kotlin
  authorizeHttpRequests { request ->
      request.requestMatchers("/orders").hasRole("USER")
  }
  ```

* **Method-Level Authorization**

  ```kotlin
  @PreAuthorize("hasRole('USER')")
  @GetMapping
  fun getOrders(): List<Order> = orderService.orders()
  ```

In this tutorial, we will focus solely on **Endpoint-Level Authorization**.

---

## 🏗️ Project Setup

Here’s the `build.gradle.kts` you'll need:

```kotlin
plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.atomic.coding"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

---

## 📦 Simple Controller for Testing

```kotlin
@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @GetMapping
    fun getOrders(): List<Order> = orderService.orders()
}
```

---

## 🔐 UserDetailsService

For simplicity, we’ll store users using `InMemoryUserDetailsManager` instead of storing users in a database.

> Curious how to implement a database-based `UserDetailsService`?
> Feel free to check out previous tutorials in this series.

```kotlin
@Bean
fun userDetailsService(): UserDetailsService {
    val wayne = User
        .withUsername("wayne")
        .password(passwordEncoder().encode("wayne123"))
        .build()
    return InMemoryUserDetailsManager(wayne)
}
```

We’ve declared a single user: **wayne**.

---

## 🔐 Security Configuration

```kotlin
@Configuration
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .httpBasic { }
            .authorizeHttpRequests { request ->
                request.anyRequest().authenticated()
            }
            .build()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
```

This is a basic Spring Security configuration with Basic Authentication. The line:

```kotlin
request.anyRequest().authenticated()
```

...ensures that **all requests must be authenticated**.

---

## 🧪 Testing

### ❌ Providing Wrong Credentials

```bash
curl --location 'http://localhost:8080/orders' \
--header 'Authorization: Basic d2F5bmU6d2F5bmUxMjM='
```

📍 Output: **401 Unauthorized**

### ✅ Providing Correct Credentials

```bash
curl --location 'http://localhost:8080/orders' \
--header 'Authorization: Basic d2F5bmU6d2F5bmUxMjM='
```

📍 Output: **200 OK**

---

## 🔒 Declaring Authorization Rules

Let’s define our first authorization rule — only users with `READ` authority can access the `/orders` endpoint.

```kotlin
@Bean
fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
    http
        .httpBasic { }
        .authorizeHttpRequests { request ->
            request.requestMatchers("/orders").hasAuthority("READ")
            request.anyRequest().authenticated()
        }
        .build()
```

### 🧪 Try accessing the endpoint now:

```bash
curl --location 'http://localhost:8080/orders' \
--header 'Authorization: Basic d2F5bmU6d2F5bmUxMjM='
```

📍 Output: **403 Forbidden**

> ✅ This is expected!
> The user doesn't have the required `READ` authority.

---

## 🧑‍🤝‍🧑 Adding More Users

```kotlin
@Bean
fun userDetailsService(): UserDetailsService {
    val wayne = User
        .withUsername("wayne")
        .password(passwordEncoder().encode("wayne123"))
        .authorities("READ", "WRITE", "DELETE")
        .build()

    val james = User
        .withUsername("james")
        .password(passwordEncoder().encode("james123"))
        .authorities("READ")
        .build()

    return InMemoryUserDetailsManager(wayne, james)
}
```

### 🧪 Testing

#### Accessing with **wayne**:

```bash
curl --location 'http://localhost:8080/orders' \
--header 'Authorization: Basic d2F5bmU6d2F5bmUxMjM='
```

📍 Output: **200 OK**

#### Accessing with **james**:

```bash
curl --location 'http://localhost:8080/orders' \
--header 'Authorization: Basic amFtZXM6amFtZXMxMjM='
```

📍 Output: **200 OK**

---

## 🆚 Authorities vs Roles

What’s the difference?

Technically, **there is no difference** — both are backed by the `GrantedAuthority` interface.

But conventionally:

* **Authorities** represent actions like: `READ`, `WRITE`, `DELETE`
* **Roles** represent personas like: `USER`, `ADMIN`, `MANAGER`

### Examples:

```kotlin
val user1 = User
    .withUsername("wayne")
    .password(passwordEncoder().encode("wayne123"))
    .authorities("READ")
    .build() // SimpleGrantedAuthority("READ")

val user2 = User
    .withUsername("wayne")
    .password(passwordEncoder().encode("wayne123"))
    .roles("USER")
    .build() // SimpleGrantedAuthority("ROLE_USER")
```

> ⚠️ Tip: Stick to **one convention** (roles or authorities) to avoid confusion.

---

## 🔐 Using Roles in SecurityConfig

```kotlin
@Bean
fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
    http
        .httpBasic { }
        .authorizeHttpRequests { request ->
            request.requestMatchers("/orders").hasRole("USER")
        }
        .build()

@Bean
fun userDetailsService(): UserDetailsService {
    val wayne = User
        .withUsername("wayne")
        .password(passwordEncoder().encode("wayne123"))
        .roles("USER")
        .build()

    val james = User
        .withUsername("james")
        .password(passwordEncoder().encode("james123"))
        .roles("GUEST")
        .build()

    return InMemoryUserDetailsManager(wayne, james)
}
```

### 🧪 Testing

* **wayne** (ROLE_USER) → ✅ 200 OK
* **james** (ROLE_GUEST) → ❌ 403 Forbidden

---

## 🔁 Extended Setup & Testing

We’ll now define:

* Multiple users with different privileges
* Multiple endpoints
* Different authorization rules

### Controllers

```kotlin
@RestController
@RequestMapping("/health")
class HealthController {
    @GetMapping
    fun healthCheck(): String = "OK, and Running!"
}

@RestController
@RequestMapping("/orders")
class OrderController(private val orderService: OrderService) {
    @GetMapping
    fun getOrders(): List<Order> = orderService.orders()
}

@RestController
@RequestMapping("/statistics")
class StatisticsController(private val statisticsService: StatisticsService) {
    @GetMapping
    fun getStatistics(): Statistics = statisticsService.statistics()
}

@RestController
@RequestMapping("/items")
class ItemController(private val itemService: ItemService) {

    @GetMapping
    fun getItems(): List<Item> = itemService.items()

    @PostMapping
    fun addItem(@RequestParam("name") name: String) {
        itemService.addItem(name)
    }

    @DeleteMapping("/{id}")
    fun deleteItem(@PathVariable id: Int) = itemService.deleteItem(id)
}
```

---

## 👥 Users

```kotlin
@Bean
fun userDetailsService(): UserDetailsService {
    val wayne = User
        .withUsername("wayne")
        .password(passwordEncoder().encode("wayne123"))
        .authorities("READ", "STATISTICS")
        .build()

    val james = User
        .withUsername("james")
        .password(passwordEncoder().encode("james123"))
        .authorities("WRITE")
        .build()

    val bill = User
        .withUsername("bill")
        .password(passwordEncoder().encode("bill123"))
        .authorities("DELETE")
        .build()

    return InMemoryUserDetailsManager(wayne, james, bill)
}
```

---

## 🛡️ Security Rules

```kotlin
@Bean
fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
    http
        .httpBasic { }
        .csrf { it.disable() }
        .authorizeHttpRequests { request ->
            request.requestMatchers("/health").permitAll()
            request.requestMatchers("/orders").hasAnyAuthority("READ", "WRITE")
            request.requestMatchers(HttpMethod.GET, "/items").hasAuthority("READ")
            request.requestMatchers(HttpMethod.POST, "/items").hasAuthority("WRITE")
            request.requestMatchers(HttpMethod.DELETE, "/items/**").hasAuthority("DELETE")
            request.requestMatchers("/statistics").hasAuthority("STATISTICS")
            request.anyRequest().authenticated()
        }
        .build()
```

### 🧠 Breaking Down the Authorization Rules

Let’s look closely at what we’ve configured in the `securityFilterChain`:

* `requestMatchers("/health").permitAll()`
  → No authentication required; anyone can access this endpoint.

* `requestMatchers("/orders").hasAnyAuthority("READ", "WRITE")`
  → Users must have either `READ` or `WRITE` authority to access `/orders`.

* `requestMatchers(HttpMethod.GET, "/items").hasAuthority("READ")`
  → Only users with `READ` can perform GET requests on `/items`.

* `requestMatchers(HttpMethod.POST, "/items").hasAuthority("WRITE")`
  → POST (create) actions require `WRITE` authority.

* `requestMatchers(HttpMethod.DELETE, "/items/**").hasAuthority("DELETE")`
  → DELETE actions on any item (e.g. `/items/5`) require `DELETE` authority.

* `requestMatchers("/statistics").hasAuthority("STATISTICS")`
  → Only users with `STATISTICS` authority can access this endpoint.

> ✅ Tip: You can also group similar endpoints with a wildcard matcher. For example:

```kotlin
request.requestMatchers("/statistics/**").hasAuthority("STATISTICS")
```

This ensures that all routes under `/statistics/`, like:

* `/statistics`
* `/statistics/upload`
* `/statistics/generate`

…are protected and accessible only by users with the `STATISTICS` authority.

---

## 🎮 Try It Yourself!

We’ve defined 3 users with different sets of authorities. Use the credentials below with your favorite HTTP client (
like `curl`, Postman, or Insomnia) and observe how access is allowed or denied depending on the roles/authorities.

### 👤 Test Users

| Username | Password   | Base64 Encoding        | Roles/Authorities    |
|----------|------------|------------------------|----------------------|
| `wayne`  | `wayne123` | `d2F5bmU6d2F5bmUxMjM=` | `READ`, `STATISTICS` |
| `james`  | `james123` | `amFtZXM6amFtZXMxMjM=` | `WRITE`              |
| `bill`   | `bill123`  | `YmlsbDpiaWxsMTIz`     | `DELETE`             |

### 🚀 Suggested Actions

* Try accessing `/health` without any credentials → should work for everyone!
* Try different endpoints (`/orders`, `/items`, `/statistics`) using the users above.
* Use different HTTP methods (GET, POST, DELETE) and see who gets `200 OK` and who gets `403 Forbidden`.

### 🔧 Example Request (using `curl`):

```bash
curl http://localhost:8080/orders \
--header "Authorization: Basic d2F5bmU6d2F5bmUxMjM="
```

Feel free to experiment by changing the:

* **URL path** (`/orders`, `/items`, etc.)
* **HTTP method** (`GET`, `POST`, `DELETE`)
* **Credentials**

---

### 🚫 Understanding 401 vs 403 — Why Requests Fail

When working with secured endpoints, it's important to understand the **difference between these two common HTTP status codes**:

| Status             | Meaning                      | What It Tells You                                                                                                |
| ------------------ | ---------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| `401 Unauthorized` | 🔒 **Authentication failed** | The user is not logged in or provided invalid credentials. Spring Security could not identify *who* you are.     |
| `403 Forbidden`    | 🚫 **Authorization failed**  | You are authenticated, but not authorized to access the resource. You don't have the required role or authority. |

#### 🧠 Example Scenarios

* If you call an endpoint without a token or with the wrong password → `401 Unauthorized`
* If you call an endpoint with correct login, but lack the required role → `403 Forbidden`

> 💡 **Why this matters:** Understanding whether it’s an auth*n* issue (identity) or auth*z* issue (permission) will save you time debugging real-world problems — especially in production or CI pipelines.

---


## ✅ Recap – What We Covered

* ✅ How Spring Security handles **Authorization**
* ✅ Difference between **Authentication** and **Authorization**
* ✅ Roles vs Authorities
* ✅ Declaring Endpoint-Level Authorization Rules

---

## 👉 Coming Next

In the next part of this series, we’ll discuss:

* Method-Level Authorization (`@PreAuthorize`, etc.)
* CSRF Protection
* CORS Configuration
* And more advanced security topics

---

**Stay tuned!** If you’re enjoying the series, feel free to like, share, or leave a comment. See you in the next
tutorial!