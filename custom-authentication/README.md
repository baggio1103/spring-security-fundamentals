---

# 🔐 Spring Security, Part IV — Custom API Key Authentication

Welcome back to the **Spring Security Series**!

If you haven’t read the earlier tutorials, I highly recommend starting there — they lay the foundation for what we’re about to build.

In this post, we’ll implement a **custom API key authentication mechanism** in Spring Security. While there are many built-in options (Basic Auth, JWT, OAuth2, OIDC), building your own authentication from scratch gives you a **deeper understanding** of how Spring Security works internally.

---

## 🧠 What You’ll Learn

* What authentication is in Spring Security
* How `Authentication`, `AuthenticationManager`, `AuthenticationProvider`, and filters work together
* How to implement a custom `ApiKeyAuthentication` mechanism
* How to test the custom authentication flow

---

## 🔐 The Use Case: API Key Authentication

We want to authenticate clients using a **shared API key** passed in a custom header (`X-API-Key`), rather than usernames and passwords.

If the API key is valid:
✅ The client is authenticated.

If it's missing or incorrect:
❌ The server responds with `401 Unauthorized`.

---

## ⚙️ Authentication: The Basics

In Spring Security, the `Authentication` interface represents a request to authenticate. It includes data like credentials, authorities, and authentication status.

```java
public interface Authentication extends Principal, Serializable {
    Collection<? extends GrantedAuthority> getAuthorities();
    Object getCredentials();
    Object getDetails();
    Object getPrincipal();
    boolean isAuthenticated();
    void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException;
}
```

To build custom authentication, we'll create our own implementation of this interface.

---

## 🏗 Project Setup

Here’s a minimal `build.gradle.kts` for our Kotlin Spring Boot app:

> 💡 **What this does:**
> Includes Spring Security, Web, Jackson for JSON, and Kotlin Reflect. We're also using Java 21.

```kotlin
plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.eatify"
version = "0.0.1-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
```

---

## 📦 A Simple Controller for Testing

> 💡 **What this does:**
> Just a dummy controller to test if authentication works.

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

## 🔐 Step 1: Custom `Authentication` Implementation

> 💡 **What this does:**
> Implements the `Authentication` interface. This class holds the API key and the authentication state.

```kotlin
class ApiKeyAuthentication(
    val apiKey: String,
    private val authenticated: Boolean
) : Authentication {

    override fun getName() = apiKey
    override fun getAuthorities() = emptyList<GrantedAuthority>()
    override fun getCredentials() = null
    override fun getDetails() = null
    override fun getPrincipal() = null
    override fun isAuthenticated() = authenticated
    override fun setAuthenticated(isAuthenticated: Boolean) {}
}
```

---

## 🔐 Step 2: Custom `AuthenticationProvider`

> 💡 **What this does:**
> Checks if the provided `ApiKeyAuthentication` contains the correct key (from config). If not, throws an error.

```kotlin
@Component
class ApiKeyAuthenticationProvider(
    @Value("\${authentication.api.secret-key}")
    private val apiKey: String,
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val apiKeyAuthentication = authentication as ApiKeyAuthentication
        if (apiKeyAuthentication.apiKey != apiKey) {
            throw BadCredentialsException("API key is incorrect!")
        }
        return ApiKeyAuthentication(apiKey, true)
    }

    override fun supports(authentication: Class<*>): Boolean =
        authentication == ApiKeyAuthentication::class.java
}
```

> 🛠 In your `application.yml`:

```yaml
authentication:
  api:
    secret-key: "some_secret_key" ### for testing purpose only
```

---

## 🔐 Step 3: Custom `AuthenticationManager`

> 💡 **What this does:**
> Delegates authentication to the provider (if it supports the request type). Throws error if unsupported.

```kotlin
@Component
class ApiKeyAuthenticationManager(
    private val authenticationProvider: AuthenticationProvider
) : AuthenticationManager {

    override fun authenticate(authentication: Authentication): Authentication {
        if (authenticationProvider.supports(authentication.javaClass)) {
            return authenticationProvider.authenticate(authentication)
        }
        throw AuthenticationServiceException("AuthenticationProvider not found")
    }
}
```

---

## 🔐 Step 4: Custom `OncePerRequestFilter`

> 💡 **What this does:**
>
> * Extracts the API key from the `X-API-Key` header
> * Authenticates it using the `AuthenticationManager`
> * Sets the `SecurityContextHolder` if successful
> * Otherwise, returns `401 Unauthorized`

```kotlin
@Component
class ApiKeyAuthenticationFilter(
    private val authenticationManager: AuthenticationManager,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKey = request.getHeader("X-API-Key")

        if (apiKey == null) {
            logger.info("API key is missing, continuing filter chain")
            filterChain.doFilter(request, response)
            return
        }

        try {
            val apiKeyAuthentication = authenticationManager.authenticate(ApiKeyAuthentication(apiKey, false))
            SecurityContextHolder.getContext().authentication = apiKeyAuthentication
            filterChain.doFilter(request, response)
        } catch (ex: AuthenticationException) {
            SecurityContextHolder.clearContext()
            logger.error("Authentication failed: ${ex.message}")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write("""{"error": "Authentication error"}""")
        }
    }
}
```

---

## 🛡 Step 5: Configure Spring Security

> 💡 **What this does:**
> Adds the custom filter before `BasicAuthenticationFilter`. Any request must be authenticated to proceed.

```kotlin
@Configuration
class SecurityConfig(
    private val apiKeyAuthenticationFilter: ApiKeyAuthenticationFilter,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .addFilterAt(apiKeyAuthenticationFilter, BasicAuthenticationFilter::class.java)
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            .build()
}
```

---

## 🧪 Testing the Setup

### ❌ No API Key:

```bash
curl http://localhost:8080/api/orders
# ➜ 403 Forbidden (Spring filters it)
```

### ❌ Wrong API Key:

```bash
curl -H "X-API-Key: wrong_key" http://localhost:8080/api/orders
# ➜ 401 Unauthorized
```

### ✅ Correct API Key:

```bash
curl -H "X-API-Key: some_secret_key" http://localhost:8080/api/orders
# ➜ 200 OK
```

---

## ✅ Recap: What We Built

We implemented a **fully working custom authentication flow** using:

* `ApiKeyAuthentication` (custom `Authentication`)
* `ApiKeyAuthenticationProvider`
* `ApiKeyAuthenticationManager`
* `ApiKeyAuthenticationFilter`
* A secure filter chain configuration

---

## 🚀 What’s Next?

In the next tutorial, we’ll look at:

* Supporting **multiple authentication providers** (e.g., API key + database users)
* Adding **fine-grained authorization** (method-level security)
* Improving **error handling and response formats**

Stay tuned! 🎯

---
