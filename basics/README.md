# Spring Security, Part II: In-Memory Authentication with `UserDetailsService`

Welcome back to the **Spring Security Series**!
If you haven't read the [first tutorial](#), I recommend checking that out before continuing — it sets the foundation for what we're about to explore.

In this second post, we'll dive into:

* Pre-configured security setup in Spring Boot
* `UserDetails` and `UserDetailsService`
* Creating an in-memory user with `InMemoryUserDetailsManager`

Spring Security is **highly extensible** and provides default authentication mechanisms out of the box. Simply adding the security starter dependency gets you a secure setup with zero configuration.

Let’s walk through it with a basic Kotlin Spring Boot project.

---

## 🛠 Project Setup

**`build.gradle.kts`**

```kotlin
plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.0"
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
```

---

## 🧾 A Simple REST Controller

We’ll create a basic endpoint to return a list of orders:

```kotlin
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {
    @GetMapping
    fun orders(): List<Order> = orderService.orders()
}
```

---

## 🔐 Pre-Configured Security

Let’s run the app and send a request to `/api/orders`. You’ll receive:

📛 **401 Unauthorized**

That’s expected. Even without any custom configuration, **Spring Security enforces basic authentication by default.**

In the console, Spring logs:

```
Using generated security password: 98f6dd92-c74a-4b42-abc2-61a40a3bb6a3
```

✅ Try using:

* **Username:** `user`
* **Password:** the generated value above

Now the request will succeed, returning a 200 OK and the order list.

---

## 🧱 Under the Hood: Spring Security Components

Spring Security relies on two key interfaces for user authentication:

### `UserDetailsService`

This interface defines the **contract** for loading user-specific data. You implement `loadUserByUsername()` to return a `UserDetails` object:

```java
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
```

The user data can come from memory, a database, or even an external API — Spring doesn’t care, as long as it gets a `UserDetails` object in return.

### `UserDetails`

This interface represents the **security context** of the user:

```java
public interface UserDetails extends Serializable {
    Collection<? extends GrantedAuthority> getAuthorities();
    String getPassword();
    String getUsername();
    boolean isAccountNonExpired();
    boolean isAccountNonLocked();
    boolean isCredentialsNonExpired();
    boolean isEnabled();
}
```

---

## 🔑 Password Handling with `PasswordEncoder`

Storing plain-text passwords is a massive security risk. Spring Security provides the `PasswordEncoder` interface:

```java
public interface PasswordEncoder {
    String encode(CharSequence rawPassword);
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
```

Most common implementations:

* `BCryptPasswordEncoder` ✅
* `Argon2PasswordEncoder` 🧠

We’ll use **BCrypt** here.

---

## 🧪 Setting Up an In-Memory User

```kotlin
@Bean
fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

@Bean
fun userDetailsService(): UserDetailsService {
    val user = User.withUsername("atomic.coding")
        .password("\$2a\$10\$kEQxusWgs1ncnA.f.IuedeZlvtCNSu4zVT3XovHFFmPWRcaYwrlzu") // encoded "qwerty"
        .authorities("read")
        .build()

    return InMemoryUserDetailsManager(user)
}
```

---

## 🔐 Configuring Security

```kotlin
@Configuration
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .httpBasic {}  // Use Basic Auth
            .authorizeHttpRequests {
                it.anyRequest().authenticated()
            }
            .build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val user = User.withUsername("atomic.coding")
            .password("\$2a\$10\$kEQxusWgs1ncnA.f.IuedeZlvtCNSu4zVT3XovHFFmPWRcaYwrlzu")
            .authorities("read")
            .build()
        return InMemoryUserDetailsManager(user)
    }
}
```

When `UserDetailsService` is explicitly provided, Spring **does not generate a default user**. You can confirm this — there will be no random password printed to the console.

---

## 🧪 Test Results

* ✅ Correct credentials: `200 OK`
* ❌ Incorrect credentials: `401 Unauthorized`

Postman or curl with the right `Authorization: Basic` header should work seamlessly.

---

## ✅ What We Learned

* How Spring Security applies **pre-configured basic authentication**
* The role of `UserDetails`, `UserDetailsService`, and `PasswordEncoder`
* How to configure **in-memory users** for development/testing

---

## 🔜 What’s Next?

In the next tutorial, we’ll:

* Store users in a PostgreSQL database
* Retrieve them dynamically
* Secure the app with a custom implementation of `UserDetailsService`

Stay tuned — like, share, and subscribe for more hands-on Spring Security insights!

---

Let me know if you want this in Markdown, PDF, or a full-blown HTML blog template.
