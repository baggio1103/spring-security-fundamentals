# 🔐 Spring Security, Part VII — Exploring Method-Level Authorization

Have you ever wondered how annotations like `@PreAuthorize`, `@PostAuthorize`, `@PreFilter`, and `@PostFilter` actually
work?

If so, this tutorial will clear things up for you.

Welcome back to the **Spring Security Series**!
If you haven’t gone through the earlier parts of this series, I highly recommend checking them out first—they lay the
foundation for what we’ll build in this tutorial.

In the **previous tutorial**, we focused on applying authorization rules at the **endpoint/filter level**. In this one,
we’ll take a different approach:
We’ll implement the **same authorization logic**, but from a **method-level perspective**.

This shift in approach opens up a lot of flexibility and is a powerful way to enforce security deeper in your
application stack.

---

## ✅ What We’ll Cover

* Building and securing a Spring Boot application
* Implementing endpoints with varying access requirements based on roles and authorities
* Creating `CustomUserDetails` and defining multiple users
* Applying method-level authorization using:

  * `@PreAuthorize`
  * `@PostAuthorize`
  * `@PreFilter`
  * `@PostFilter`
* Understanding the **differences between endpoint-level and method-level authorization**

…and much more.

---

## 🤔 Why Method-Level Authorization?

You might ask:
**How can we apply authorization rules inside the controller, service, or even repository layers?**

The key lies in the `SecurityContextHolder`.
Once authentication succeeds, the `Authentication` object is stored in the security context, making it accessible *
*anywhere** in your application.

This allows you to apply fine-grained authorization rules not only at the endpoint level, but also in:

* The **Controller layer**
* The **Service layer**
* Even the **Repository layer**

While it’s common to place authorization rules in controllers, method-level annotations give you **more control**,
especially when securing **reusable business logic** in services.

---

## Endpoint-Level Authorization vs Method-Level Authorization

What’s the difference between **endpoint-level authorization** and **method-level authorization**?

Both serve the same purpose—restricting access to resources—but architecturally they differ.

* **Endpoint-level authorization** rules are applied at the **Filter level** as part of the **Security Filter Chain**.
* **Method-level authorization** works differently—it’s implemented via **Aspects**, which means authorization rules can
  be applied **anywhere** in your code.

This gives you much greater flexibility in controlling when and how authorization is enforced.

Let’s dive in and explore how Spring Security empowers us to secure applications at a more granular level.

---

## Building the Project

```kotlin
plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.atomic.coding"
version = "0.0.1-SNAPSHOT"
description = "method-authorization"

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

## Security Configuration

```kotlin
@Configuration
class SecurityConfig {

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain = httpSecurity
        .httpBasic { }
        .csrf { it.disable() }
        .authorizeHttpRequests { request ->
            request.requestMatchers("/health").permitAll()
            request.anyRequest().authenticated()
        }
        .build()

    @Bean
    fun userDetailsService(): UserDetailsService {
        val wayne = User
            .withUsername("wayne")
            .password(passwordEncoder().encode("wayne123"))
            .build()
        return InMemoryUserDetailsManager(wayne)
    }
}
```

This security configuration is simple:

* The endpoint `/health` is publicly available.
* All other endpoints require authentication.

---

## Sample Controller

```kotlin
@RestController
@RequestMapping("/items")
class ItemController(
    private val itemService: ItemService
) {

    private val logger: Logger = LoggerFactory.getLogger(ItemController::class.java)

    @GetMapping
    fun items(): List<Item> {
        return itemService.listItems()
    }
}
```

---

## Testing

Accessing with an existing user (`wayne:wayne123`):

```bash
curl --location 'http://localhost:8080/items' \
--header 'Authorization: Basic d2F5bmU6d2F5bmUxMjM='
```

`Response: 200 OK`

Accessing with a non-existing user (`user:qwerty`):

```bash
curl --location 'http://localhost:8080/items' \
--header 'Authorization: Basic dXNlcjpxd2VydHk='
```

`Response: 401 UNAUTHORIZED`

Accessing without credentials:

```bash
curl --location 'http://localhost:8080/items'
```

`Response: 401 UNAUTHORIZED`

---

## 🔐 Authorizing Endpoints

As we saw in the previous tutorial, we can apply authorization rules at the **endpoint/filter level**.
Let’s restrict `/items` so it’s accessible only to users with the `READ` authority.

At the endpoint level, this would look like:

```kotlin
request.requestMatcher("/items").hasAuthority("READ")
```

Now, let’s define the same rule at the **method level**:

```kotlin
@GetMapping
@PreAuthorize("hasAuthority('READ')")
fun items(): List<Item> {
    return itemService.listItems()
}
```

Both approaches—`@PreAuthorize("hasAuthority('READ')")` and `request.requestMatcher("/items").hasAuthority("READ")`
— achieve the same result:
Only users with the `READ` authority can access `/items`.

---

## 🔧 Enabling Method-Level Authorization

To enable method-level authorization, we need to annotate our configuration class with `@EnableMethodSecurity`.

```kotlin
@Configuration
// Enables @PreAuthorize() and @PostAuthorize()
@EnableMethodSecurity(prePostEnabled = true) // true by default
class SecurityConfig {
    // the rest remains unchanged
}
```

---

## 🧪 Testing

Accessing with user `wayne:wayne123`:

```bash
curl --location 'http://localhost:8080/items' \
--header 'Authorization: Basic d2F5bmU6d2F5bmUxMjM='
```

`Response: 403 Forbidden`
Because Wayne doesn’t have any authorities yet.

---

## 👥 Adding Users with Authorities

Let’s add multiple users with different authorities:

```kotlin
@Bean
fun userDetailsService(): UserDetailsService {
    val wayne = User
        .withUsername("wayne")
        .password(passwordEncoder().encode("wayne123"))
        .authorities("READ")
        .build()

    val bill = User
        .withUsername("bill")
        .password(passwordEncoder().encode("bill123"))
        .authorities("READ", "WRITE")
        .build()

    val james = User
        .withUsername("james")
        .password(passwordEncoder().encode("james123"))
        .authorities("READ", "WRITE", "DELETE")
        .build()
    return InMemoryUserDetailsManager(wayne, bill, james)
}
```

---

## 🧪 Testing Again

Accessing with Wayne (`wayne:wayne123`):

```bash
curl --location 'http://localhost:8080/items' \
--header 'Authorization: Basic d2F5bmU6d2F5bmUxMjM='
```

`Response: 200 OK`

---

## 📌 Types of Method-Level Authorization Rules

Spring Security supports several annotations for method-level authorization:

* `@PreAuthorize`
* `@PostAuthorize`
* `@PreFilter`
* `@PostFilter`
* `@Secured`
* `@RolesAllowed`

Let’s explore each in detail.

---

## 🔎 @PreAuthorize

`@PreAuthorize` is the most common way to restrict access at the method level.

It accepts a SpEL (Spring Expression Language) condition and evaluates it **before method execution**.
If the condition is met, the method executes. Otherwise, a `403 FORBIDDEN` response is returned.

Example:

```kotlin
@RestController
@RequestMapping("/items")
class ItemController(
    private val itemService: ItemService
) {

    @GetMapping
    @PreAuthorize("hasAuthority('READ')")
    fun items(): List<Item> {
        return itemService.listItems()
    }
}
```

The real power of `@PreAuthorize` is that you can use **method arguments** in the authorization rules.

For example, consider `/orders/{customerName}`.
We want to allow access if:

* `customerName` matches the authenticated user’s name
* The user has the `READ` authority.

```kotlin
@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
) {

    @GetMapping("/{customerName}")
    @PreAuthorize("hasAuthority('READ') && authentication.name == #customerName")
    fun getOrdersByCustomerName(@PathVariable("customerName") customerName: String): Order {
        return orderService.listOrderByCustomerName(customerName)
    }
}
```

👉 Inside SpEL, the `authentication` object is accessible and represents the current authenticated user.
To reference method arguments, use `#argumentName`.

---

### Alternative Without @PreAuthorize

Without method-level security, you’d need to implement the check manually:

```kotlin
@GetMapping("/{customerName}")
fun getOrdersByCustomerName(@PathVariable("customerName") customerName: String): Order {
    val authentication = SecurityContextHolder.getContext().authentication
    if (authentication.name != customerName) {
        throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }
    return orderService.listOrderByCustomerName(customerName)
}
```

As you can see, `@PreAuthorize` makes the code cleaner and keeps business logic decoupled from authorization rules.

---

## 🧪 Testing

Accessing `/orders/james` with user `james:james123`:

```bash
curl --location 'http://localhost:8080/orders/james' \
--header 'Authorization: Basic amFtZXM6amFtZXMxMjM='
```

`Response: 200 OK`

Accessing `/orders/james` with user `wayne:wayne123`:

```bash
curl --location 'http://localhost:8080/orders/james' \
--header 'Authorization: Basic d2F5bmU6d2F5bmUxMjM='
```

`Response: 403 FORBIDDEN`
Because Wayne’s username doesn’t match `james`.

---

## 🗑 Extending with Delete Authorization

Now let’s extend our `OrderController` to include a method for deleting orders.

**Scenario**:
A user should only be able to delete their own orders.

The rule is:

* The user must have the `DELETE` authority.
* The `{customerName}` path variable must match the authenticated user’s name.

```kotlin
@DeleteMapping("/{customerName}")
@PreAuthorize("hasAuthority('DELETE') && authentication.name == #customerName")
fun deleteOrder(@PathVariable("customerName") customerName: String) {
    orderService.deleteOrderByCustomerName(customerName)
}
```

---

## 🛠 Extracting Authorization Logic into a Bean

Sometimes, authorization rules get more complex. Instead of writing long expressions directly in `@PreAuthorize`, it’s
cleaner to extract them into a reusable component.

Here’s an example `DeleteAuthorityEvaluator` that encapsulates the logic for checking delete permissions:

```kotlin
@Component
class DeleteAuthorityEvaluator {

    private val logger: Logger = LoggerFactory.getLogger(DeleteAuthorityEvaluator::class.java)

    fun evaluate(customerName: String): Boolean {
        logger.info("Applying Authorization rule for customer $customerName")
        val authentication = SecurityContextHolder.getContext().authentication
        val authorities = authentication.authorities.map { it.authority }
        return authentication.name == customerName && authorities.contains("DELETE")
    }
  
}
```

Now we can reuse this logic in our controller:

```kotlin
@DeleteMapping("/{customerName}")
@PreAuthorize("@deleteAuthorityEvaluator.evaluate(#customerName)")
fun deleteOrder(@PathVariable("customerName") customerName: String) {
    orderService.deleteOrderByCustomerName(customerName)
}
```

Here’s how it works:

* Inside `@PreAuthorize`, you can access **any Spring bean** using SpEL (`@beanName.methodName(...)`).
* Request parameters are available via `#parameterName`.

---

### ✅ Benefits of Extracting Authorization Logic

* Easier to **debug** and **test** authorization rules
* Supports **complex conditions**
* Keeps controllers **cleaner** and focused on business logic

---

## 📋 Securing CRUD Endpoints

Let’s apply different authorities to different operations in `ItemController`:

* `@GetMapping` → requires `READ`
* `@PostMapping` → requires `WRITE`
* `@DeleteMapping` → requires `DELETE`

```kotlin
@RestController
@RequestMapping("/items")
class ItemController(
    private val itemService: ItemService
) {

    private val logger: Logger = LoggerFactory.getLogger(ItemController::class.java)

    @GetMapping
    @PreAuthorize("hasAuthority('READ')")
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
}
```

You can now experiment with different users (`wayne`, `bill`, `james`) and verify which endpoints they can and cannot
access.

---

## 🔎 @PostAuthorize

`@PostAuthorize` is another annotation for method-level authorization—but it works differently.

Instead of running before method execution, it applies **after the method runs**, checking whether the returned object
should be accessible to the user.

This makes `@PostAuthorize` useful when you want to restrict **access to the response**, not the execution itself.

Example: Suppose we have a `StatisticsController` that returns statistics for a given `itemType`. We want to ensure that
the statistics are only visible to the authenticated customer.

We can use the special SpEL variable `returnObject` to access the returned value.

```kotlin
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
}

data class Statistics(
    val customerName: String,
    val count: Int,
    val itemType: ItemType,
)
```

Here, the response will only be returned if `statistics.customerName` matches the authenticated user’s name.

---

### ⚠️ Important Caveat

`@PostAuthorize` is **not recommended** for methods that modify data (e.g., `save()`, `update()`, `delete()`).
Why? Because the method is already executed **before** the authorization check, meaning data may already have been
changed even if access is later denied.

Example (❌ Don’t do this):

```kotlin
@PutMapping("/{itemType}")
@PostAuthorize("authentication.name == returnObject.customerName")
fun updateStatistics(@PathVariable("itemType") itemType: ItemType): Statistics {
    log.info("Updating statistics for item: $itemType")
    return statisticsService.updateStatistics(itemType)
}
```

---

## 🧪 Testing @PostAuthorize

Try fetching statistics:

* With the correct user: `200 OK`
* With the wrong user: `403 FORBIDDEN`

Notice in the logs: the method **still executed**, but access to the response was denied.

This illustrates that `@PostAuthorize` doesn’t prevent execution—it only controls visibility of the result.

---

## 🔎 @PreFilter

`@PreFilter` is a specialized Spring Security annotation used to filter **method arguments** when they are collections (
e.g., `List`, `Set`, arrays).

It ensures that only the items a user is authorized to process remain in the input collection. This is useful when:

* A request contains mixed data, but the user should only act on a subset.
* You want to enforce **item-level security** before executing business logic.

---

### Example Scenario

Imagine each user has access to certain brands. Items belong to brands too—for instance:

* A T-shirt belongs to **Uniqlo**
* An iPhone belongs to **Apple**

Suppose we build an API that accepts a list of brand names and returns items for those brands.
Users should only get items for the brands they are authorized to access—even if they request more.

---

### Extending `UserDetails` with Brands

We’ll extend `UserDetails` to include a `brands` property:

```kotlin
class CustomUserDetails(
    authorities: List<String>,
    private val username: String,
    private val password: String,
    val brands: Set<String>,
) : UserDetails {

    private val grantedAuthorities: List<GrantedAuthority> =
        authorities.map { auth -> SimpleGrantedAuthority(auth) }

    override fun getUsername(): String = username
    override fun getPassword(): String = password
    override fun getAuthorities(): List<GrantedAuthority> = grantedAuthorities
}
```

---

### In-Memory Users with Brand Access

We’ll create a custom `UserDetailsService` that returns `CustomUserDetails`:

```kotlin
@Bean
fun userDetailsService(): UserDetailsService {
    val wayne = CustomUserDetails(
        authorities = listOf("READ"),
        username = "wayne",
        password = passwordEncoder().encode("wayne123"),
        brands = setOf("Apple", "Casio")
    )

    val bill = CustomUserDetails(
        authorities = listOf("READ", "WRITE"),
        username = "bill",
        password = passwordEncoder().encode("bill123"),
        brands = setOf("Uniqlo", "Apple", "Casio", "Sony")
    )

    val james = CustomUserDetails(
        authorities = listOf("READ", "WRITE", "DELETE"),
        username = "james",
        password = passwordEncoder().encode("james123"),
        brands = setOf("Uniqlo", "Apple", "Casio", "Sony", "Parker")
    )

    return UserDetailsService { username ->
        when (username) {
            "wayne" -> wayne
            "bill" -> bill
            "james" -> james
            else -> throw UsernameNotFoundException("User not found: $username")
        }
    }
}

@Bean
fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
```

---

### Using `@PreFilter`

Now we can use `@PreFilter` to filter the input collection before it reaches the controller.

* We reference each input element using the special variable `filterObject`.
* We check if the authenticated user’s brands contain the input brand.

```kotlin
@PostMapping("/pre-filter")
@PreFilter("authentication.principal.brands.contains(filterObject)")
fun preFilterItems(@RequestBody companies: Set<String>): List<Item> {
    return itemService.listItemsByCompanyIn(companies)
}
```

👉 Example:
If Wayne has access to `[Apple, Casio]` but requests `[Casio, Lego, Parker, Apple, Sony]`,
only `[Casio, Apple]` will be passed to the controller.

---

## 🔎 @PostFilter

`@PostFilter` works similarly, but applies **after method execution** to filter the **response collection**.

It’s useful when you want to ensure users only see allowed items, even if the method fetches a broader set.

* You reference each returned element using `filterObject`.
* Any elements not satisfying the condition are removed from the response.

Example: Filter `Item`s so users only get items belonging to brands they have access to:

```kotlin
data class Item(
    val name: String,
    val type: ItemType,
    val brand: String,
)

@GetMapping("/post-filter")
@PostFilter("authentication.principal.brands.contains(filterObject.brand)")
fun postFilterItems(): List<Item> {
    return itemService.listItems()
}
```

Here, only items whose `brand` is in `authentication.principal.brands` will be included in the response.

---

## 🧪 Testing `@PreFilter`

Endpoint:

```kotlin
@PostMapping("/pre-filter")
@PreFilter("authentication.principal.brands.contains(filterObject)")
fun preFilterItems(@RequestBody companies: Set<String>): List<Item> {
    return itemService.listItemsByCompanyIn(companies)
}
```

### ✅ Case 1: Wayne (`wayne:wayne123`)

Wayne has access to `Apple` and `Casio`.

```bash
curl --location 'http://localhost:8080/items/pre-filter' \
--header 'Authorization: Basic d2F5bmU6d2F5bmUxMjM=' \
--header 'Content-Type: application/json' \
--data '["Casio", "Lego", "Parker", "Apple", "Sony"]'
```

**Response:**

```json
[
  { "name": "iPhone", "brand": "Apple", "type": "ELECTRONICS" },
  { "name": "G-Shock", "brand": "Casio", "type": "WATCH" }
]
```

👉 Only `Apple` and `Casio` remain. The others (`Lego`, `Parker`, `Sony`) were **filtered out** before reaching the service.

---

### ✅ Case 2: Bill (`bill:bill123`)

Bill has access to `Uniqlo`, `Apple`, `Casio`, `Sony`.

```bash
curl --location 'http://localhost:8080/items/pre-filter' \
--header 'Authorization: Basic YmlsbDpiaWxsMTIz' \
--header 'Content-Type: application/json' \
--data '["Casio", "Lego", "Parker", "Apple", "Sony"]'
```

**Response:**

```json
[
  { "name": "iPhone", "brand": "Apple", "type": "ELECTRONICS" },
  { "name": "G-Shock", "brand": "Casio", "type": "WATCH" },
  { "name": "Bravia", "brand": "Sony", "type": "TV" }
]
```

---

### ✅ Case 3: James (`james:james123`)

James has access to all 5 brands.

```bash
curl --location 'http://localhost:8080/items/pre-filter' \
--header 'Authorization: Basic amFtZXM6amFtZXMxMjM=' \
--header 'Content-Type: application/json' \
--data '["Casio", "Lego", "Parker", "Apple", "Sony"]'
```

**Response:**
All items are returned because James has access to all brands.

---

## 🧪 Testing `@PostFilter`

Endpoint:

```kotlin
@GetMapping("/post-filter")
@PostFilter("authentication.principal.brands.contains(filterObject.brand)")
fun postFilterItems(): List<Item> {
    return itemService.listItems()
}
```

Suppose our inventory contains items from brands `[Apple, Casio, Sony, Uniqlo, Parker, Lego]`.

### ✅ Case 1: Wayne (`wayne:wayne123`)

```bash
curl --location 'http://localhost:8080/items/post-filter' \
--header 'Authorization: Basic d2F5bmU6d2F5bmUxMjM='
```

**Response:**

```json
[
  { "name": "iPhone", "brand": "Apple", "type": "ELECTRONICS" },
  { "name": "G-Shock", "brand": "Casio", "type": "WATCH" }
]
```

👉 Wayne only sees items from **Apple** and **Casio**.

---

### ✅ Case 2: Bill (`bill:bill123`)

```bash
curl --location 'http://localhost:8080/items/post-filter' \
--header 'Authorization: Basic YmlsbDpiaWxsMTIz'
```

**Response:**

```json
[
  { "name": "iPhone", "brand": "Apple", "type": "ELECTRONICS" },
  { "name": "G-Shock", "brand": "Casio", "type": "WATCH" },
  { "name": "Bravia", "brand": "Sony", "type": "TV" },
  { "name": "U-Crew", "brand": "Uniqlo", "type": "CLOTHING" }
]
```

---

### ✅ Case 3: James (`james:james123`)

```bash
curl --location 'http://localhost:8080/items/post-filter' \
--header 'Authorization: Basic amFtZXM6amFtZXMxMjM='
```

**Response:**
James gets **all items**, since he has access to every brand.

---

💡 Key Difference Recap:

* `@PreFilter`: filters the **input collection** before method execution.
* `@PostFilter`: filters the **output collection** after method execution.

---

## 🔐 Applying Filters in Services Too

Remember: `@PreAuthorize` and `@PreFilter` (like `@PostAuthorize`) aren’t limited to controllers.
You can apply them in services or repositories as well.

For example, let’s restrict deletion of items in `ItemService`:

```kotlin
class ItemService {

    @PreAuthorize("hasAuthority('DELETE')")
    fun deleteItem(itemName: String) {
        items.removeIf { it.name == itemName }
    }
}
```

This way, only users with the `DELETE` authority can delete items, regardless of how or where the method is called.

---

## 🔐 `@Secured`

`@Secured` is an older annotation used for method-level authorization.

* Unlike `@PreAuthorize`, it does **not** use SpEL (Spring Expression Language).
* You can only specify roles/authorities directly.
* Role names must be prefixed with `"ROLE_"`.

---

### Example

```kotlin
@RestController
@RequestMapping("/items/secured/")
class ItemController(
    private val itemService: ItemService
) {

    @GetMapping
    @Secured("ROLE_ADMIN")
    fun getAllItems(): List<Item> {
        return itemService.listItems()
    }
}
```

Here, only users with `ROLE_ADMIN` can access `/items`.

---

### ⚙️ Enable `@Secured`

To use it, enable `securedEnabled` in your security configuration:

```kotlin
@Configuration
@EnableMethodSecurity(securedEnabled = true)
class SecurityConfig
```

---

### 🧪 Testing `@Secured`

Suppose we update `james` to have `ROLE_ADMIN`:

```kotlin
val james = User
    .withUsername("james")
    .password(passwordEncoder().encode("james123"))
    .roles("ADMIN") // automatically adds ROLE_ prefix
    .build()
```

#### ✅ Case 1: James (Admin)

```bash
curl --location 'http://localhost:8080/items' \
--header 'Authorization: Basic amFtZXM6amFtZXMxMjM='
```

**Response:**

```json
[
  {
    "name": "iPhone",
    "brand": "Apple",
    "type": "ELECTRONICS"
  },
  {
    "name": "G-Shock",
    "brand": "Casio",
    "type": "WATCH"
  },
  ...
]
```

#### ❌ Case 2: Wayne (Non-Admin)

```bash
curl --location 'http://localhost:8080/secured' \
--header 'Authorization: Basic d2F5bmU6d2F5bmUxMjM='
```

**Response:**

```json
{
  "error": "Forbidden",
  "status": 403
}
```

---

## 🔐 `@RolesAllowed`

`@RolesAllowed` comes from **JSR-250** (Java EE standard annotations).
It’s very similar to `@Secured`, but more portable.

* Like `@Secured`, it does **not** support SpEL.
* You can specify multiple roles in an array.
* Unlike @Secured, it does not necessarily require the "ROLE_" prefix — both "USER" and "ROLE_USER" are accepted.

---

### Example

```kotlin
@RestController
@RequestMapping("/orders/roles-allowed")
class OrderController(
    private val orderService: OrderService
) {

    @GetMapping("/{customerName}")
    @RolesAllowed("ROLE_USER", "ROLE_ADMIN")
    fun getOrders(@PathVariable customerName: String): Order {
        return orderService.listOrderByCustomerName(customerName)
    }
}
```

Here, access is granted if the user has **either** `ROLE_USER` or `ROLE_ADMIN`.

---

### ⚙️ Enable `@RolesAllowed`

To use it, enable `jsr250Enabled`:

```kotlin
@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
class SecurityConfig
```

---

### 🧪 Testing `@RolesAllowed`

Suppose we set up users:

```kotlin
val wayne = User
    .withUsername("wayne")
    .password(passwordEncoder().encode("wayne123"))
    .roles("USER") // gives ROLE_USER
    .build()

val james = User
    .withUsername("james")
    .password(passwordEncoder().encode("james123"))
    .roles("ADMIN") // gives ROLE_ADMIN
    .build()
```

#### ✅ Case 1: Wayne (User)

```bash
curl --location 'http://localhost:8080/orders/roles-allowed/wayne' \
--header 'Authorization: Basic d2F5bmU6d2F5bmUxMjM='
```

**Response:**

```json
{
  "id": 1,
  "customerName": "wayne",
  "items": [
    ...
  ]
}
```

#### ✅ Case 2: James (Admin)

```bash
curl --location 'http://localhost:8080/roles-allowed/james' \
--header 'Authorization: Basic amFtZXM6amFtZXMxMjM='
```

**Response:**

```json
{
  "id": 2,
  "customerName": "james",
  "items": [
    ...
  ]
}
```

#### ❌ Case 3: Bill (No Role)

```bash
curl --location 'http://localhost:8080/roles-allowed/bill' \
--header 'Authorization: Basic YmlsbDpiaWxsMTIz'
```

**Response:**

```json
{
  "error": "Forbidden",
  "status": 403
}
```

---

## 🔎 Comparing `@Secured`, `@RolesAllowed`, and `@PreAuthorize`

| Annotation      | SpEL Support | Multiple Roles | Standardization | Preferred?    |
|-----------------|--------------|----------------|-----------------|---------------|
| `@Secured`      | ❌ No         | ✅ Yes          | Spring-only     | Legacy use    |
| `@RolesAllowed` | ❌ No         | ✅ Yes          | JSR-250         | Rarely used   |
| `@PreAuthorize` | ✅ Yes        | ✅ Yes          | Spring          | ✅ Recommended |

👉 `@PreAuthorize` is the **most flexible and recommended** approach, since it supports complex rules via SpEL.
`@Secured` and `@RolesAllowed` are simpler but limited.

---

✅ That wraps up **all method-level authorization annotations**!

---

# 📝 Recap — Method-Level Authorization in Spring Security

In this tutorial, we took a deep dive into **method-level authorization** in Spring Security.
Here’s what we covered step by step:

* ✅ How method-level authorization differs from endpoint-level authorization (Aspects vs Filters).
* ✅ Using `@PreAuthorize` for **pre-execution checks** with SpEL (most common and flexible).
* ✅ Using `@PostAuthorize` for **post-execution checks** (rarely used, mostly for filtering returned objects).
* ✅ Using `@PreFilter` and `@PostFilter` to filter **input collections** and **output collections**.
* ✅ Applying authorization rules at different layers — Controller, Service, Repository.
* ✅ Leveraging **custom beans** for reusable authorization logic (`DeleteAuthorityEvaluator`).
* ✅ Using legacy annotations like `@Secured` and JSR-250’s `@RolesAllowed`.
* ✅ Comparing all annotations side by side.

---

## 📊 Comparison Table — Method-Level Authorization Annotations

| Annotation       | When It Runs      | Supports SpEL? | Typical Use Case                                                                 | Notes                                                   |
|------------------|-------------------|----------------|----------------------------------------------------------------------------------|---------------------------------------------------------|
| `@PreAuthorize`  | **Before method** | ✅ Yes          | Most common; restrict access based on roles, authorities, or request parameters. | ✅ Recommended in modern Spring apps.                    |
| `@PostAuthorize` | **After method**  | ✅ Yes          | Restrict access to the returned object (`returnObject`).                         | ⚠️ Avoid for methods that modify data.                  |
| `@PreFilter`     | **Before method** | ✅ Yes          | Filter input collections (e.g., remove unauthorized items before processing).    | Works with `filterObject`.                              |
| `@PostFilter`    | **After method**  | ✅ Yes          | Filter returned collections (e.g., return only data user can access).            | Works with `filterObject`.                              |
| `@Secured`       | **Before method** | ❌ No           | Simple role-based access (`ROLE_ADMIN`, `ROLE_USER`).                            | Spring-only; no SpEL support.                           |
| `@RolesAllowed`  | **Before method** | ❌ No           | Simple role-based access with JSR-250 standard.                                  | Requires `@EnableMethodSecurity(jsr250Enabled = true)`. |

---

## 🎯 Key Takeaways

* Method-level security gives **fine-grained control** beyond endpoint-level authorization.
* Use `@PreAuthorize` as the **go-to choice** — it’s expressive, flexible, and supports SpEL.
* Use `@PreFilter` / `@PostFilter` when working with collections to enforce item-level security.
* Avoid `@PostAuthorize` for write operations — since the check happens **after execution**, the data may already be
  modified.
* `@Secured` and `@RolesAllowed` are simpler alternatives, but less flexible than `@PreAuthorize`.

---

This concludes our **first series on Spring Security** 🎉
Here’s what we’ve explored so far:

* Spring Security architecture and filter chain
* Authentication: Basic Auth, API Key, Custom Authentication Providers
* UserDetails, UserDetailsService, and InMemory vs DB-based users
* Multiple authentication providers
* Endpoint-level and method-level authorization

👉 **Next Up**: We’ll dive into **OAuth2 Authentication**, where we’ll cover how to integrate with identity providers,
secure APIs, and work with JWTs.

Stay tuned! If you enjoyed this series, feel free to **like, share, and leave a comment** 🙌
